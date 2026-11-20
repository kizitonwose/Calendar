package io.github.ceniorpomidor.workcalendar.domain.time

import io.github.ceniorpomidor.workcalendar.domain.model.HolidayOverride
import io.github.ceniorpomidor.workcalendar.domain.model.HolidayOverrideType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.MonthDay

/**
 * Public holidays and working days.
 *
 * Built-in data: non-working public holidays of the Russian Federation (art. 112 of the
 * Labour Code), the automatic transfer of a holiday that falls on a weekend to the next
 * working day (except the January holidays), and the transfers set by the government for
 * 2025–2027 (the production calendar up to the beginning of 2028). Anything else can be added
 * by the user as [HolidayOverride]s.
 */
class HolidayCalendar(
    overrides: List<HolidayOverride> = emptyList(),
    private val useBuiltIn: Boolean = true,
) {
    private val overrides: Map<LocalDate, HolidayOverride> = overrides.associateBy { it.date }
    private val transferCache = HashMap<Int, Set<LocalDate>>()

    /** Public holiday that is paid at the holiday rate. */
    fun isPublicHoliday(date: LocalDate): Boolean {
        val override = overrides[date]
        if (override?.type == HolidayOverrideType.HOLIDAY) return true
        return useBuiltIn && FIXED_HOLIDAYS.containsKey(MonthDay.from(date))
    }

    fun holidayName(date: LocalDate): String? {
        val override = overrides[date]
        if (override != null && override.type != HolidayOverrideType.WORKDAY && override.title.isNotBlank()) return override.title
        if (useBuiltIn) {
            FIXED_HOLIDAYS[MonthDay.from(date)]?.let { return it }
            if (date in transfersFor(date.year)) return "Выходной (перенос)"
        }
        return when (override?.type) {
            HolidayOverrideType.HOLIDAY -> "Праздник"
            HolidayOverrideType.DAY_OFF -> "Выходной"
            else -> null
        }
    }

    /** A day that is not a working day in a standard five-day week. */
    fun isNonWorkingDay(date: LocalDate): Boolean {
        when (overrides[date]?.type) {
            HolidayOverrideType.WORKDAY -> return false
            HolidayOverrideType.DAY_OFF, HolidayOverrideType.HOLIDAY -> return true
            null -> Unit
        }
        if (useBuiltIn) {
            if (date in BUILT_IN_WORKDAYS) return false
            if (isPublicHoliday(date)) return true
            if (date in transfersFor(date.year)) return true
        }
        return date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
    }

    fun isBusinessDay(date: LocalDate): Boolean = !isNonWorkingDay(date)

    /** A Saturday or Sunday made a working day by a decree or by the user. */
    fun isWorkingWeekend(date: LocalDate): Boolean {
        val weekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
        return weekend && isBusinessDay(date)
    }

    /** Day off that should be highlighted in the calendar (holiday or transferred day off, not a plain weekend). */
    fun isSpecialDayOff(date: LocalDate): Boolean {
        val weekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
        return isNonWorkingDay(date) && (!weekend || isPublicHoliday(date))
    }

    fun minusBusinessDays(date: LocalDate, days: Int): LocalDate {
        var d = date
        var left = days
        while (left > 0) {
            d = d.minusDays(1)
            if (isBusinessDay(d)) left--
        }
        return d
    }

    fun plusBusinessDays(date: LocalDate, days: Int): LocalDate {
        var d = date
        var left = days
        while (left > 0) {
            d = d.plusDays(1)
            if (isBusinessDay(d)) left--
        }
        return d
    }

    fun businessDayOnOrBefore(date: LocalDate): LocalDate {
        var d = date
        var guard = 0
        while (!isBusinessDay(d) && guard++ < 30) d = d.minusDays(1)
        return d
    }

    fun businessDayOnOrAfter(date: LocalDate): LocalDate {
        var d = date
        var guard = 0
        while (!isBusinessDay(d) && guard++ < 30) d = d.plusDays(1)
        return d
    }

    fun publicHolidaysIn(range: DateRange): List<LocalDate> = range.filter { isPublicHoliday(it) }

    private fun transfersFor(year: Int): Set<LocalDate> = transferCache.getOrPut(year) { computeTransfers(year) }

    /** Days off moved from weekends: decree transfers plus the automatic art. 112 rule. */
    private fun computeTransfers(year: Int): Set<LocalDate> {
        val result = HashSet<LocalDate>()
        BUILT_IN_TRANSFERS[year]?.let { result.addAll(it) }
        val hasDecree = BUILT_IN_TRANSFERS.containsKey(year)
        // Holidays (except January 1–8) that fall on a weekend move to the next working day.
        for ((monthDay, _) in FIXED_HOLIDAYS) {
            if (monthDay.monthValue == 1 && monthDay.dayOfMonth <= 8) continue
            val holiday = monthDay.atYear(year)
            if (holiday.dayOfWeek != DayOfWeek.SATURDAY && holiday.dayOfWeek != DayOfWeek.SUNDAY) continue
            if (hasDecree && year in DECREE_COVERS_WEEKEND_RULE) continue
            var candidate = holiday.plusDays(1)
            while (
                candidate.dayOfWeek == DayOfWeek.SATURDAY ||
                candidate.dayOfWeek == DayOfWeek.SUNDAY ||
                FIXED_HOLIDAYS.containsKey(MonthDay.from(candidate)) ||
                candidate in result
            ) {
                candidate = candidate.plusDays(1)
            }
            result += candidate
        }
        return result
    }

    companion object {
        val FIXED_HOLIDAYS: Map<MonthDay, String> = linkedMapOf(
            MonthDay.of(1, 1) to "Новогодние каникулы",
            MonthDay.of(1, 2) to "Новогодние каникулы",
            MonthDay.of(1, 3) to "Новогодние каникулы",
            MonthDay.of(1, 4) to "Новогодние каникулы",
            MonthDay.of(1, 5) to "Новогодние каникулы",
            MonthDay.of(1, 6) to "Новогодние каникулы",
            MonthDay.of(1, 7) to "Рождество Христово",
            MonthDay.of(1, 8) to "Новогодние каникулы",
            MonthDay.of(2, 23) to "День защитника Отечества",
            MonthDay.of(3, 8) to "Международный женский день",
            MonthDay.of(5, 1) to "Праздник Весны и Труда",
            MonthDay.of(5, 9) to "День Победы",
            MonthDay.of(6, 12) to "День России",
            MonthDay.of(11, 4) to "День народного единства",
        )

        /**
         * Transfers set by government decrees. The decrees list all transfers of the year
         * explicitly (including the weekend rule), so for these years only the list is used.
         */
        private val BUILT_IN_TRANSFERS: Map<Int, Set<LocalDate>> = mapOf(
            2025 to setOf(
                LocalDate.of(2025, 5, 2),
                LocalDate.of(2025, 5, 8),
                LocalDate.of(2025, 6, 13),
                LocalDate.of(2025, 11, 3),
                LocalDate.of(2025, 12, 31),
            ),
            2026 to setOf(
                LocalDate.of(2026, 1, 9),
                LocalDate.of(2026, 3, 9),
                LocalDate.of(2026, 5, 11),
                LocalDate.of(2026, 12, 31),
            ),
            // Decree No. 1187 of 17.09.2026: 2 January -> 5 November, 3 January -> 31 December,
            // 20 February (Saturday) -> 22 February; holidays on weekends: 1 May -> 3 May,
            // 9 May -> 10 May, 12 June -> 14 June.
            2027 to setOf(
                LocalDate.of(2027, 2, 22),
                LocalDate.of(2027, 5, 3),
                LocalDate.of(2027, 5, 10),
                LocalDate.of(2027, 6, 14),
                LocalDate.of(2027, 11, 5),
                LocalDate.of(2027, 12, 31),
            ),
        )

        private val DECREE_COVERS_WEEKEND_RULE: Set<Int> = setOf(2025, 2026, 2027)

        /** Last year covered by the built-in decrees. */
        const val LAST_DECREE_YEAR = 2027

        /** Working Saturdays set by decrees. */
        private val BUILT_IN_WORKDAYS: Set<LocalDate> = setOf(
            LocalDate.of(2025, 11, 1),
            LocalDate.of(2027, 2, 20),
        )
    }
}
