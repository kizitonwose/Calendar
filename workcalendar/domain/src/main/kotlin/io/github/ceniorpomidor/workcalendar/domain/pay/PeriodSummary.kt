package io.github.ceniorpomidor.workcalendar.domain.pay

import io.github.ceniorpomidor.workcalendar.domain.model.Absence
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceType
import io.github.ceniorpomidor.workcalendar.domain.model.ManualAccrual
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftKind
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.model.sumOfMoney
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/** Aggregated hours and money for a period. All money values are before taxes unless stated. */
data class PeriodSummary(
    val range: DateRange,
    /** Planned paid minutes of all scheduled shifts (planned, confirmed, missed). */
    val plannedMinutes: Long,
    /** Confirmed worked minutes. */
    val workedMinutes: Long,
    /** Planned paid minutes of shifts that are not confirmed yet (past and upcoming). */
    val pendingMinutes: Long,
    /** Minutes of extra shifts plus overtime of regular shifts (confirmed). */
    val extraMinutes: Long,
    val holidayMinutes: Long,
    val scheduledShifts: Int,
    val workedShifts: Int,
    val missedShifts: Int,
    val cancelledShifts: Int,
    val movedShifts: Int,
    val extraShifts: Int,
    /** Planned shifts whose end has passed but hours are not entered. */
    val unconfirmedShifts: Int,
    val upcomingShifts: Int,
    val daysOff: Int,
    val vacationDays: Int,
    val sickDays: Int,
    val otherAbsenceDays: Int,
    /** Earnings for confirmed shifts. */
    val confirmedEarnings: Money,
    /** Earnings of the whole plan (planned hours of all scheduled shifts); null when no rate is set. */
    val plannedEarnings: Money?,
    /** Confirmed earnings plus the planned pay of shifts not yet confirmed; null when no rate is set. */
    val forecastEarnings: Money?,
    /** Bonuses for holiday, overtime and extra shifts included in [confirmedEarnings]. */
    val surcharges: Money,
    val vacationPay: Money,
    val sickPay: Money,
    val otherAbsencePay: Money,
    /** Absences without a known amount (paid but not calculated). */
    val absencesWithoutAmount: Int,
    val bonuses: Money,
    val deductions: Money,
    /** Planned shifts without a known hourly rate. */
    val shiftsWithoutRate: Int,
) {
    /** Accrued for work, absences and manual accruals minus manual deductions. */
    val grossAccrued: Money get() = confirmedEarnings + vacationPay + sickPay + otherAbsencePay + bonuses - deductions

    val averagePerShift: Money? get() = if (workedShifts > 0) confirmedEarnings.scale(1, workedShifts.toLong()) else null

    val averagePerHour: Money? get() = if (workedMinutes > 0) confirmedEarnings.scale(60, workedMinutes) else null
}

