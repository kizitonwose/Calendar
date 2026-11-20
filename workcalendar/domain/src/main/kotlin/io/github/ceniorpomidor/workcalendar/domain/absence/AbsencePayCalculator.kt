package io.github.ceniorpomidor.workcalendar.domain.absence

import io.github.ceniorpomidor.workcalendar.domain.model.Absence
import io.github.ceniorpomidor.workcalendar.domain.model.AbsencePayCalculation
import io.github.ceniorpomidor.workcalendar.domain.model.AbsencePayMethod
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceRules
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceType
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.pay.PayCalculator
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.time.HolidayCalendar
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/**
 * Vacation pay and sick benefit calculations. The rules follow the Russian legislation in a
 * simplified form and are only used when enabled in the settings; results are estimates.
 */
object AbsencePayCalculator {
    private val AVERAGE_DAYS_IN_MONTH = BigDecimal("29.3")

    /** Vacation days to pay: calendar days, optionally without public holidays (art. 120). */
    fun vacationDays(absence: Absence, holidays: HolidayCalendar, excludeHolidays: Boolean): Int =
        if (excludeHolidays) absence.range.count { !holidays.isPublicHoliday(it) } else absence.calendarDays

    /** Pay for the shifts planned during the vacation, as if they were worked. */
    fun byPlannedShifts(absence: Absence, shifts: List<Shift>, pay: PayCalculator, now: LocalDateTime): AbsencePayCalculation {
        val planned = shifts.filter {
            it.date in absence.range &&
                ((it.status == ShiftStatus.COVERED && (it.absenceId == absence.id || absence.id == 0L)) || it.status == ShiftStatus.PLANNED)
        }
        val lines = ArrayList<String>()
        val warnings = ArrayList<String>()
        var total = Money.ZERO
        var minutes = 0L
        var missingRate = 0
        for (shift in planned) {
            val estimate = pay.estimate(shift)
            if (estimate == null) {
                missingRate++
                continue
            }
            total += estimate.total
            minutes += estimate.paidMinutes
        }
        lines += "Смен по графику в период отпуска: ${planned.size}"
        lines += "Часов по графику: ${Formats.hours(minutes)}"
        lines += "Оплата этих смен по действующей ставке: ${Formats.money(total)}"
        if (planned.isEmpty()) warnings += "На период отпуска нет запланированных смен — проверьте, что график применён"
        if (missingRate > 0) warnings += "Для ${Formats.shifts(missingRate)} не задана ставка"
        return AbsencePayCalculation(
            method = AbsencePayMethod.PLANNED_SHIFTS,
            total = total,
            days = absence.calendarDays,
            explanation = lines,
            insufficientData = planned.isEmpty() || missingRate > 0,
            warnings = warnings,
            calculatedAt = now,
        )
    }

    /**
     * Average earnings method (art. 139 of the Labour Code, Regulation No. 922): earnings for the
     * 12 months before the vacation month divided by 29.3 per fully worked month, multiplied by
     * the vacation days.
     */
    fun byAverageEarnings(
        absence: Absence,
        history: EarningsHistory,
        otherAbsences: List<Absence>,
        holidays: HolidayCalendar,
        rules: AbsenceRules,
        now: LocalDateTime,
    ): AbsencePayCalculation {
        val startMonth = YearMonth.from(absence.startDate)
        var earnings = Money.ZERO
        var days = BigDecimal.ZERO
        val missing = ArrayList<YearMonth>()
        for (i in 12 downTo 1) {
            val month = startMonth.minusMonths(i.toLong())
            val monthEarnings = history.workEarnings(month)
            if (monthEarnings == null) {
                missing += month
                continue
            }
            val calendarDays = month.lengthOfMonth()
            val excluded = excludedDays(month, otherAbsences.filter { it.id != absence.id }, history.employmentStart)
            if (excluded >= calendarDays) continue
            val monthDays = if (excluded == 0) {
                AVERAGE_DAYS_IN_MONTH
            } else {
                AVERAGE_DAYS_IN_MONTH.multiply(BigDecimal(calendarDays - excluded)).divide(BigDecimal(calendarDays), 6, RoundingMode.HALF_UP)
            }
            earnings += monthEarnings
            days = days.add(monthDays)
        }
        val vacationDays = vacationDays(absence, holidays, rules.excludeHolidaysFromVacation)
        val lines = ArrayList<String>()
        val warnings = ArrayList<String>()
        val periodStart = startMonth.minusMonths(12)
        lines += "Расчётный период: ${Formats.monthTitle(periodStart)} – ${Formats.monthTitle(startMonth.minusMonths(1))}"
        if (missing.isNotEmpty()) {
            warnings += "Нет данных о заработке за: ${missing.joinToString { "${Formats.monthShort(it.month)} ${it.year}" }}. " +
                "Укажите заработок прошлых периодов в настройках, иначе расчёт неточный"
        }
        if (days.signum() == 0 || earnings.isZero) {
            warnings += "Недостаточно данных для расчёта среднего заработка"
            return AbsencePayCalculation(
                method = AbsencePayMethod.AVERAGE_EARNINGS,
                total = Money.ZERO,
                days = vacationDays,
                explanation = lines,
                insufficientData = true,
                warnings = warnings,
                calculatedAt = now,
            )
        }
        val averageDaily = Money(BigDecimal(earnings.kopecks).divide(days, 0, RoundingMode.HALF_UP).longValueExact())
        val total = averageDaily * vacationDays
        lines += "Заработок за период: ${Formats.money(earnings)}"
        lines += "Учтено дней: ${days.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',')}"
        lines += "Среднедневной заработок: ${Formats.money(averageDaily)}"
        lines += "Дней отпуска к оплате: $vacationDays"
        lines += "Отпускные: ${Formats.money(averageDaily)} × $vacationDays = ${Formats.money(total)}"
        return AbsencePayCalculation(
            method = AbsencePayMethod.AVERAGE_EARNINGS,
            total = total,
            averageDaily = averageDaily,
            days = vacationDays,
            explanation = lines,
            insufficientData = missing.isNotEmpty(),
            warnings = warnings,
            calculatedAt = now,
        )
    }

