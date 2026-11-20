package io.github.ceniorpomidor.workcalendar.data.repo

import androidx.room.withTransaction
import io.github.ceniorpomidor.workcalendar.data.db.AppDatabase
import io.github.ceniorpomidor.workcalendar.data.db.toDomain
import io.github.ceniorpomidor.workcalendar.data.db.toEntity
import io.github.ceniorpomidor.workcalendar.domain.absence.AbsencePayCalculator
import io.github.ceniorpomidor.workcalendar.domain.absence.AbsencePaymentDates
import io.github.ceniorpomidor.workcalendar.domain.absence.EarningsHistory
import io.github.ceniorpomidor.workcalendar.domain.absence.InsuranceExperience
import io.github.ceniorpomidor.workcalendar.domain.model.Absence
import io.github.ceniorpomidor.workcalendar.domain.model.AbsencePayCalculation
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceType
import io.github.ceniorpomidor.workcalendar.domain.model.ChangeCategory
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftOrigin
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.model.SickPayMethod
import io.github.ceniorpomidor.workcalendar.domain.model.VacationPayMethod
import io.github.ceniorpomidor.workcalendar.domain.notify.AbsencePayment
import io.github.ceniorpomidor.workcalendar.domain.schedule.AbsenceCoverage
import io.github.ceniorpomidor.workcalendar.domain.schedule.CoveragePlan
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** Result of checking an absence before saving. */
data class AbsenceCheck(
    val overlap: Absence?,
    val coverage: CoveragePlan,
)

