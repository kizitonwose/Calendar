package com.kizitonwose.calendar.data

import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.Year
import com.kizitonwose.calendar.utils.weeksInMonth
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertEquals

class YearDataTest {
    @Test
    @JsName("test1")
    fun `year data is accurate with non-leap year`() {
        val year = Year(2019)
        val firstDayOfWeek = DayOfWeek.MONDAY
        val outDateStyle = OutDateStyle.EndOfRow
        val yearData = getCalendarYearData(year, 0, firstDayOfWeek, outDateStyle)
        val months = yearData.months
        val days = yearData.months.flatMap { it.weekDays }.flatten()
        yearData.months.forEachIndexed { index, month ->
            val monthData = getCalendarMonthData(
                startMonth = YearMonth(year.value, Month.JANUARY),
                offset = month.yearMonth.month.ordinal,
                firstDayOfWeek = firstDayOfWeek,
                outDateStyle = outDateStyle,
            )
            assertEquals(monthData.calendarMonth, month)
            assertEquals(Month.entries[Month.JANUARY.ordinal + index], month.yearMonth.month)
            assertEquals(month.yearMonth.weeksInMonth(firstDayOfWeek), month.weekDays.count())
        }
        assertEquals(12, months.count())
        assertEquals(year, yearData.year)
        assertEquals(36, days.count { it.position == DayPosition.InDate })
        assertEquals(33, days.count { it.position == DayPosition.OutDate })
        assertEquals(365, days.count { it.position == DayPosition.MonthDate })
    }

    @Test
    @JsName("test2")
    fun `year data is accurate with leap year`() {
        val year = Year(2020)
        val firstDayOfWeek = DayOfWeek.SUNDAY
        val outDateStyle = OutDateStyle.EndOfGrid
        val yearData = getCalendarYearData(year, 0, firstDayOfWeek, outDateStyle)
        val months = yearData.months
        val days = yearData.months.flatMap { it.weekDays }.flatten()
        yearData.months.forEachIndexed { index, month ->
            val monthData = getCalendarMonthData(
                startMonth = YearMonth(year.value, Month.JANUARY),
                offset = month.yearMonth.month.ordinal,
                firstDayOfWeek = firstDayOfWeek,
                outDateStyle = outDateStyle,
            )
            assertEquals(monthData.calendarMonth, month)
            assertEquals(Month.entries[Month.JANUARY.ordinal + index], month.yearMonth.month)
            assertEquals(6, month.weekDays.count())
            val weeksWithoutGridOutDates = month.weekDays.filterNot { week -> week.all { it.position == DayPosition.OutDate } }
            assertEquals(month.yearMonth.weeksInMonth(firstDayOfWeek), weeksWithoutGridOutDates.count())
        }
        assertEquals(12, months.count())
        assertEquals(year, yearData.year)
        assertEquals(35, days.count { it.position == DayPosition.InDate })
        assertEquals(103, days.count { it.position == DayPosition.OutDate })
        assertEquals(366, days.count { it.position == DayPosition.MonthDate })
    }

    @Test
    @JsName("test3")
    fun `generated year is at the correct offset`() {
        val yearData = getCalendarYearData(Year(2020), 6, DayOfWeek.SUNDAY, OutDateStyle.EndOfGrid)
        val yearData2 = getCalendarYearData(Year(2021), 0, DayOfWeek.SUNDAY, OutDateStyle.EndOfRow)

        assertEquals(yearData.year, Year(2026))
        assertEquals(yearData2.year, Year(2021))
    }

    @Test
    @JsName("test4")
    fun `year index calculation works as expected`() {
        val index = getYearIndex(startYear = Year(2020), targetYear = Year(2030))
        val index2 = getYearIndex(startYear = Year(2052), targetYear = Year(2052))

        assertEquals(10, index)
        assertEquals(0, index2)
    }

    @Test
    @JsName("test5")
    fun `year indices count calculation works as expected`() {
        val count = getYearIndicesCount(startYear = Year(2020), endYear = Year(2040))
        val count2 = getYearIndicesCount(startYear = Year(2052), endYear = Year(2052))

        assertEquals(21, count)
        assertEquals(1, count2)
    }
}