    /**
     * Temporary disability benefit (Federal law 255-FZ): average daily earnings for the two
     * previous calendar years / 730 × experience percent × days of illness. The first
     * [AbsenceRules.employerSickDays] days are paid by the employer, the rest by the social fund.
     */
    fun sickBenefit(
        absence: Absence,
        history: EarningsHistory,
        rules: AbsenceRules,
        experienceMonths: Int,
        now: LocalDateTime,
    ): AbsencePayCalculation {
        val year = absence.startDate.year
        val lines = ArrayList<String>()
        val warnings = ArrayList<String>()
        var base = Money.ZERO
        var unknownYears = 0
        for (y in listOf(year - 2, year - 1)) {
            val earned = history.yearEarnings(y)
            if (earned == null) {
                unknownYears++
                warnings += "Нет данных о заработке за $y год — укажите его в настройках (справка о заработке)"
                lines += "Заработок за $y: нет данных"
                continue
            }
            val cap = rules.yearlyCaps[y]
            val counted = if (cap != null && earned > cap) cap else earned
            base += counted
            lines += if (counted != earned) {
                "Заработок за $y: ${Formats.money(earned)} (учтено не более ${Formats.money(cap!!)})"
            } else {
                "Заработок за $y: ${Formats.money(earned)}"
            }
        }
        var averageDaily = base.scale(1, 730)
        val minimumDaily = rules.minimumWage.scale(24, 730)
        lines += "Среднедневной заработок: ${Formats.money(base)} / 730 = ${Formats.money(averageDaily)}"
        if (averageDaily < minimumDaily) {
            averageDaily = minimumDaily
            lines += "Меньше минимального — применён минимум от МРОТ: ${Formats.money(minimumDaily)}"
        }
        val percent = InsuranceExperience.sickPercent(experienceMonths)
        lines += "Страховой стаж: ${InsuranceExperience.describe(experienceMonths)} → $percent%"
        val dailyBenefit = averageDaily.percent(percent)
        val days = absence.calendarDays
        var total = Money.ZERO
        var employerPart = Money.ZERO
        val employerDays = if (absence.type == AbsenceType.SICK) rules.employerSickDays.coerceAtLeast(0) else 0
        absence.range.forEachIndexed { index, date ->
            val daily = if (experienceMonths < 6) {
                val cap = rules.minimumWage.scale(1, date.lengthOfMonth().toLong())
                if (dailyBenefit > cap) cap else dailyBenefit
            } else {
                dailyBenefit
            }
            total += daily
            if (index < employerDays) employerPart += daily
        }
        if (experienceMonths < 6) lines += "Стаж менее 6 месяцев: пособие не больше МРОТ за месяц"
        lines += "Пособие в день: ${Formats.money(dailyBenefit)}"
        lines += "Дней болезни: $days"
        lines += "Итого: ${Formats.money(total)}"
        if (employerPart.isPositive) {
            lines += "За счёт работодателя (первые ${minOf(employerDays, days)} дн.): ${Formats.money(employerPart)}"
            lines += "За счёт Соцфонда: ${Formats.money(total - employerPart)}"
        }
        return AbsencePayCalculation(
            method = AbsencePayMethod.SICK_BENEFIT,
            total = total,
            employerPart = employerPart,
            fundPart = total - employerPart,
            averageDaily = averageDaily,
            percent = percent,
            days = days,
            explanation = lines,
            insufficientData = unknownYears > 0,
            warnings = warnings,
            calculatedAt = now,
        )
    }

    /** Days of [month] not counted in the vacation base: absences and days before employment. */
    fun excludedDays(month: YearMonth, absences: List<Absence>, employmentStart: LocalDate?): Int {
        val monthRange = DateRange.month(month)
        val excluded = HashSet<LocalDate>()
        for (absence in absences) {
            val part = absence.range.intersect(monthRange) ?: continue
            excluded += part.dates()
        }
        if (employmentStart != null && employmentStart.isAfter(monthRange.start)) {
            val end = if (employmentStart.isAfter(monthRange.endInclusive)) monthRange.endInclusive else employmentStart.minusDays(1)
            excluded += DateRange(monthRange.start, end).dates()
        }
        return excluded.size
    }
}

/** Expected payment dates of absence payments. */
object AbsencePaymentDates {
    /** Vacation pay is expected [businessDaysBefore] working days before the vacation. */
    fun vacationPayDate(absence: Absence, holidays: HolidayCalendar, businessDaysBefore: Int): LocalDate =
        absence.paymentDate ?: holidays.minusBusinessDays(absence.startDate, businessDaysBefore)

    /** Employer's part of the sick benefit: the nearest payday after the sick leave is closed. */
    fun sickEmployerDate(absence: Absence, paydays: List<LocalDate>, holidays: HolidayCalendar): LocalDate =
        absence.employerPaymentDate
            ?: paydays.filter { it.isAfter(absence.endDate) }.minOrNull()
            ?: holidays.plusBusinessDays(absence.endDate, 1)

    /** Social fund part: within [businessDays] working days after the sick leave is closed. */
    fun sickFundDate(absence: Absence, holidays: HolidayCalendar, businessDays: Int): LocalDate =
        absence.paymentDate ?: holidays.plusBusinessDays(absence.endDate, businessDays)
}
