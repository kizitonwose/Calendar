package io.github.ceniorpomidor.workcalendar.data.repo

import androidx.room.withTransaction
import io.github.ceniorpomidor.workcalendar.data.db.AppDatabase
import io.github.ceniorpomidor.workcalendar.data.db.toDomain
import io.github.ceniorpomidor.workcalendar.data.db.toEntity
import io.github.ceniorpomidor.workcalendar.domain.model.ChangeCategory
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftKind
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftOrigin
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class ValidationException(message: String) : Exception(message)

/** Operations on single shifts. Each operation is idempotent and logged. */
class ShiftRepository(
    private val db: AppDatabase,
    private val calc: CalcRepository,
    private val log: ChangeLogger,
    private val changes: DataChanges,
    private val clock: AppClock,
) {
    private val dao get() = db.shiftDao()

    fun observeRange(range: DateRange): Flow<List<Shift>> = dao.observeRange(range.start, range.endInclusive).map { list -> list.map { it.toDomain() } }

    fun observeShift(id: Long): Flow<Shift?> = dao.observeById(id).map { it?.toDomain() }

    /** Planned shifts that ended before [until] and have no hours yet. */
    fun observeUnconfirmed(until: LocalDateTime): Flow<List<Shift>> = dao.observePlannedEndedBefore(until).map { list -> list.map { it.toDomain() } }

    fun observeNext(from: LocalDateTime, limit: Int): Flow<List<Shift>> = dao.observeNext(from, limit).map { list -> list.map { it.toDomain() } }

    suspend fun get(id: Long): Shift? = dao.getById(id)?.toDomain()

    suspend fun getRange(range: DateRange): List<Shift> = dao.getRange(range.start, range.endInclusive).map { it.toDomain() }

    suspend fun getUnconfirmed(until: LocalDateTime): List<Shift> = dao.getPlannedEndedBefore(until).map { it.toDomain() }

    private fun validate(shift: Shift) {
        if (!shift.plannedEnd.isAfter(shift.plannedStart)) throw ValidationException("Окончание смены должно быть позже начала")
        val minutes = ChronoUnit.MINUTES.between(shift.plannedStart, shift.plannedEnd)
        if (minutes > 48 * 60) throw ValidationException("Смена не может быть длиннее 48 часов")
        if (shift.plannedBreakMinutes < 0 || shift.plannedBreakMinutes >= minutes) throw ValidationException("Перерыв должен быть короче смены")
        shift.hourlyRateOverride?.let { if (it.isNegative) throw ValidationException("Ставка не может быть отрицательной") }
    }

    /** Creates a manual shift (also an extra shift on a day off). */
    suspend fun create(shift: Shift): Long {
        val now = clock.now()
        val manual = shift.copy(id = 0, origin = ShiftOrigin.MANUAL, status = ShiftStatus.PLANNED, createdAt = now, updatedAt = now)
        validate(manual)
        val id = dao.insert(manual.toEntity())
        val kind = if (manual.kind == ShiftKind.EXTRA) "Доп. смена" else "Смена"
        log.log(ChangeCategory.SHIFT, "$kind добавлена", "${Formats.date(manual.date)} ${Formats.timeRange(manual.plannedStart, manual.plannedEnd)}", id)
        changes.notifyChanged()
        return id
    }

    /** Saves edits of the plan (times, break, kind, rate, title, note). */
    suspend fun updatePlan(updated: Shift) {
        val old = get(updated.id) ?: throw ValidationException("Смена не найдена")
        validate(updated)
        val planChanged = old.plannedStart != updated.plannedStart || old.plannedEnd != updated.plannedEnd ||
            old.plannedBreakMinutes != updated.plannedBreakMinutes || old.kind != updated.kind ||
            old.hourlyRateOverride != updated.hourlyRateOverride
        var result = updated.copy(
            userModified = old.userModified || (old.origin == ShiftOrigin.TEMPLATE && planChanged),
            updatedAt = clock.now(),
        )
        // A confirmed shift keeps its hours, its pay is recalculated only if the rate inputs changed.
        if (result.status == ShiftStatus.CONFIRMED && planChanged) {
            val context = calc.current()
            result = result.copy(pay = context.pay.calculate(result, result.workedMinutes ?: 0))
        }
        dao.update(result.toEntity())
        if (planChanged) {
            log.log(
                ChangeCategory.SHIFT,
                "Смена изменена",
                "${Formats.date(old.date)}: ${Formats.timeRange(old.plannedStart, old.plannedEnd)} → ${Formats.timeRange(result.plannedStart, result.plannedEnd)}",
                old.id,
            )
        }
        changes.notifyChanged()
    }

    suspend fun updateNote(id: Long, note: String) {
        val old = get(id) ?: return
        dao.update(old.copy(note = note, updatedAt = clock.now()).toEntity())
    }

    /**
     * Saves actual worked time. Setting the hours replaces previous values (never adds), so a
     * repeated confirmation cannot double the hours or earnings.
     */
    suspend fun confirm(
        id: Long,
        workedMinutes: Int,
        actualStart: LocalDateTime? = null,
        actualEnd: LocalDateTime? = null,
        actualBreakMinutes: Int? = null,
    ): Shift {
        if (workedMinutes < 0 || workedMinutes > 48 * 60) throw ValidationException("Недопустимое количество часов")
        if (actualStart != null && actualEnd != null && !actualEnd.isAfter(actualStart)) {
            throw ValidationException("Фактическое окончание должно быть позже начала")
        }
        val context = calc.current()
        val now = clock.now()
        val confirmed = db.withTransaction {
            val old = get(id) ?: throw ValidationException("Смена не найдена")
            if (old.status == ShiftStatus.CANCELLED || old.status == ShiftStatus.DELETED || old.status == ShiftStatus.MOVED) {
                throw ValidationException("Смена отменена или перенесена")
            }
            val base = old.copy(
                status = ShiftStatus.CONFIRMED,
                workedMinutes = workedMinutes,
                actualStart = actualStart,
                actualEnd = actualEnd,
                actualBreakMinutes = actualBreakMinutes,
                absenceId = if (old.status == ShiftStatus.COVERED) null else old.absenceId,
                confirmedAt = now,
                updatedAt = now,
            )
            // Keep the rate of the first confirmation unless the rate inputs were changed.
            val previousRate = old.pay?.hourlyRate
            val pay = context.pay.calculate(base, workedMinutes)
            val result = base.copy(pay = if (previousRate != null && pay != null && old.hourlyRateOverride == null) keepRate(base, workedMinutes, previousRate, context) else pay)
            dao.update(result.toEntity())
            val before = old.workedMinutes?.let { Formats.hours(it.toLong()) } ?: "—"
            log.log(
                ChangeCategory.SHIFT,
                if (old.status == ShiftStatus.CONFIRMED) "Часы исправлены" else "Часы подтверждены",
                "${Formats.date(old.date)}: $before → ${Formats.hours(workedMinutes.toLong())}" +
                    (result.pay?.let { ", начислено ${Formats.money(it.total)}" } ?: ", ставка не задана"),
                id,
            )
            result
        }
        changes.notifyChanged()
        return confirmed
    }

    private fun keepRate(shift: Shift, workedMinutes: Int, rate: Money, context: CalcContext): io.github.ceniorpomidor.workcalendar.domain.model.ShiftPay? =
        context.pay.calculate(shift.copy(hourlyRateOverride = rate), workedMinutes)

    /** Confirms the planned hours of a shift with one tap. */
    suspend fun confirmAsPlanned(id: Long): Shift {
        val shift = get(id) ?: throw ValidationException("Смена не найдена")
        val context = calc.current()
        return confirm(id, shift.plannedPaidMinutes(context.zone).toInt())
    }

    suspend fun confirmAllAsPlanned(ids: List<Long>): Int {
        var count = 0
        for (id in ids) {
            val shift = get(id) ?: continue
            if (shift.status != ShiftStatus.PLANNED) continue
            confirmAsPlanned(id)
            count++
        }
        return count
    }

    /** The shift did not take place: zero hours, no earnings. */
    suspend fun markMissed(id: Long) {
        val old = get(id) ?: return
        val now = clock.now()
        dao.update(
            old.copy(
                status = ShiftStatus.MISSED,
                workedMinutes = 0,
                pay = null,
                actualStart = null,
                actualEnd = null,
                confirmedAt = now,
                updatedAt = now,
            ).toEntity(),
        )
        log.log(ChangeCategory.SHIFT, "Смена не состоялась", Formats.date(old.date), id)
        changes.notifyChanged()
    }

    /** Returns a confirmed or missed shift to the "not confirmed" state. */
    suspend fun resetConfirmation(id: Long) {
        val old = get(id) ?: return
        val status = if (old.absenceId != null) ShiftStatus.COVERED else ShiftStatus.PLANNED
        dao.update(
            old.copy(status = status, workedMinutes = null, pay = null, actualStart = null, actualEnd = null, actualBreakMinutes = null, confirmedAt = null, updatedAt = clock.now())
                .toEntity(),
        )
        log.log(ChangeCategory.SHIFT, "Подтверждение снято", "${Formats.date(old.date)}: было ${old.workedMinutes?.let { Formats.hours(it.toLong()) } ?: "—"}", id)
        changes.notifyChanged()
    }

    suspend fun cancel(id: Long) {
        val old = get(id) ?: return
        dao.update(old.copy(status = ShiftStatus.CANCELLED, userModified = old.origin == ShiftOrigin.TEMPLATE || old.userModified, updatedAt = clock.now()).toEntity())
        log.log(ChangeCategory.SHIFT, "Смена отменена", "${Formats.date(old.date)} ${Formats.timeRange(old.plannedStart, old.plannedEnd)}", id)
        changes.notifyChanged()
    }

    /** Brings back a cancelled shift. */
    suspend fun restore(id: Long) {
        val old = get(id) ?: return
        if (old.status != ShiftStatus.CANCELLED && old.status != ShiftStatus.DELETED) return
        dao.update(old.copy(status = ShiftStatus.PLANNED, updatedAt = clock.now()).toEntity())
        log.log(ChangeCategory.SHIFT, "Смена восстановлена", Formats.date(old.date), id)
        changes.notifyChanged()
    }

    /**
     * Deletes a shift. Template shifts stay as hidden tombstones so the schedule does not
     * create them again; manual shifts are removed completely.
     */
    suspend fun delete(id: Long) {
        val old = get(id) ?: return
        if (old.origin == ShiftOrigin.TEMPLATE) {
            dao.update(old.copy(status = ShiftStatus.DELETED, userModified = true, updatedAt = clock.now()).toEntity())
        } else {
            dao.delete(old.toEntity())
        }
        log.log(ChangeCategory.SHIFT, "Смена удалена", "${Formats.date(old.date)} ${Formats.timeRange(old.plannedStart, old.plannedEnd)}", id)
        changes.notifyChanged()
    }

    /**
     * Moves a shift to [newDate] keeping its time of day (or [newStart] if given). The original
     * stays on its date as a "moved" marker; other dates and the schedule are not affected.
     */
    suspend fun move(id: Long, newDate: LocalDate, newStart: LocalDateTime? = null): Long {
        val now = clock.now()
        val newId = db.withTransaction {
            val old = get(id) ?: throw ValidationException("Смена не найдена")
            if (old.isClosed) throw ValidationException("Подтверждённую смену нельзя перенести")
            if (newDate == old.date && newStart == null) throw ValidationException("Выберите другую дату")
            val duration = ChronoUnit.MINUTES.between(old.plannedStart, old.plannedEnd)
            val start = newStart ?: newDate.atTime(old.plannedStart.toLocalTime())
            val moved = old.copy(
                id = 0,
                date = start.toLocalDate(),
                plannedStart = start,
                plannedEnd = start.plusMinutes(duration),
                origin = ShiftOrigin.MANUAL,
                status = ShiftStatus.PLANNED,
                userModified = false,
                assignmentId = null,
                absenceId = null,
                movedFromShiftId = old.id,
                movedFromDate = old.date,
                movedToDate = null,
                createdAt = now,
                updatedAt = now,
            )
            validate(moved)
            val insertedId = dao.insert(moved.toEntity())
            dao.update(
                old.copy(
                    status = ShiftStatus.MOVED,
                    movedToDate = moved.date,
                    userModified = old.origin == ShiftOrigin.TEMPLATE || old.userModified,
                    updatedAt = now,
                ).toEntity(),
            )
            log.log(ChangeCategory.SHIFT, "Смена перенесена", "${Formats.date(old.date)} → ${Formats.date(moved.date)} ${Formats.timeRange(moved.plannedStart, moved.plannedEnd)}", insertedId)
            insertedId
        }
        changes.notifyChanged()
        return newId
    }

    /** Recalculates pay of confirmed shifts in [range] with the current rates (explicit user action). */
    suspend fun recalculatePay(range: DateRange): Pair<Int, Money> {
        val context = calc.current()
        var count = 0
        var diff = Money.ZERO
        db.withTransaction {
            val shifts = getRange(range).filter { it.status == ShiftStatus.CONFIRMED }
            val updated = shifts.map { s ->
                val recalculated = context.pay.recalculated(s)
                diff += (recalculated.pay?.total ?: Money.ZERO) - (s.pay?.total ?: Money.ZERO)
                count++
                recalculated.copy(updatedAt = clock.now()).toEntity()
            }
            if (updated.isNotEmpty()) dao.updateAll(updated)
        }
        log.log(ChangeCategory.RATE, "Пересчёт смен", "${Formats.period(range.start, range.endInclusive)}: ${Formats.shifts(count)}, разница ${Formats.money(diff)}")
        changes.notifyChanged()
        return count to diff
    }

    /** Number of confirmed shifts in [range] whose pay would change after recalculation. */
    suspend fun previewRecalculation(range: DateRange): Pair<Int, Money> {
        val context = calc.current()
        val shifts = getRange(range).filter { it.status == ShiftStatus.CONFIRMED }
        var count = 0
        var diff = Money.ZERO
        for (s in shifts) {
            val newTotal = context.pay.recalculated(s).pay?.total ?: Money.ZERO
            val oldTotal = s.pay?.total ?: Money.ZERO
            if (newTotal != oldTotal) {
                count++
                diff += newTotal - oldTotal
            }
        }
        return count to diff
    }

    suspend fun search(query: String): List<Shift> = dao.search("%$query%").map { it.toDomain() }
}
