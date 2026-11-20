package io.github.ceniorpomidor.workcalendar.data.repo

import androidx.room.withTransaction
import io.github.ceniorpomidor.workcalendar.data.db.AppDatabase
import io.github.ceniorpomidor.workcalendar.data.db.toDomain
import io.github.ceniorpomidor.workcalendar.data.db.toEntity
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.domain.model.ChangeCategory
import io.github.ceniorpomidor.workcalendar.domain.model.ScheduleAssignment
import io.github.ceniorpomidor.workcalendar.domain.model.SchedulePattern
import io.github.ceniorpomidor.workcalendar.domain.model.ScheduleTemplate
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftOrigin
import io.github.ceniorpomidor.workcalendar.domain.schedule.AssignmentTimeline
import io.github.ceniorpomidor.workcalendar.domain.schedule.ChangeType
import io.github.ceniorpomidor.workcalendar.domain.schedule.RegenerationPlan
import io.github.ceniorpomidor.workcalendar.domain.schedule.RegenerationPlanner
import io.github.ceniorpomidor.workcalendar.domain.schedule.ScheduleCut
import io.github.ceniorpomidor.workcalendar.domain.schedule.TimelineChange
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.YearMonth

data class ApplyRequest(
    val templateId: Long?,
    val name: String,
    val pattern: SchedulePattern,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val overwriteUserChanges: Boolean,
)

/** What applying a schedule will do; shown to the user before anything is changed. */
data class ApplyPreview(
    val request: ApplyRequest,
    val timeline: TimelineChange,
    val plan: RegenerationPlan,
    val range: DateRange,
) {
    val addCount: Int get() = plan.count(ChangeType.ADD)
    val updateCount: Int get() = plan.count(ChangeType.UPDATE) + plan.count(ChangeType.RESET_USER_CHANGE)
    val removeCount: Int get() = plan.count(ChangeType.REMOVE) + plan.count(ChangeType.REMOVE_USER_CHANGE)
    val keptCount: Int get() = plan.kept.size
}

