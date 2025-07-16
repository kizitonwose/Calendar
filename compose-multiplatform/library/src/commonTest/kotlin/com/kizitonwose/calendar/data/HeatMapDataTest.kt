package com.kizitonwose.calendar.data

import com.kizitonwose.calendar.core.DayPosition
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

class HeatMapDataTest {
    private val october2022 = YearMonth(2022, Month.OCTOBER)
    private val november2022 = YearMonth(2022, Month.NOVEMBER)
    private val december2022 = YearMonth(2022, Month.DECEMBER)
    private val firstDayOfWeek = DayOfWeek.MONDAY

    /** October, November and December 2022
     *  with October as the start month and
     *  Monday as the first day of week.
     * ┌──┬─────────────────┬───────────┬───────────┐
     * │  │Oct 2022         │Nov 2022   │Dec 2022   │
     * ├──┼──┬──┬──┬──┬──┬──┼──┬──┬──┬──┼──┬──┬──┬──┤
     * │Mo│26│03│10│17│24│31│07│14│21│28│05│12│19│26│
     * ├──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┤
     * │Tu│27│04│11│18│25│01│08│15│22│29│06│13│20│27│
     * ├──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┤
     * │We│28│05│12│19│26│02│09│16│23│30│07│14│21│28│
     * ├──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┤
     * │Th│29│06│13│20│27│03│10│17│24│01│08│15│22│29│
     * ├──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┤
     * │Fr│30│07│14│21│28│04│11│18│24│02│09│16│23│30│
     * ├──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┤
     * │Sa│01│08│15│22│29│05│12│19│26│03│10│17│24│31│
     * ├──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┤
     * │Su│02│09│16│23│30│06│13│20│27│04│11│18│25│01│
     * └──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┘
     **/

    @Test
    @JsName("test1")
    fun `number of day positions are accurate`() {
        val monthData = getHeatMapCalendarMonthData(october2022, 0, firstDayOfWeek)
        val days = monthData.calendarMonth.weekDays.flatten()

        assertEquals(5, days.count { it.position == DayPosition.InDate })
        assertEquals(6, days.count { it.position == DayPosition.OutDate })
        assertEquals(31, days.count { it.position == DayPosition.MonthDate })
        assertEquals(42, days.count())
        assertEquals(6, monthData.calendarMonth.weekDays.count())
        monthData.calendarMonth.weekDays.forEach { weekDays ->
            assertEquals(7, weekDays.count())
        }
    }

    @Test
    @JsName("test2")
    fun `first date in the following month is accurate`() {
        val novemberMonthData = getHeatMapCalendarMonthData(october2022, 1, firstDayOfWeek)
        val days = novemberMonthData.calendarMonth.weekDays.flatten()

        assertEquals(7, days.first().date.dayOfMonth)
        assertEquals(october2022.nextMonth, days.first().date.yearMonth)
        assertEquals(DayPosition.MonthDate, days.first().position)
    }

    @Test
    @JsName("test3")
    fun `dates in the following month are in the correct positions`() {
        val novemberMonthData = getHeatMapCalendarMonthData(october2022, 1, firstDayOfWeek)
        val days = novemberMonthData.calendarMonth.weekDays.flatten()

        val monthDates = days.take(24)
        val outDates = days.takeLast(4)

        assertTrue(outDates.all { it.position == DayPosition.OutDate })
        assertTrue(monthDates.all { it.position == DayPosition.MonthDate })
        assertEquals(28, days.count())
    }

    @Test
    @JsName("test4")
    fun `dates in the following month have the correct month values`() {
        val november2022 = october2022.nextMonth
        val december2022 = november2022.nextMonth
        val novemberMonthData = getHeatMapCalendarMonthData(october2022, 1, firstDayOfWeek)
        val days = novemberMonthData.calendarMonth.weekDays.flatten()

        val monthDates = days.take(24)
        val outDates = days.takeLast(4)

        assertTrue(outDates.all { it.date.yearMonth == december2022 })
        assertTrue(monthDates.all { it.date.yearMonth == november2022 })
    }

    @Test
    @JsName("test5")
    fun `dates in the first month are in the correct positions`() {
        val monthData = getHeatMapCalendarMonthData(october2022, 0, firstDayOfWeek)
        val days = monthData.calendarMonth.weekDays.flatten()

        val inDates = days.take(5)
        val outDates = days.takeLast(6)
        val monthDates = days.drop(5).dropLast(6)

        assertTrue(inDates.all { it.position == DayPosition.InDate })
        assertTrue(outDates.all { it.position == DayPosition.OutDate })
        assertTrue(monthDates.all { it.position == DayPosition.MonthDate })
    }

    @Test
    @JsName("test6")
    fun `dates in the first month have the correct month values`() {
        val previousMonth = october2022.previousMonth
        val nextMonth = october2022.nextMonth
        val monthData = getHeatMapCalendarMonthData(october2022, 0, firstDayOfWeek)
        val days = monthData.calendarMonth.weekDays.flatten()

        val inDates = days.take(5)
        val outDates = days.takeLast(6)
        val monthDates = days.drop(5).dropLast(6)

        assertTrue(inDates.all { it.date.yearMonth == previousMonth })
        assertTrue(outDates.all { it.date.yearMonth == nextMonth })
        assertTrue(monthDates.all { it.date.yearMonth == october2022 })
    }

    @Test
    @JsName("test7")
    fun `days are in the appropriate week columns`() {
        val monthData = getHeatMapCalendarMonthData(october2022, 0, firstDayOfWeek)
        val daysOfWeek = daysOfWeek(firstDayOfWeek)

        monthData.calendarMonth.weekDays.forEach { week ->
            week.forEachIndexed { index, day ->
                assertEquals(daysOfWeek[index], day.date.dayOfWeek)
            }
        }
    }

    @Test
    @JsName("test8")
    fun `generated month is at the correct offset`() {
        val novemberMonthData = getHeatMapCalendarMonthData(october2022, 1, firstDayOfWeek)
        val decemberMonthData = getHeatMapCalendarMonthData(october2022, 2, firstDayOfWeek)

        assertEquals(november2022, novemberMonthData.calendarMonth.yearMonth)
        assertEquals(december2022, decemberMonthData.calendarMonth.yearMonth)
    }
}
