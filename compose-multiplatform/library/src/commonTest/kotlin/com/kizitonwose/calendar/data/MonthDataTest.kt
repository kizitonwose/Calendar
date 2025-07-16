package com.kizitonwose.calendar.data

import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.utils.nextMonth
import com.kizitonwose.calendar.utils.previousMonth
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MonthDataTest {
    private val may2019 = YearMonth(2019, Month.MAY)
    private val november2019 = YearMonth(2019, Month.NOVEMBER)
    private val firstDayOfWeek = DayOfWeek.MONDAY

    /** May and November 2019 with Monday as the first day of week.
     * ┌────────────────────┐ ┌────────────────────┐
     * │      May 2019      │ │   November 2019    │
     * ├──┬──┬──┬──┬──┬──┬──┤ ├──┬──┬──┬──┬──┬──┬──┤
     * │Mo│Tu│We│Th│Fr│Sa│Su│ │Mo│Tu│We│Th│Fr│Sa│Su│
     * ├──┼──┼──┼──┼──┼──┼──┤ ├──┼──┼──┼──┼──┼──┼──┤
     * │29│30│01│02│03│04│05│ │28│29│30│31│01│02│03│
     * ├──┼──┼──┼──┼──┼──┼──┤ ├──┼──┼──┼──┼──┼──┼──┤
     * │06│07│08│09│10│11│12│ │04│05│06│07│08│09│10│
     * ├──┼──┼──┼──┼──┼──┼──┤ ├──┼──┼──┼──┼──┼──┼──┤
     * │13│14│15│16│17│18│19│ │11│12│13│14│15│16│17│
     * ├──┼──┼──┼──┼──┼──┼──┤ ├──┼──┼──┼──┼──┼──┼──┤
     * │20│21│22│23│24│25│26│ │18│19│20│21│22│23│24│
     * ├──┼──┼──┼──┼──┼──┼──┤ ├──┼──┼──┼──┼──┼──┼──┤
     * │27│28│29│30│31│01│02│ │25│26│27│28│29│30│01│
     * └──┴──┴──┴──┴──┴──┴──┘ └──┴──┴──┴──┴──┴──┴──┘
     **/

    @Test
    @JsName("test1")
    fun `number of day positions are accurate with EndOfRow OutDateStyle`() {
        val monthData = getCalendarMonthData(may2019, 0, firstDayOfWeek, OutDateStyle.EndOfRow)
        val days = monthData.calendarMonth.weekDays.flatten()
        assertEquals(2, days.count { it.position == DayPosition.InDate })
        assertEquals(2, days.count { it.position == DayPosition.OutDate })
        assertEquals(31, days.count { it.position == DayPosition.MonthDate })
        assertEquals(35, days.count())
        assertEquals(5, monthData.calendarMonth.weekDays.count())
        monthData.calendarMonth.weekDays.forEach { weekDays ->
            assertEquals(7, weekDays.count())
        }
    }

    @Test
    @JsName("test2")
    fun `number of day positions are accurate with EndOfGrid OutDateStyle`() {
        val monthData = getCalendarMonthData(may2019, 0, firstDayOfWeek, OutDateStyle.EndOfGrid)
        val days = monthData.calendarMonth.weekDays.flatten()
        assertEquals(2, days.count { it.position == DayPosition.InDate })
        assertEquals(9, days.count { it.position == DayPosition.OutDate })
        assertEquals(31, days.count { it.position == DayPosition.MonthDate })
        assertEquals(42, days.count())
        assertEquals(6, monthData.calendarMonth.weekDays.count())
        monthData.calendarMonth.weekDays.forEach { weekDays ->
            assertEquals(7, weekDays.count())
        }
    }

    @Test
    @JsName("test3")
    fun `dates are in the correct positions with EndOfRow OutDateStyle`() {
        val monthData = getCalendarMonthData(may2019, 0, firstDayOfWeek, OutDateStyle.EndOfRow)
        val days = monthData.calendarMonth.weekDays.flatten()

        val inDates = days.take(2)
        val outDates = days.takeLast(2)
        val monthDates = days.drop(2).dropLast(2)

        assertTrue(inDates.all { it.position == DayPosition.InDate })
        assertTrue(outDates.all { it.position == DayPosition.OutDate })
        assertTrue(monthDates.all { it.position == DayPosition.MonthDate })
    }

    @Test
    @JsName("test14")
    fun `dates are in the correct positions with EndOfGrid OutDateStyle`() {
        val monthData = getCalendarMonthData(may2019, 0, firstDayOfWeek, OutDateStyle.EndOfGrid)
        val days = monthData.calendarMonth.weekDays.flatten()

        val inDates = days.take(2)
        val outDates = days.takeLast(2)
        val monthDates = days.drop(2).dropLast(9)

        assertTrue(inDates.all { it.position == DayPosition.InDate })
        assertTrue(outDates.all { it.position == DayPosition.OutDate })
        assertTrue(monthDates.all { it.position == DayPosition.MonthDate })
    }

    @Test
    @JsName("test5")
    fun `dates have the correct month values`() {
        val previousMonth = may2019.previousMonth
        val nextMonth = may2019.nextMonth
        val monthData = getCalendarMonthData(may2019, 0, firstDayOfWeek, OutDateStyle.EndOfRow)
        val days = monthData.calendarMonth.weekDays.flatten()

        val inDates = days.take(2)
        val outDates = days.takeLast(2)
        val monthDates = days.drop(2).dropLast(2)

        assertTrue(inDates.all { it.date.yearMonth == previousMonth })
        assertTrue(outDates.all { it.date.yearMonth == nextMonth })
        assertTrue(monthDates.all { it.date.yearMonth == may2019 })
    }

    @Test
    @JsName("test6")
    fun `end of row out date style does not add a new row`() {
        val endOfRowMonthData =
            getCalendarMonthData(may2019, 0, firstDayOfWeek, OutDateStyle.EndOfRow)

        assertEquals(5, endOfRowMonthData.calendarMonth.weekDays.count())
    }

    @Test
    @JsName("test7")
    fun `end of grid out date style adds a new row`() {
        val endOfGridMonthData =
            getCalendarMonthData(may2019, 0, firstDayOfWeek, OutDateStyle.EndOfGrid)

        assertEquals(endOfGridMonthData.calendarMonth.weekDays.count(), 6)
        endOfGridMonthData.calendarMonth.weekDays.last().forEach { day ->
            assertEquals(DayPosition.OutDate, day.position)
            assertEquals(may2019.nextMonth, day.date.yearMonth)
        }
    }

    @Test
    @JsName("test8")
    fun `days are in the appropriate week columns`() {
        val monthData = getCalendarMonthData(may2019, 0, firstDayOfWeek, OutDateStyle.EndOfRow)
        val daysOfWeek = daysOfWeek(firstDayOfWeek)

        monthData.calendarMonth.weekDays.forEach { week ->
            week.forEachIndexed { index, day ->
                assertEquals(daysOfWeek[index], day.date.dayOfWeek)
            }
        }
    }

    @Test
    @JsName("test9")
    fun `generated month is at the correct offset`() {
        val monthData = getCalendarMonthData(may2019, 6, firstDayOfWeek, OutDateStyle.EndOfRow)

        assertEquals(november2019, monthData.calendarMonth.yearMonth)
    }

    @Test
    @JsName("test10")
    fun `month index calculation works as expected`() {
        val index = getMonthIndex(startMonth = may2019, targetMonth = november2019)

        assertEquals(6, index)
    }

    @Test
    @JsName("test11")
    fun `month indices count calculation works as expected`() {
        val count = getMonthIndicesCount(startMonth = may2019, endMonth = november2019)

        assertEquals(7, count)
    }
}
