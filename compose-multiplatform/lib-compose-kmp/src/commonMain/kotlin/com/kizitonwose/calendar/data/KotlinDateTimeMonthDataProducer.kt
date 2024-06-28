package com.kizitonwose.calendar.data

import androidx.compose.runtime.saveable.Saver
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.YearMonth
import kotlinx.datetime.DayOfWeek

internal object KotlinDateTimeMonthDataProducer : MonthDataProducer<YearMonth, CalendarMonth> {
    override fun getMonthData(
        startMonth: YearMonth,
        offset: Int,
        firstDayOfWeek: DayOfWeek,
        outDateStyle: OutDateStyle,
    ): CalendarMonth {
        return getCalendarMonthData(
            startMonth = startMonth,
            offset = offset,
            firstDayOfWeek = firstDayOfWeek,
            outDateStyle = outDateStyle,
        ).calendarMonth
    }

    override fun getMonthIndex(startMonth: YearMonth, month: YearMonth): Int {
        return getCalendarMonthIndex(
            startMonth = startMonth,
            targetMonth = month,
        )
    }

    override fun CalendarMonth.yearMonth(): YearMonth = yearMonth

    override fun isInRange(startMonth: YearMonth, endMonth: YearMonth, month: YearMonth): Boolean =
        month in startMonth..endMonth

    override val saver: Saver<CalendarState<YearMonth, CalendarMonth>, Any> = getSaverInstance()
}