/** Builds [PeriodSummary]s from shifts, absences and accruals. */
class SummaryCalculator(private val pay: PayCalculator) {
    fun summarize(
        range: DateRange,
        shifts: List<Shift>,
        absences: List<Absence>,
        accruals: List<ManualAccrual>,
        now: LocalDateTime,
    ): PeriodSummary {
        val zone = pay.zone
        val inRange = shifts.filter { it.date in range }
        var plannedMinutes = 0L
        var workedMinutes = 0L
        var pendingMinutes = 0L
        var extraMinutes = 0L
        var holidayMinutes = 0L
        var scheduled = 0
        var worked = 0
        var missed = 0
        var cancelled = 0
        var moved = 0
        var extra = 0
        var unconfirmed = 0
        var upcoming = 0
        var confirmed = Money.ZERO
        var surcharges = Money.ZERO
        var planned: Money? = Money.ZERO
        var forecast: Money? = Money.ZERO
        var withoutRate = 0
        val workDates = HashSet<LocalDate>()

        for (shift in inRange) {
            when (shift.status) {
                ShiftStatus.CANCELLED -> cancelled++
                ShiftStatus.MOVED -> moved++
                else -> Unit
            }
            if (!shift.isActive) continue
            scheduled++
            workDates += shift.date
            if (shift.kind == ShiftKind.EXTRA) extra++
            val plannedPaid = shift.plannedPaidMinutes(zone)
            plannedMinutes += plannedPaid
            val estimate = pay.estimate(shift)
            if (estimate == null) {
                withoutRate++
                planned = null
            } else {
                planned = planned?.plus(estimate.total)
            }
            when (shift.status) {
                ShiftStatus.CONFIRMED -> {
                    worked++
                    val minutes = (shift.workedMinutes ?: 0).toLong()
                    workedMinutes += minutes
                    val snapshot = shift.pay ?: pay.calculate(shift, minutes.toInt())
                    if (snapshot != null) {
                        confirmed += snapshot.total
                        surcharges += snapshot.bonuses
                        holidayMinutes += snapshot.holidayMinutes
                        extraMinutes += if (shift.kind == ShiftKind.EXTRA) minutes else snapshot.overtimeMinutes.toLong()
                        forecast = forecast?.plus(snapshot.total)
                    } else {
                        extraMinutes += if (shift.kind == ShiftKind.EXTRA) minutes else (minutes - plannedPaid).coerceAtLeast(0)
                        forecast = null
                    }
                }
                ShiftStatus.MISSED -> missed++
                ShiftStatus.PLANNED -> {
                    pendingMinutes += plannedPaid
                    if (!now.isBefore(shift.plannedEnd)) unconfirmed++ else upcoming++
                    forecast = if (estimate == null) null else forecast?.plus(estimate.total)
                }
                else -> Unit
            }
        }

        var vacationDays = 0
        var sickDays = 0
        var otherDays = 0
        var vacationPay = Money.ZERO
        var sickPay = Money.ZERO
        var otherPay = Money.ZERO
        var withoutAmount = 0
        val absenceDates = HashSet<LocalDate>()
        for (absence in absences) {
            val part = absence.range.intersect(range) ?: continue
            absenceDates += part.dates()
            val days = part.days
            when (absence.type) {
                AbsenceType.VACATION, AbsenceType.UNPAID -> vacationDays += days
                AbsenceType.SICK -> sickDays += days
                AbsenceType.OTHER -> otherDays += days
            }
            val amount = absence.amount
            if (amount == null) {
                withoutAmount++
                continue
            }
            val share = amount.scale(days.toLong(), absence.calendarDays.toLong())
            when (absence.type) {
                AbsenceType.VACATION, AbsenceType.UNPAID -> vacationPay += share
                AbsenceType.SICK -> sickPay += share
                AbsenceType.OTHER -> otherPay += share
            }
        }

        val accrualsInRange = accruals.filter { it.date in range }
        val bonuses = accrualsInRange.filter { !it.type.isNegative }.sumOfMoney { it.amount }
        val deductions = accrualsInRange.filter { it.type.isNegative }.sumOfMoney { it.amount }
        val daysOff = range.count { it !in workDates && it !in absenceDates }

        return PeriodSummary(
            range = range,
            plannedMinutes = plannedMinutes,
            workedMinutes = workedMinutes,
            pendingMinutes = pendingMinutes,
            extraMinutes = extraMinutes,
            holidayMinutes = holidayMinutes,
            scheduledShifts = scheduled,
            workedShifts = worked,
            missedShifts = missed,
            cancelledShifts = cancelled,
            movedShifts = moved,
            extraShifts = extra,
            unconfirmedShifts = unconfirmed,
            upcomingShifts = upcoming,
            daysOff = daysOff,
            vacationDays = vacationDays,
            sickDays = sickDays,
            otherAbsenceDays = otherDays,
            confirmedEarnings = confirmed,
            plannedEarnings = planned,
            forecastEarnings = forecast,
            surcharges = surcharges,
            vacationPay = vacationPay,
            sickPay = sickPay,
            otherAbsencePay = otherPay,
            absencesWithoutAmount = withoutAmount,
            bonuses = bonuses,
            deductions = deductions,
            shiftsWithoutRate = withoutRate,
        )
    }

    /** Summaries for consecutive months (statistics). */
    fun monthly(
        months: List<YearMonth>,
        shifts: List<Shift>,
        absences: List<Absence>,
        accruals: List<ManualAccrual>,
        now: LocalDateTime,
    ): List<PeriodSummary> = months.map { summarize(DateRange.month(it), shifts, absences, accruals, now) }
}
