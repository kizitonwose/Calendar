package com.kizitonwose.calendar.core

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Returns the days of week values such that the desired
 * [firstDayOfWeek] property is at the start position.
 *
 * @see [firstDayOfWeekFromLocale]
 */
@JvmOverloads
public fun daysOfWeek(firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale()): List<DayOfWeek> {
    val pivot = 7 - firstDayOfWeek.ordinal
    val daysOfWeek = DayOfWeek.entries
    // Order `daysOfWeek` array so that firstDayOfWeek is at the start position.
    return daysOfWeek.takeLast(pivot) + daysOfWeek.dropLast(pivot)
}

/**
 * Returns the first day of the week from the provided locale.
 */
@JvmOverloads
public fun firstDayOfWeekFromLocale(locale: Locale = Locale.getDefault()): DayOfWeek = WeekFields.of(locale).firstDayOfWeek

/**
 * Returns a [LocalDate] at the start of the month.
 *
 * Complements [YearMonth.atEndOfMonth].
 */
public fun YearMonth.atStartOfMonth(): LocalDate = this.atDay(1)

public val LocalDate.yearMonth: YearMonth
    get() = YearMonth.of(year, month)

public val YearMonth.nextMonth: YearMonth
    get() = this.plusMonths(1)

public val YearMonth.previousMonth: YearMonth
    get() = this.minusMonths(1)
