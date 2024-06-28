package com.kizitonwose.calendar.data

import com.kizitonwose.calendar.core.CalendarDay3
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.YearMonth
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.lengthOfMonth
import com.kizitonwose.calendar.core.minusDays
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.plusDays
import com.kizitonwose.calendar.core.plusMonths
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.core.yearMonth
import kotlinx.datetime.DayOfWeek

data class MonthData internal constructor(
    private val month: YearMonth,
    private val inDays: Int,
    private val outDays: Int,
) {
    private val totalDays = inDays + month.lengthOfMonth() + outDays

    private val firstDay = month.atStartOfMonth().minusDays(inDays)

    private val rows = (0 until totalDays).chunked(7)

    private val previousMonth = month.previousMonth

    private val nextMonth = month.nextMonth

    val calendarMonth =
        CalendarMonth(month, rows.map { week -> week.map { dayOffset -> getDay(dayOffset) } })

    private fun getDay(dayOffset: Int): CalendarDay3 {
        val date = firstDay.plusDays(dayOffset)
        val position = when (date.yearMonth) {
            month -> DayPosition.MonthDate
            previousMonth -> DayPosition.InDate
            nextMonth -> DayPosition.OutDate
            else -> throw IllegalArgumentException("Invalid date: $date in month: $month")
        }
        return CalendarDay3(date, position)
    }
}

fun getCalendarMonthData(
    startMonth: YearMonth,
    offset: Int,
    firstDayOfWeek: DayOfWeek,
    outDateStyle: OutDateStyle,
): MonthData {
    val month = startMonth.plusMonths(offset)
    val firstDay = month.atStartOfMonth()
    val inDays = firstDayOfWeek.daysUntil(firstDay.dayOfWeek)
    val outDays = (inDays + month.lengthOfMonth()).let { inAndMonthDays ->
        val endOfRowDays = if (inAndMonthDays % 7 != 0) 7 - (inAndMonthDays % 7) else 0
        val endOfGridDays = if (outDateStyle == OutDateStyle.EndOfRow) {
            0
        } else {
            val weeksInMonth = (inAndMonthDays + endOfRowDays) / 7
            (6 - weeksInMonth) * 7
        }
        return@let endOfRowDays + endOfGridDays
    }
    return MonthData(month, inDays, outDays)
}

internal fun getHeatMapCalendarMonthData(
    startMonth: YearMonth,
    offset: Int,
    firstDayOfWeek: DayOfWeek,
): MonthData {
    val month = startMonth.plusMonths(offset)
    val firstDay = month.atStartOfMonth()
    val inDays = if (offset == 0) {
        firstDayOfWeek.daysUntil(firstDay.dayOfWeek)
    } else {
        -firstDay.dayOfWeek.daysUntil(firstDayOfWeek)
    }
    val outDays = (inDays + month.lengthOfMonth()).let { inAndMonthDays ->
        if (inAndMonthDays % 7 != 0) 7 - (inAndMonthDays % 7) else 0
    }
    return MonthData(month, inDays, outDays)
}

fun getCalendarMonthIndex(startMonth: YearMonth, targetMonth: YearMonth): Int {
    return startMonth.monthsUntil(targetMonth)
}

internal fun getMonthIndicesCount(startMonth: YearMonth, endMonth: YearMonth): Int {
    // Add one to include the start month itself!
    return getCalendarMonthIndex(startMonth, endMonth) + 1
}
