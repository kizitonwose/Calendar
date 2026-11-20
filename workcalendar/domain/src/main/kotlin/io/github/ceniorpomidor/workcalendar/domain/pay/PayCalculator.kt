package io.github.ceniorpomidor.workcalendar.domain.pay

import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.RatePeriod
import io.github.ceniorpomidor.workcalendar.domain.model.RateTable
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftKind
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftPay
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.time.HolidayCalendar
import io.github.ceniorpomidor.workcalendar.domain.time.TimeMath
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Calculates pay for single shifts. Used everywhere (calendar, finance, notifications) so that
 * all amounts are consistent.
 */
class PayCalculator(
    val rates: RateTable,
    val holidays: HolidayCalendar,
    val zone: ZoneId,
) {
    /** Hourly rate for the shift: individual rate or the rate effective on its date. */
    fun hourlyRateFor(shift: Shift): Money? = shift.hourlyRateOverride ?: rates.on(shift.date)?.hourlyRate

    /** Interval of actual work used to find holiday hours. */
    fun workInterval(shift: Shift, workedMinutes: Int): Pair<LocalDateTime, LocalDateTime> {
        val actualStart = shift.actualStart
        val actualEnd = shift.actualEnd
        if (actualStart != null && actualEnd != null && actualEnd.isAfter(actualStart)) return actualStart to actualEnd
        val breakMinutes = shift.actualBreakMinutes ?: shift.plannedBreakMinutes
        val start = actualStart ?: shift.plannedStart
        return start to start.plusMinutes((workedMinutes + breakMinutes).toLong())
    }

    /**
     * Pay for [workedMinutes] of the shift with the given rate period. Returns null when no
     * hourly rate is known.
     */
    fun calculate(shift: Shift, workedMinutes: Int, period: RatePeriod? = rates.on(shift.date)): ShiftPay? {
        val rate = shift.hourlyRateOverride ?: period?.hourlyRate ?: return null
        val worked = workedMinutes.coerceAtLeast(0)
        val (start, end) = workInterval(shift, worked)
        val holiday = holidayMinutes(start, end).coerceAtMost(worked.toLong()).toInt()
        val overtime = if (shift.kind == ShiftKind.REGULAR) (worked - shift.plannedPaidMinutes(zone).toInt()).coerceAtLeast(0) else 0
        val holidayPercent = period?.holidayBonusPercent ?: 0
        val overtimePercent = period?.overtimeBonusPercent ?: 0
        val extraPercent = if (shift.kind == ShiftKind.EXTRA) period?.extraShiftBonusPercent ?: 0 else 0
        return ShiftPay(
            hourlyRate = rate,
            paidMinutes = worked,
            holidayMinutes = holiday,
            overtimeMinutes = overtime,
            base = Money.forMinutes(worked.toLong(), rate),
            holidayBonus = Money.forMinutes(holiday.toLong(), rate, holidayPercent),
            overtimeBonus = Money.forMinutes(overtime.toLong(), rate, overtimePercent),
            extraShiftBonus = Money.forMinutes(worked.toLong(), rate, extraPercent),
            holidayPercent = holidayPercent,
            overtimePercent = overtimePercent,
            extraShiftPercent = extraPercent,
        )
    }

    /** Preliminary pay for the planned hours. */
    fun estimate(shift: Shift): ShiftPay? = calculate(shift, shift.plannedPaidMinutes(zone).toInt())

    /**
     * Earned amount of a confirmed shift: the saved snapshot, or a fresh calculation when the
     * snapshot is missing. Null for unconfirmed shifts or when no rate is known.
     */
    fun earned(shift: Shift): Money? {
        if (shift.status == ShiftStatus.MISSED) return Money.ZERO
        if (shift.status != ShiftStatus.CONFIRMED) return null
        shift.pay?.let { return it.total }
        val worked = shift.workedMinutes ?: return null
        return calculate(shift, worked)?.total
    }

    /** Recalculates the pay snapshot of a confirmed shift with the current rates and rules. */
    fun recalculated(shift: Shift): Shift {
        if (shift.status != ShiftStatus.CONFIRMED) return shift
        val worked = shift.workedMinutes ?: return shift
        return shift.copy(pay = calculate(shift, worked))
    }

    private fun holidayMinutes(start: LocalDateTime, end: LocalDateTime): Long {
        var total = 0L
        var day = start.toLocalDate()
        while (!day.isAfter(end.toLocalDate())) {
            if (holidays.isPublicHoliday(day)) {
                total += TimeMath.overlapMinutes(start, end, day.atStartOfDay(), day.plusDays(1).atStartOfDay(), zone)
            }
            day = day.plusDays(1)
        }
        return total
    }
}
