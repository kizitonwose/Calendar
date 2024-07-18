package com.kizitonwose.calendar.data

import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.WeekDayPosition
import com.kizitonwose.calendar.core.daysUntil
import com.kizitonwose.calendar.core.minusDays
import com.kizitonwose.calendar.core.plusDays
import com.kizitonwose.calendar.core.plusWeeks
import com.kizitonwose.calendar.core.weeksUntil
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

public data class WeekDateRange(
    val startDateAdjusted: LocalDate,
    val endDateAdjusted: LocalDate,
)

public fun getWeekCalendarAdjustedRange(
    startDate: LocalDate,
    endDate: LocalDate,
    firstDayOfWeek: DayOfWeek,
): WeekDateRange {
    val inDays = firstDayOfWeek.daysUntil(startDate.dayOfWeek)
    val startDateAdjusted = startDate.minusDays(inDays)
    val weeksBetween = startDateAdjusted.weeksUntil(endDate)
    val endDateAdjusted = startDateAdjusted.plusWeeks(weeksBetween).plusDays(6)
    return WeekDateRange(startDateAdjusted = startDateAdjusted, endDateAdjusted = endDateAdjusted)
}

public fun getWeekCalendarData(
    startDateAdjusted: LocalDate,
    offset: Int,
    desiredStartDate: LocalDate,
    desiredEndDate: LocalDate,
): WeekData {
    val firstDayInWeek = startDateAdjusted.plusWeeks(offset)
    return WeekData(firstDayInWeek, desiredStartDate, desiredEndDate)
}

public data class WeekData internal constructor(
    private val firstDayInWeek: LocalDate,
    private val desiredStartDate: LocalDate,
    private val desiredEndDate: LocalDate,
) {
    val week: Week = Week((0 until 7).map { dayOffset -> getDay(dayOffset) })

    private fun getDay(dayOffset: Int): WeekDay {
        val date = firstDayInWeek.plusDays(dayOffset)
        val position = when {
            date < desiredStartDate -> WeekDayPosition.InDate
            date > desiredEndDate -> WeekDayPosition.OutDate
            else -> WeekDayPosition.RangeDate
        }
        return WeekDay(date, position)
    }
}

public fun getWeekIndex(startDateAdjusted: LocalDate, date: LocalDate): Int {
    return startDateAdjusted.weeksUntil(date)
}

public fun getWeekIndicesCount(startDateAdjusted: LocalDate, endDateAdjusted: LocalDate): Int {
    // Add one to include the start week itself!
    return getWeekIndex(startDateAdjusted, endDateAdjusted) + 1
}
