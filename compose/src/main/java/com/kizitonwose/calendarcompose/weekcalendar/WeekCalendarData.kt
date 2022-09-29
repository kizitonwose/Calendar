package com.kizitonwose.calendarcompose.weekcalendar

import com.kizitonwose.calendarcompose.shared.daysUntil
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal data class WeekIndexData(
    val startDateAdjusted: LocalDate,
    val endDateAdjusted: LocalDate,
    val weekCount: Int,
)

internal fun getWeekCalendarIndexData(
    startDate: LocalDate,
    endDate: LocalDate,
    firstDayOfWeek: DayOfWeek,
): WeekIndexData {
    val inDays = firstDayOfWeek.daysUntil(startDate.dayOfWeek)
    val startDateAdjusted = startDate.minusDays(inDays.toLong())
    val weeksBetween =
        ChronoUnit.WEEKS.between(startDateAdjusted.atStartOfDay(), endDate.atStartOfDay()).toInt()
    val endDateAdjusted = startDateAdjusted.plusWeeks(weeksBetween.toLong()).plusDays(6)
    return WeekIndexData(
        startDateAdjusted = startDateAdjusted,
        endDateAdjusted = endDateAdjusted,
        weekCount = weeksBetween + 1 // Add one to include the start week itself!
    )
}

internal fun getWeekCalendarData(startDate: LocalDate, offset: Int): WeekData {
    val firstDayInWeek = startDate.plusWeeks(offset.toLong())
    return WeekData(firstDayInWeek)
}

internal data class WeekData(val firstDayInWeek: LocalDate) {
    val days = (0 until 7).map { firstDayInWeek.plusDays(it.toLong()) }
}