class ScheduleRepository(
    private val db: AppDatabase,
    private val settings: SettingsRepository,
    private val calc: CalcRepository,
    private val log: ChangeLogger,
    private val changes: DataChanges,
    private val clock: AppClock,
) {
    private val generationMutex = Mutex()

    val templates: Flow<List<ScheduleTemplate>> = db.scheduleDao().observeTemplates().map { list -> list.map { it.toDomain() } }

    val assignments: Flow<List<ScheduleAssignment>> = db.scheduleDao().observeAssignments().map { list -> list.map { it.toDomain() } }

    suspend fun getTemplate(id: Long): ScheduleTemplate? = db.scheduleDao().getTemplate(id)?.toDomain()

    suspend fun getAssignments(): List<ScheduleAssignment> = db.scheduleDao().getAssignments().map { it.toDomain() }

    suspend fun saveTemplate(template: ScheduleTemplate): Long {
        val now = clock.now()
        return if (template.id == 0L) {
            val id = db.scheduleDao().insertTemplate(template.copy(createdAt = now, updatedAt = now).toEntity())
            log.log(ChangeCategory.SCHEDULE, "Шаблон создан", "«${template.name}»", id)
            id
        } else {
            db.scheduleDao().updateTemplate(template.copy(updatedAt = now).toEntity())
            log.log(ChangeCategory.SCHEDULE, "Шаблон изменён", "«${template.name}» (календарь меняется только при применении)", template.id)
            template.id
        }
    }

    suspend fun deleteTemplate(template: ScheduleTemplate) {
        db.scheduleDao().deleteTemplate(template.toEntity())
        log.log(ChangeCategory.SCHEDULE, "Шаблон удалён", "«${template.name}». Уже созданные смены не изменены", template.id)
    }

    fun horizonEnd(settings: AppSettings): LocalDate = YearMonth.from(clock.today()).plusMonths(settings.schedule.horizonMonths.toLong()).atEndOfMonth()

    /** Calculates the effect of applying [request] without changing anything. */
    suspend fun preview(request: ApplyRequest): ApplyPreview {
        val context = calc.current()
        val existingAssignments = getAssignments()
        val newAssignment = ScheduleAssignment(
            id = NEW_ASSIGNMENT_ID,
            templateId = request.templateId,
            templateName = request.name,
            pattern = request.pattern,
            startDate = request.startDate,
            endDate = request.endDate,
            createdAt = clock.now(),
        )
        var tempId = NEW_ASSIGNMENT_ID - 1
        val raw = AssignmentTimeline.insert(existingAssignments, newAssignment)
        val change = raw.copy(created = raw.created.map { if (it.id == 0L) it.copy(id = tempId--) else it })
        val timeline = change.applyTo(existingAssignments)

        val horizon = horizonEnd(context.settings)
        val lastTemplate = db.shiftDao().lastTemplateDate()
        var end = if (lastTemplate != null && lastTemplate > horizon) lastTemplate else horizon
        request.endDate?.let { if (it < end) end = it }
        if (end < request.startDate) end = YearMonth.from(request.startDate).plusMonths(1).atEndOfMonth()
        request.endDate?.let { if (it < end) end = it }
        val range = DateRange(request.startDate, end)

        val existing = db.shiftDao().getRange(range.start, range.endInclusive).map { it.toDomain() }
        val absences = db.absenceDao().getOverlapping(range.start, range.endInclusive).map { it.toDomain() }
        val plan = RegenerationPlanner.plan(range, timeline, existing, absences, context.holidays, request.overwriteUserChanges)
        return ApplyPreview(request, change, plan, range)
    }

    /** Applies a previewed schedule change in one transaction. */
    suspend fun apply(preview: ApplyPreview) {
        val now = clock.now()
        generationMutex.withLock {
            db.withTransaction {
                val dao = db.scheduleDao()
                preview.timeline.removed.forEach { dao.deleteAssignment(it.id) }
                preview.timeline.updated.forEach { dao.updateAssignment(it.toEntity()) }
                val ids = HashMap<Long, Long>()
                for (created in preview.timeline.created) {
                    ids[created.id] = dao.insertAssignment(created.copy(id = 0).toEntity())
                }

                fun fix(shift: Shift): Shift {
                    val assignmentId = shift.assignmentId ?: return shift
                    return if (assignmentId < 0) shift.copy(assignmentId = ids[assignmentId]) else shift
                }

                val shiftDao = db.shiftDao()
                val deletes = preview.plan.deletes.map { it.id }
                if (deletes.isNotEmpty()) shiftDao.deleteByIds(deletes)
                val updates = preview.plan.updates.map { fix(it).copy(updatedAt = now).toEntity() }
                if (updates.isNotEmpty()) shiftDao.updateAll(updates)
                val inserts = preview.plan.inserts.map { fix(it).copy(createdAt = now, updatedAt = now).toEntity() }
                if (inserts.isNotEmpty()) shiftDao.insertIgnore(inserts)
            }
        }
        val r = preview.request
        val period = if (r.endDate != null) Formats.period(r.startDate, r.endDate) else "с ${Formats.date(r.startDate)}"
        log.log(
            ChangeCategory.SCHEDULE,
            "График применён",
            "«${r.name}» $period: добавлено ${preview.addCount}, изменено ${preview.updateCount}, удалено ${preview.removeCount}, " +
                "сохранено ручных изменений ${preview.keptCount}",
        )
        changes.notifyChanged()
    }

    /** Creates missing template shifts for [range] (idempotent, keeps all user changes). */
    suspend fun ensureGenerated(range: DateRange): Int = generationMutex.withLock {
        val assignments = getAssignments()
        if (assignments.isEmpty()) return@withLock 0
        val firstStart = assignments.minOf { it.startDate }
        if (range.endInclusive < firstStart) return@withLock 0
        val effective = DateRange(if (range.start < firstStart) firstStart else range.start, range.endInclusive)
        val context = calc.current()
        val existing = db.shiftDao().getRange(effective.start, effective.endInclusive).map { it.toDomain() }
        val absences = db.absenceDao().getOverlapping(effective.start, effective.endInclusive).map { it.toDomain() }
        val plan = RegenerationPlanner.planMissing(effective, assignments, existing, absences, context.holidays)
        if (plan.inserts.isEmpty()) return@withLock 0
        val now = clock.now()
        val inserted = db.shiftDao().insertIgnore(plan.inserts.map { it.copy(createdAt = now, updatedAt = now).toEntity() }).count { it > 0 }
        if (inserted > 0) changes.notifyChanged()
        inserted
    }

    /** Fills the calendar up to the configured horizon. */
    suspend fun ensureHorizon(): Int {
        val s = settings.get()
        return ensureGenerated(DateRange(clock.today().minusDays(7), horizonEnd(s)))
    }

    /**
     * Removes an applied schedule from the calendar. Untouched future shifts created by it are
     * deleted when [removeFutureShifts]; past and edited shifts always stay.
     */
    suspend fun removeAssignment(assignment: ScheduleAssignment, removeFutureShifts: Boolean) {
        val today = clock.today()
        generationMutex.withLock {
            db.withTransaction {
                db.scheduleDao().deleteAssignment(assignment.id)
                if (removeFutureShifts) {
                    val end = assignment.endDate ?: db.shiftDao().lastTemplateDate() ?: today
                    val from = if (assignment.startDate > today) assignment.startDate else today
                    if (end >= from) {
                        val ids = db.shiftDao().getRange(from, end).map { it.toDomain() }
                            .filter { it.origin == ShiftOrigin.TEMPLATE && it.isPristineTemplate && it.assignmentId == assignment.id }
                            .map { it.id }
                        if (ids.isNotEmpty()) db.shiftDao().deleteByIds(ids)
                    }
                }
            }
        }
        log.log(ChangeCategory.SCHEDULE, "График отменён", "«${assignment.templateName}» с ${Formats.date(assignment.startDate)}", assignment.id)
        changes.notifyChanged()
    }

    private suspend fun shiftsFrom(date: LocalDate): List<Shift> {
        val last = db.shiftDao().lastTemplateDate()?.let { maxOf(it, date) } ?: date
        return db.shiftDao().getRange(date, last.plusYears(1)).map { it.toDomain() }
    }

    /** What [stopFrom] would do, for the confirmation. */
    suspend fun previewStop(date: LocalDate, includeManual: Boolean): ScheduleCut =
        ScheduleCut.plan(getAssignments(), shiftsFrom(date), date, includeManual)

    /** Extra and manual planned shifts from [date]. */
    suspend fun manualShiftsFrom(date: LocalDate): Int = ScheduleCut.manualCount(shiftsFrom(date), date)

    /**
     * Stops the applied schedules from [date] and deletes their shifts from this day on (extra and
     * manual ones too when [includeManual]), so that a new schedule can be applied. Shifts with
     * entered hours stay.
     */
    suspend fun stopFrom(date: LocalDate, includeManual: Boolean): ScheduleCut {
        val cut = generationMutex.withLock {
            val plan = ScheduleCut.plan(getAssignments(), shiftsFrom(date), date, includeManual)
            db.withTransaction {
                val dao = db.scheduleDao()
                plan.removed.forEach { dao.deleteAssignment(it.id) }
                plan.ended.forEach { dao.updateAssignment(it.toEntity()) }
                val ids = plan.deleted.map { it.id }
                if (ids.isNotEmpty()) db.shiftDao().deleteByIds(ids)
            }
            plan
        }
        log.log(
            ChangeCategory.SCHEDULE,
            "График остановлен",
            "С ${Formats.date(date)} удалено смен: ${cut.deleted.size}" + if (includeManual) " (включая ручные)" else "",
        )
        changes.notifyChanged()
        return cut
    }

    companion object {
        const val NEW_ASSIGNMENT_ID: Long = -1
    }
}
