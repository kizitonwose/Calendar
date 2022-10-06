package com.kizitonwose.calendardata

import com.kizitonwose.calendarcore.DayPosition
import com.kizitonwose.calendarcore.daysOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

import java.time.DayOfWeek
import java.time.YearMonth

class HeatMapDataTests {

    private val october2022 = YearMonth.of(2022, 10)
    private val november2022 = YearMonth.of(2022, 11)
    private val december2022 = YearMonth.of(2022, 12)
    private val firstDayOfWeek = DayOfWeek.MONDAY

    /** October, November and December 2022
     *  with Monday as the first day of week.
    ┌──┬─────────────────┬───────────┬───────────┐
    │  │Oct 2022         │Nov 2022   │Dec 2022   │
    ├──┼──┬──┬──┬──┬──┬──┼──┬──┬──┬──┼──┬──┬──┬──┤
    │Mo│26│03│10│17│24│31│07│14│21│28│05│12│19│26│
    ├──┼──┼──┼──┼──┼──┼──├──┼──┼──┼──┼──┼──┼──┼──┤
    │Tu│27│04│11│18│25│01│08│15│22│29│06│13│20│27│
    ├──┼──┼──┼──┼──┼──┼──├──┼──┼──┼──┼──┼──┼──┼──┤
    │We│28│05│12│19│26│02│09│16│23│30│07│14│21│28│
    ├──┼──┼──┼──┼──┼──┼──├──┼──┼──┼──┼──┼──┼──┼──┤
    │Th│29│06│13│20│27│03│10│17│24│01│08│15│22│29│
    ├──┼──┼──┼──┼──┼──┼──├──┼──┼──┼──┼──┼──┼──┼──┤
    │Fr│30│07│14│21│28│04│11│18│24│02│09│16│23│30│
    ├──┼──┼──┼──┼──┼──┼──├──┼──┼──┼──┼──┼──┼──┼──┤
    │Sa│01│08│15│22│29│05│12│19│26│03│10│17│24│31│
    ├──┼──┼──┼──┼──┼──┼──├──┼──┼──┼──┼──┼──┼──┼──┤
    │Su│02│09│16│23│30│06│13│20│27│04│11│18│25│01│
    └──┴──┴──┴──┴──┴──┴──└──┴──┴──┴──┴──┴──┴──┴──┘
     **/

    @Test
    fun `number of in and out dates are accurate`() {
        val monthData = getHeatMapCalendarMonthData(october2022, 0, firstDayOfWeek)

        assertEquals(5, monthData.inDays)
        assertEquals(6, monthData.outDays)
    }

    @Test
    fun `negative number of in are accurate`() {
        val novemberMonthData = getHeatMapCalendarMonthData(october2022, 1, firstDayOfWeek)

        assertEquals(-6, novemberMonthData.inDays)
    }

    @Test
    fun `month in and out dates are in the correct positions`() {
        val monthData = getHeatMapCalendarMonthData(october2022, 0, firstDayOfWeek)

        val inDates = monthData.calendarMonth.weekDays.flatten().take(5)
        val outDates = monthData.calendarMonth.weekDays.flatten().takeLast(6)
        val monthDates = monthData.calendarMonth.weekDays.flatten().drop(5).dropLast(6)

        assertTrue(inDates.all { it.position == DayPosition.InDate })
        assertTrue(outDates.all { it.position == DayPosition.OutDate })
        assertTrue(monthDates.all { it.position == DayPosition.MonthDate })
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
