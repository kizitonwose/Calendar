package com.kizitonwose.calendar.data

import androidx.compose.runtime.saveable.Saver
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth3
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.toJavaYearMonth
import kotlinx.datetime.toJavaLocalDate
import java.time.DayOfWeek
import java.time.YearMonth

internal object JdkMonthDataProducer : MonthDataProducer<YearMonth, CalendarMonth3> {
    override fun getMonthData(
        startMonth: YearMonth,
        offset: Int,
        firstDayOfWeek: DayOfWeek,
        outDateStyle: OutDateStyle,
    ): CalendarMonth3 {
        val calendarMonth = getCalendarMonthData(
            startMonth = startMonth.toKotlinYearMonth(),
            offset = offset,
            firstDayOfWeek = firstDayOfWeek,
            outDateStyle = outDateStyle,
        ).calendarMonth
        return CalendarMonth3(calendarMonth.yearMonth.toJavaYearMonth(), weekDays = calendarMonth.weekDays.map { it.map { CalendarDay(it.date.toJavaLocalDate(), it.position) } })
    }

    override fun getMonthIndex(startMonth: YearMonth, month: YearMonth): Int {
        return getCalendarMonthIndex(
            startMonth = startMonth.toKotlinYearMonth(),
            targetMonth = month.toKotlinYearMonth(),
        )
    }

    override fun CalendarMonth3.yearMonth(): YearMonth = yearMonth

    override fun isInRange(startMonth: YearMonth, endMonth: YearMonth, month: YearMonth): Boolean =
        month in startMonth..endMonth

    override val saver: Saver<CalendarState<YearMonth, CalendarMonth3>, Any> = getSaverInstance()
}

private fun YearMonth.toKotlinYearMonth() =
    com.kizitonwose.calendar.core.YearMonth(year, month)