class AbsenceRepository(
    private val db: AppDatabase,
    private val schedule: ScheduleRepository,
    private val calc: CalcRepository,
    private val log: ChangeLogger,
    private val changes: DataChanges,
    private val clock: AppClock,
) {
    private val dao get() = db.absenceDao()

    val all: Flow<List<Absence>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeRange(range: DateRange): Flow<List<Absence>> = dao.observeOverlapping(range.start, range.endInclusive).map { list -> list.map { it.toDomain() } }

    fun observe(id: Long): Flow<Absence?> = dao.observeById(id).map { it?.toDomain() }

    suspend fun get(id: Long): Absence? = dao.getById(id)?.toDomain()

    suspend fun getAll(): List<Absence> = dao.getAll().map { it.toDomain() }

    /** Overlaps with other absences and the effect on shifts. */
    suspend fun check(absence: Absence): AbsenceCheck {
        val others = dao.getOverlapping(absence.startDate, absence.endDate).map { it.toDomain() }
        val overlap = AbsenceCoverage.findOverlap(absence, others)
        val shifts = affectedShifts(absence)
        return AbsenceCheck(overlap, AbsenceCoverage.plan(absence.copy(id = if (absence.id == 0L) TEMP_ID else absence.id), shifts))
    }

    private suspend fun affectedShifts(absence: Absence): List<Shift> {
        val inRange = db.shiftDao().getRange(absence.startDate, absence.endDate).map { it.toDomain() }
        val covered = if (absence.id != 0L) db.shiftDao().getByAbsence(absence.id).map { it.toDomain() } else emptyList()
        return (inRange + covered).distinctBy { it.id }
    }

    /**
     * Saves the absence. Planned shifts of the period are kept for history as "covered" (or
     * removed when [keepShifts] is false) and excluded from hours and earnings.
     */
    suspend fun save(absence: Absence, keepShifts: Boolean = true): Long {
        val check = check(absence)
        check.overlap?.let { throw ValidationException("Период пересекается: ${it.displayTitle()} ${Formats.period(it.startDate, it.endDate)}") }
        // Planned shifts must exist for the period (needed for vacation pay by planned shifts).
        schedule.ensureGenerated(absence.range)
        val now = clock.now()
        val id = db.withTransaction {
            val id = if (absence.id == 0L) {
                dao.insert(absence.copy(createdAt = now, updatedAt = now).toEntity())
            } else {
                dao.update(absence.copy(updatedAt = now).toEntity())
                absence.id
            }
            val saved = absence.copy(id = id)
            val plan = AbsenceCoverage.plan(saved, affectedShifts(saved))
            val shiftDao = db.shiftDao()
            if (keepShifts) {
                if (plan.toCover.isNotEmpty()) shiftDao.updateAll(plan.toCover.map { it.copy(updatedAt = now).toEntity() })
            } else {
                for (shift in plan.toCover) {
                    if (shift.origin == ShiftOrigin.TEMPLATE) {
                        shiftDao.update(shift.copy(status = ShiftStatus.DELETED, absenceId = null, userModified = true, updatedAt = now).toEntity())
                    } else {
                        shiftDao.delete(shift.toEntity())
                    }
                }
            }
            if (plan.toRestore.isNotEmpty()) shiftDao.updateAll(plan.toRestore.map { it.copy(updatedAt = now).toEntity() })
            id
        }
        // Calculate the payment after the shifts are covered.
        val stored = get(id) ?: return id
        if (stored.manualAmount == null && stored.paid) {
            val calculation = calculate(stored)
            if (calculation != null) dao.update(stored.copy(calculation = calculation).toEntity())
        }
        val action = if (absence.id == 0L) "добавлен" else "изменён"
        log.log(
            ChangeCategory.ABSENCE,
            "${stored.displayTitle()} $action",
            "${Formats.period(stored.startDate, stored.endDate)} (${Formats.days(stored.calendarDays)})" +
                (get(id)?.amount?.let { ", сумма ${Formats.money(it)}" } ?: ""),
            id,
        )
        changes.notifyChanged()
        return id
    }

    suspend fun delete(absence: Absence) {
        val now = clock.now()
        db.withTransaction {
            val released = AbsenceCoverage.release(absence.id, db.shiftDao().getByAbsence(absence.id).map { it.toDomain() })
            if (released.isNotEmpty()) db.shiftDao().updateAll(released.map { it.copy(updatedAt = now).toEntity() })
            dao.delete(absence.toEntity())
        }
        log.log(ChangeCategory.ABSENCE, "${absence.displayTitle()} удалён", Formats.period(absence.startDate, absence.endDate), absence.id)
        changes.notifyChanged()
    }

    /** Recalculates and stores the automatic amount. */
    suspend fun recalculate(id: Long): Absence? {
        val absence = get(id) ?: return null
        val calculation = calculate(absence) ?: return absence
        val updated = absence.copy(calculation = calculation, updatedAt = clock.now())
        dao.update(updated.toEntity())
        log.log(ChangeCategory.ABSENCE, "Пересчёт выплаты", "${absence.displayTitle()} ${Formats.period(absence.startDate, absence.endDate)}: ${Formats.money(calculation.total)}", id)
        changes.notifyChanged()
        return updated
    }

    /** Automatic calculation according to the settings, or null when it is disabled. */
    suspend fun calculate(absence: Absence): AbsencePayCalculation? {
        if (!absence.paid) return null
        val context = calc.current()
        val settings = context.settings
        val now = clock.now()
        return when (absence.type) {
            AbsenceType.VACATION -> when (settings.absence.vacationMethod) {
                VacationPayMethod.NONE -> null
                VacationPayMethod.PLANNED_SHIFTS -> {
                    val shifts = db.shiftDao().getRange(absence.startDate, absence.endDate).map { it.toDomain() }
                    AbsencePayCalculator.byPlannedShifts(absence, shifts, context.pay, now)
                }
                VacationPayMethod.AVERAGE_EARNINGS -> {
                    val others = getAll().filter { it.id != absence.id }
                    AbsencePayCalculator.byAverageEarnings(absence, history(context), others, context.holidays, settings.absence, now)
                }
            }
            AbsenceType.SICK -> when (settings.absence.sickMethod) {
                SickPayMethod.NONE -> null
                SickPayMethod.BENEFIT_255FZ -> {
                    val months = InsuranceExperience.months(settings.employmentStartDate, settings.priorExperienceMonths, absence.startDate)
                    AbsencePayCalculator.sickBenefit(absence, history(context), settings.absence, months, now)
                }
            }
            AbsenceType.UNPAID, AbsenceType.OTHER -> null
        }
    }

    private suspend fun history(context: CalcContext): EarningsHistory {
        val shifts = db.shiftDao().getAll().map { it.toDomain() }
        val absences = getAll()
        val accruals = db.financeDao().getAccruals().map { it.toDomain() }
        val external = db.financeDao().getExternal().map { it.toDomain() }
        return EarningsHistory.build(
            shifts,
            absences,
            accruals,
            external,
            context.pay,
            context.settings.employmentStartDate,
            context.settings.trackingStartDate,
        )
    }

    /** Expected payments (vacation pay, parts of the sick benefit) for reminders and finance. */
    suspend fun payments(absences: List<Absence>, context: CalcContext, paydays: List<LocalDate>): List<AbsencePayment> {
        val rules = context.settings.absence
        val result = ArrayList<AbsencePayment>()
        for (absence in absences) {
            if (!absence.paid) continue
            when (absence.type) {
                AbsenceType.VACATION -> result += AbsencePayment(
                    absence,
                    AbsencePayment.Part.VACATION,
                    AbsencePaymentDates.vacationPayDate(absence, context.holidays, rules.vacationNotifyBusinessDaysBefore),
                    absence.amount,
                )
                AbsenceType.SICK -> {
                    val employerPart = absence.calculation?.employerPart
                    val fundPart = absence.calculation?.fundPart
                    val fundDate = AbsencePaymentDates.sickFundDate(absence, context.holidays, rules.fundPaymentBusinessDays)
                    if (absence.manualAmount != null || employerPart == null) {
                        result += AbsencePayment(absence, AbsencePayment.Part.SICK_FUND, fundDate, absence.amount)
                    } else {
                        if (employerPart.isPositive) {
                            val employerDate = AbsencePaymentDates.sickEmployerDate(absence, paydays, context.holidays)
                            result += AbsencePayment(absence, AbsencePayment.Part.SICK_EMPLOYER, employerDate, employerPart)
                        }
                        result += AbsencePayment(absence, AbsencePayment.Part.SICK_FUND, fundDate, fundPart)
                    }
                }
                AbsenceType.OTHER -> absence.paymentDate?.let { date ->
                    result += AbsencePayment(absence, AbsencePayment.Part.OTHER, date, absence.amount)
                }
                AbsenceType.UNPAID -> Unit
            }
        }
        return result
    }

    suspend fun search(query: String): List<Absence> = dao.search("%$query%").map { it.toDomain() }

    companion object {
        private const val TEMP_ID = Long.MAX_VALUE
    }
}
