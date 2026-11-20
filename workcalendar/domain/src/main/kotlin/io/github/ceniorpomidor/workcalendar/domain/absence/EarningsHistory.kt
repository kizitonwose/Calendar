package io.github.ceniorpomidor.workcalendar.domain.absence

import io.github.ceniorpomidor.workcalendar.domain.model.Absence
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceType
import io.github.ceniorpomidor.workcalendar.domain.model.ExternalEarning
import io.github.ceniorpomidor.workcalendar.domain.model.ManualAccrual
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.pay.PayCalculator
import java.time.LocalDate
import java.time.YearMonth

/**
 * Earnings by month assembled from the app data and the amounts entered for the time before
 * the app was used. Basis for vacation and sick pay calculations.
 */
class EarningsHistory(
    /** Pay for confirmed shifts plus manual accruals minus deductions, by month. */
    private val workByMonth: Map<YearMonth, Money>,
    /** Vacation pay by month (part of the sick benefit base, not of the vacation base). */
    private val vacationPayByMonth: Map<YearMonth, Money>,
    private val external: List<ExternalEarning>,
    /** First date with data in the app; earlier periods need external amounts. */
    val dataStart: LocalDate?,
    val employmentStart: LocalDate?,
) {
    private val externalMonthly = external.filter { it.month != null }.associateBy { YearMonth.of(it.year, it.month!!) }
    private val externalYearly = external.filter { it.month == null }.associateBy { it.year }

    /** Data for the month is complete (from the app or entered manually). */
    fun hasDataFor(month: YearMonth): Boolean {
        if (externalMonthly.containsKey(month) || externalYearly.containsKey(month.year)) return true
        if (employmentStart != null && employmentStart.isAfter(month.atEndOfMonth())) return true // not employed yet: zero is correct
        val start = dataStart ?: return false
        return !start.isAfter(month.atDay(1)) || (employmentStart != null && !employmentStart.isBefore(start) && !employmentStart.isAfter(month.atEndOfMonth()))
    }

    /** Earnings for the vacation base (without vacation and sick pay). */
    fun workEarnings(month: YearMonth): Money? {
        externalMonthly[month]?.let { return it.amount }
        if (externalYearly.containsKey(month.year)) return null // only a yearly amount is known
        if (!hasDataFor(month)) return null
        return workByMonth[month] ?: Money.ZERO
    }

    /** Earnings for the sick benefit base for a calendar year (work + vacation pay), or null if unknown. */
    fun yearEarnings(year: Int): Money? {
        externalYearly[year]?.let { return it.amount }
        var total = Money.ZERO
        for (m in 1..12) {
            val month = YearMonth.of(year, m)
            val monthly = externalMonthly[month]
            if (monthly != null) {
                total += monthly.amount
                continue
            }
            if (!hasDataFor(month)) return null
            total += (workByMonth[month] ?: Money.ZERO) + (vacationPayByMonth[month] ?: Money.ZERO)
        }
        return total
    }

    companion object {
        fun build(
            shifts: List<Shift>,
            absences: List<Absence>,
            accruals: List<ManualAccrual>,
            external: List<ExternalEarning>,
            pay: PayCalculator,
            employmentStart: LocalDate?,
            trackingStart: LocalDate?,
        ): EarningsHistory {
            val work = HashMap<YearMonth, Money>()
            for (shift in shifts) {
                val earned = pay.earned(shift) ?: continue
                val month = YearMonth.from(shift.date)
                work[month] = (work[month] ?: Money.ZERO) + earned
            }
            for (accrual in accruals) {
                val month = YearMonth.from(accrual.date)
                work[month] = (work[month] ?: Money.ZERO) + accrual.signedAmount
            }
            val vacation = HashMap<YearMonth, Money>()
            for (absence in absences) {
                if (absence.type != AbsenceType.VACATION) continue
                val amount = absence.amount ?: continue
                for (date in absence.range) {
                    val month = YearMonth.from(date)
                    vacation[month] = (vacation[month] ?: Money.ZERO) + amount.scale(1, absence.calendarDays.toLong())
                }
            }
            val firstShift = shifts.filter { it.isClosed }.minOfOrNull { it.date }
            val firstAbsence = absences.minOfOrNull { it.startDate }
            val dataStart = trackingStart ?: listOfNotNull(firstShift, firstAbsence).minOrNull()
            return EarningsHistory(work, vacation, external, dataStart, employmentStart)
        }
    }
}
