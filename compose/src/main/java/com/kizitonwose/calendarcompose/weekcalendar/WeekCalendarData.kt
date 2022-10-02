package com.kizitonwose.calendarcompose.weekcalendar

import com.kizitonwose.calendarcompose.shared.daysUntil
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal data class WeekDateRange(
    val startDateAdjusted: LocalDate,
    val endDateAdjusted: LocalDate,
//    val weekCount: Int,
)

internal fun getWeekCalendarAdjustedRange(
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

internal fun getWeekCalendarData(startDate: LocalDate, offset: Int): WeekData {
    val firstDayInWeek = startDate.plusWeeks(offset.toLong())
    return WeekData(firstDayInWeek)
}

internal data class WeekData(val firstDayInWeek: LocalDate) {
    val days = (0 until 7).map { firstDayInWeek.plusDays(it.toLong()) }
}

internal fun getWeekIndicesCount(startDate: LocalDate, endDate: LocalDate): Int {
    // Add one to include the start week itself!
    return ChronoUnit.WEEKS.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toInt() + 1
}
