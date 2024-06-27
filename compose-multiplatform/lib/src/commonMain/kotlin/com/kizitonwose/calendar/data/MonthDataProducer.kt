package com.kizitonwose.calendar.data

import androidx.compose.runtime.saveable.Saver
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.YearMonth
import kotlinx.datetime.DayOfWeek

internal interface MonthDataProducer<YearMonth, CalendarMonth> where YearMonth : Comparable<YearMonth> {
    fun getCalendarMonthData(
        startMonth: YearMonth,
        offset: Int,
        firstDayOfWeek: DayOfWeek,
        outDateStyle: OutDateStyle,
    ): CalendarMonth

    fun getMonthIndex(startMonth: YearMonth, month: YearMonth): Int

    fun getMonthIndicesCount(startMonth: YearMonth, endMonth: YearMonth): Int {
        // Add one to include the start month itself!
        return getMonthIndex(startMonth, endMonth) + 1
    }

    val saver: Saver<CalendarState<YearMonth, CalendarMonth>, Any>
}

