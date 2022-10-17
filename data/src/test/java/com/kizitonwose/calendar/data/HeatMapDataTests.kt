package com.kizitonwose.calendar.data

import com.kizitonwose.calendar.core.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Month.*
import java.time.YearMonth

class HeatMapDataTests {

    private val october2022 = YearMonth.of(2022, OCTOBER)
    private val november2022 = YearMonth.of(2022, NOVEMBER)
    private val december2022 = YearMonth.of(2022, DECEMBER)
    private val firstDayOfWeek = DayOfWeek.MONDAY

    /** October, November and December 2022
     *  with October as the start month and
     *  Monday as the first day of week.
    ┌──┬─────────────────┬───────────┬───────────┐
    │  │Oct 2022         │Nov 2022   │Dec 2022   │
    ├──┼──┬──┬──┬──┬──┬──┼──┬──┬──┬──┼──┬──┬──┬──┤
    │Mo│26│03│10│17│24│31│07│14│21│28│05│12│19│26│
    ├──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┤
    │Tu│27│04│11│18│25│01│08│15│22│29│06│13│20│27│
    ├──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┤
    │We│28│05│12│19│26│02│09│16│23│30│07│14│21│28│
    ├──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┤
    │Th│29│06│13│20│27│03│10│17│24│01│08│15│22│29│
    ├──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┤
    │Fr│30│07│14│21│28│04│11│18│24│02│09│16│23│30│
    ├──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┤
    │Sa│01│08│15│22│29│05│12│19│26│03│10│17│24│31│
    ├──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┤
    │Su│02│09│16│23│30│06│13│20│27│04│11│18│25│01│
    └──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┘
     **/

    @Test
    fun `number of day positions are accurate`() {
        val monthData = getHeatMapCalendarMonthData(october2022, 0, firstDayOfWeek)
        val weekDays = monthData.calendarMonth.weekDays.flatten()

        assertEquals(5, weekDays.count { it.position == DayPosition.InDate })
        assertEquals(6, weekDays.count { it.position == DayPosition.OutDate })
        assertEquals(31, weekDays.count { it.position == DayPosition.MonthDate })
        assertEquals(42, weekDays.count())
    }

    @Test
    fun `first date in the following month is accurate`() {
        val novemberMonthData = getHeatMapCalendarMonthData(october2022, 1, firstDayOfWeek)
        val weekDays = novemberMonthData.calendarMonth.weekDays.flatten()

        assertEquals(7, weekDays.first().date.dayOfMonth)
        assertEquals(october2022.nextMonth, weekDays.first().date.yearMonth)
        assertEquals(DayPosition.MonthDate, weekDays.first().position)
    }

    @Test
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
    fun `generated month is at the correct offset`() {
        val novemberMonthData = getHeatMapCalendarMonthData(october2022, 1, firstDayOfWeek)
        val decemberMonthData = getHeatMapCalendarMonthData(october2022, 2, firstDayOfWeek)

        assertEquals(november2022, novemberMonthData.calendarMonth.yearMonth)
        assertEquals(december2022, decemberMonthData.calendarMonth.yearMonth)
    }
}
