package com.kizitonwose.calendar.data

import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Month.MAY
import java.time.Month.NOVEMBER
import java.time.YearMonth

class MonthDataTests {

    private val may2019 = YearMonth.of(2019, MAY)
    private val november2019 = YearMonth.of(2019, NOVEMBER)
    private val firstDayOfWeek = DayOfWeek.MONDAY

    /** May and November 2019 with Monday as the first day of week.
    ┌────────────────────┐ ┌────────────────────┐
    │      May 2019      │ │   November 2019    │
    ├──┬──┬──┬──┬──┬──┬──┤ ├──┬──┬──┬──┬──┬──┬──┤
    │Mo│Tu│We│Th│Fr│Sa│Su│ │Mo│Tu│We│Th│Fr│Sa│Su│
    ├──┼──┼──┼──┼──┼──┼──┤ ├──┼──┼──┼──┼──┼──┼──┤
    │29│30│01│02│03│04│05│ │28│29│30│31│01│02│03│
    ├──┼──┼──┼──┼──┼──┼──┤ ├──┼──┼──┼──┼──┼──┼──┤
    │06│07│08│09│10│11│12│ │04│05│06│07│08│09│10│
    ├──┼──┼──┼──┼──┼──┼──┤ ├──┼──┼──┼──┼──┼──┼──┤
    │13│14│15│16│17│18│19│ │11│12│13│14│15│16│17│
    ├──┼──┼──┼──┼──┼──┼──┤ ├──┼──┼──┼──┼──┼──┼──┤
    │20│21│22│23│24│25│26│ │18│19│20│21│22│23│24│
    ├──┼──┼──┼──┼──┼──┼──┤ ├──┼──┼──┼──┼──┼──┼──┤
    │27│28│29│30│31│01│02│ │25│26│27│28│29│30│01│
    └──┴──┴──┴──┴──┴──┴──┘ └──┴──┴──┴──┴──┴──┴──┘
     **/

    @Test
    fun `number of in and out dates are accurate`() {
        val monthData = getCalendarMonthData(may2019, 0, firstDayOfWeek, OutDateStyle.EndOfRow)

        assertEquals(2, monthData.inDays)
        assertEquals(2, monthData.outDays)
    }

    @Test
    fun `month in and out dates are in the correct positions`() {
        val monthData = getCalendarMonthData(may2019, 0, firstDayOfWeek, OutDateStyle.EndOfRow)

        val inDates = monthData.calendarMonth.weekDays.flatten().take(2)
        val outDates = monthData.calendarMonth.weekDays.flatten().takeLast(2)
        val monthDates = monthData.calendarMonth.weekDays.flatten().drop(2).dropLast(2)

        assertTrue(inDates.all { it.position == DayPosition.InDate })
        assertTrue(outDates.all { it.position == DayPosition.OutDate })
        assertTrue(monthDates.all { it.position == DayPosition.MonthDate })
    }

    @Test
    fun `end of row out date style does not add a new row`() {
        val endOfRowMonthData =
            getCalendarMonthData(may2019, 0, firstDayOfWeek, OutDateStyle.EndOfRow)

        assertEquals(5, endOfRowMonthData.calendarMonth.weekDays.count())
    }

    @Test
    fun `end of grid out date style adds a new row`() {
        val endOfGridMonthData =
            getCalendarMonthData(may2019, 0, firstDayOfWeek, OutDateStyle.EndOfGrid)

        assertEquals(endOfGridMonthData.calendarMonth.weekDays.count(), 6)
        assertTrue(endOfGridMonthData.calendarMonth.weekDays.last()
            .all { it.position == DayPosition.OutDate })
    }

    @Test
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
    fun `generated month is at the correct offset`() {
        val monthData = getCalendarMonthData(may2019, 6, firstDayOfWeek, OutDateStyle.EndOfRow)

        assertEquals(november2019, monthData.calendarMonth.yearMonth)
    }

    @Test
    fun `month index calculation works as expected`() {
        val index = getMonthIndex(startMonth = may2019, targetMonth = november2019)

        assertEquals(6, index)
    }

    @Test
    fun `month indices count calculation works as expected`() {
        val count = getMonthIndicesCount(startMonth = may2019, endMonth = november2019)

        assertEquals(7, count)
    }
}
