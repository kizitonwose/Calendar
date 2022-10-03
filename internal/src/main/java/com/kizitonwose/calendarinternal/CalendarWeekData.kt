package com.kizitonwose.calendarinternal

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class WeekDateRange(
    val startDateAdjusted: LocalDate,
    val endDateAdjusted: LocalDate,
)

fun getWeekCalendarAdjustedRange(
    startDate: LocalDate,
    endDate: LocalDate,
    firstDayOfWeek: DayOfWeek,
): WeekDateRange {
    val inDays = firstDayOfWeek.daysUntil(startDate.dayOfWeek)
    val startDateAdjusted = startDate.minusDays(inDays.toLong())
    val weeksBetween =
        ChronoUnit.WEEKS.between(startDateAdjusted.atStartOfDay(), endDate.atStartOfDay()).toInt()
    val endDateAdjusted = startDateAdjusted.plusWeeks(weeksBetween.toLong()).plusDays(6)
    return WeekDateRange(startDateAdjusted = startDateAdjusted, endDateAdjusted = endDateAdjusted)
}

fun getWeekCalendarData(startDate: LocalDate, offset: Int): WeekData {
    val firstDayInWeek = startDate.plusWeeks(offset.toLong())
    return WeekData(firstDayInWeek)
}

data class WeekData(val firstDayInWeek: LocalDate) {
    val days = (0 until 7).map { firstDayInWeek.plusDays(it.toLong()) }
}

fun getWeekIndicesCount(startDate: LocalDate, endDate: LocalDate): Int {
    // Add one to include the start week itself!
    return ChronoUnit.WEEKS.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toInt() + 1
}
