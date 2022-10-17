package com.kizitonwose.calendar.data

import com.kizitonwose.calendar.core.*
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields

data class MonthData internal constructor(
    private val month: YearMonth,
    private val inDays: Int,
    private val outDays: Int,
) {

    private val totalDays = inDays + month.lengthOfMonth() + outDays

    private val firstDay = month.atStartOfMonth().minusDays(inDays.toLong())

    private val rows = (0 until totalDays).chunked(7)

    private val previousMonth = month.previousMonth

    private val nextMonth = month.nextMonth

    val calendarMonth =
        CalendarMonth(month, rows.map { week -> week.map { dayOffset -> getDay(dayOffset) } })

    private fun getDay(dayOffset: Int): CalendarDay {
        val date = firstDay.plusDays(dayOffset.toLong())
        val position = when (date.yearMonth) {
            month -> DayPosition.MonthDate
            previousMonth -> DayPosition.InDate
            nextMonth -> DayPosition.OutDate
            else -> throw IllegalArgumentException("Invalid date: $date in month: $month")
        }
        return CalendarDay(date, position)
    }
}

fun getCalendarMonthData(
    startMonth: YearMonth,
    offset: Int,
    firstDayOfWeek: DayOfWeek,
    outDateStyle: OutDateStyle,
): MonthData {
    val month = startMonth.plusMonths(offset.toLong())
    val firstDay = month.atStartOfMonth()
    val inDays = firstDayOfWeek.daysUntil(firstDay.dayOfWeek)
    val outDays = (inDays + month.lengthOfMonth()).let { totalDays ->
        val endOfRow = if (totalDays % 7 != 0) 7 - (totalDays % 7) else 0
        val endOfGrid = if (outDateStyle == OutDateStyle.EndOfRow) 0 else run {
            val weekOfMonthField = WeekFields.of(firstDayOfWeek, 1).weekOfMonth()
            val weeksInMonth = month.atEndOfMonth().get(weekOfMonthField)
            return@run (6 - weeksInMonth) * 7
        }
        return@let endOfRow + endOfGrid
    }
    return MonthData(month, inDays, outDays)
}

fun getHeatMapCalendarMonthData(
    startMonth: YearMonth,
    offset: Int,
    firstDayOfWeek: DayOfWeek,
): MonthData {
    val month = startMonth.plusMonths(offset.toLong())
    val firstDay = month.atStartOfMonth()
    val inDays = if (offset == 0) {
        firstDayOfWeek.daysUntil(firstDay.dayOfWeek)
    } else {
        -firstDay.dayOfWeek.daysUntil(firstDayOfWeek)
    }
    val outDays = (inDays + month.lengthOfMonth()).let { totalDays ->
        if (totalDays % 7 != 0) 7 - (totalDays % 7) else 0
    }
    return MonthData(month, inDays, outDays)
}

fun getMonthIndex(startMonth: YearMonth, targetMonth: YearMonth): Int {
    return ChronoUnit.MONTHS.between(startMonth, targetMonth).toInt()
}

fun getMonthIndicesCount(startMonth: YearMonth, endMonth: YearMonth): Int {
    // Add one to include the start month itself!
    return getMonthIndex(startMonth, endMonth) + 1
}
