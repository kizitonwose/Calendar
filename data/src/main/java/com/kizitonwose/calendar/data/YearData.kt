package com.kizitonwose.calendar.data

import com.kizitonwose.calendar.core.CalendarYear
import com.kizitonwose.calendar.core.OutDateStyle
import java.time.DayOfWeek
import java.time.Month
import java.time.Year
import java.time.temporal.ChronoUnit

public fun getCalendarYearData(
    startYear: Year,
    offset: Int,
    firstDayOfWeek: DayOfWeek,
    outDateStyle: OutDateStyle,
): CalendarYear {
    val year = startYear.plusYears(offset.toLong())
    val months = List(Month.entries.size) { index ->
        getCalendarMonthData(
            startMonth = year.atMonth(Month.JANUARY),
            offset = index,
            firstDayOfWeek = firstDayOfWeek,
            outDateStyle = outDateStyle,
        ).calendarMonth
    }
    return CalendarYear(year, months)
}

public fun getYearIndex(startYear: Year, targetYear: Year): Int {
    return ChronoUnit.YEARS.between(startYear, targetYear).toInt()
}

public fun getYearIndicesCount(startYear: Year, endYear: Year): Int {
    // Add one to include the start year itself!
    return getYearIndex(startYear, endYear) + 1
}
