package com.kizitonwose.calendarcore

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

val LocalDate.yearMonth: YearMonth
    get() = YearMonth.of(year, month)

val YearMonth.next: YearMonth
    get() = this.plusMonths(1)

val YearMonth.previous: YearMonth
    get() = this.minusMonths(1)

fun YearMonth.atStartOfMonth(): LocalDate = this.atDay(1)

fun daysOfWeek(firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale()): List<DayOfWeek> {
    val pivot = 7 - firstDayOfWeek.ordinal
    val daysOfWeek = DayOfWeek.values()
    // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
    return (daysOfWeek.takeLast(pivot) + daysOfWeek.dropLast(pivot))
}

private fun firstDayOfWeekFromLocale() = WeekFields.of(Locale.getDefault()).firstDayOfWeek