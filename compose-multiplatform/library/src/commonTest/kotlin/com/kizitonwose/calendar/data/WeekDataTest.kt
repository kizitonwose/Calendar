package com.kizitonwose.calendar.data

import com.kizitonwose.calendar.core.WeekDayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.onDay
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeekDataTest {
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
    fun `date range adjustment works as expected`() {
        val may01 = may2019.onDay(1)
        val nov01 = november2019.onDay(1)
        val adjustedWeekRange = getWeekCalendarAdjustedRange(may01, nov01, firstDayOfWeek)

        assertEquals(LocalDate(2019, Month.APRIL, 29), adjustedWeekRange.startDateAdjusted)
        assertEquals(LocalDate(2019, Month.NOVEMBER, 3), adjustedWeekRange.endDateAdjusted)
    }

    @Test
    @JsName("test2")
    fun `week data generation works as expected`() {
        val may01 = may2019.onDay(1)
        val nov01 = november2019.onDay(1)
        val adjustedWeekRange = getWeekCalendarAdjustedRange(may01, nov01, firstDayOfWeek)
        val week = getWeekCalendarData(adjustedWeekRange.startDateAdjusted, 0, may01, nov01).week

        assertEquals(LocalDate(2019, Month.APRIL, 29), week.days.first().date)
        assertEquals(LocalDate(2019, Month.MAY, 5), week.days.last().date)
    }

    @Test
    @JsName("test3")
    fun `week in date generation works as expected`() {
        val may01 = may2019.onDay(1)
        val nov01 = november2019.onDay(1)
        val adjustedWeekRange = getWeekCalendarAdjustedRange(may01, nov01, firstDayOfWeek)
        val week = getWeekCalendarData(adjustedWeekRange.startDateAdjusted, 0, may01, nov01).week

        val inDates = week.days.take(2)
        val rangeDays = week.days.takeLast(5)
        assertTrue(inDates.all { it.position == WeekDayPosition.InDate })
        assertTrue(rangeDays.all { it.position == WeekDayPosition.RangeDate })
        assertEquals(7, week.days.count())
    }

    @Test
    @JsName("test4")
    fun `week out date generation works as expected`() {
        val may01 = may2019.onDay(1)
        val may31 = may2019.onDay(31)
        val adjustedWeekRange = getWeekCalendarAdjustedRange(may01, may31, firstDayOfWeek)
        val week = getWeekCalendarData(adjustedWeekRange.startDateAdjusted, 4, may01, may31).week

        val outDates = week.days.takeLast(2)
        val rangeDays = week.days.take(5)
        assertTrue(outDates.all { it.position == WeekDayPosition.OutDate })
        assertTrue(rangeDays.all { it.position == WeekDayPosition.RangeDate })
        assertEquals(7, week.days.count())
    }

    @Test
    @JsName("test5")
    fun `days are in the appropriate week columns`() {
        val may01 = may2019.onDay(2)
        val may31 = may2019.onDay(31)
        val adjustedWeekRange = getWeekCalendarAdjustedRange(may01, may31, firstDayOfWeek)
        val week = getWeekCalendarData(adjustedWeekRange.startDateAdjusted, 0, may01, may31).week

        val daysOfWeek = daysOfWeek(firstDayOfWeek)
        week.days.forEachIndexed { index, day ->
            assertEquals(daysOfWeek[index], day.date.dayOfWeek)
        }
    }

    @Test
    @JsName("test6")
    fun `generated week is at the correct offset`() {
        val may01 = may2019.onDay(2)
        val may31 = may2019.onDay(31)
        val adjustedWeekRange = getWeekCalendarAdjustedRange(may01, may31, firstDayOfWeek)
        val week = getWeekCalendarData(adjustedWeekRange.startDateAdjusted, 2, may01, may31).week

        assertEquals(may2019.onDay(13), week.days.first().date)
        assertEquals(may2019.onDay(19), week.days.last().date)
    }

    @Test
    @JsName("test7")
    fun `week index calculation works as expected`() {
        val may01 = may2019.onDay(2)
        val may31 = may2019.onDay(31)
        val adjustedWeekRange = getWeekCalendarAdjustedRange(may01, may31, firstDayOfWeek)
        val index = getWeekIndex(adjustedWeekRange.startDateAdjusted, may31)

        assertEquals(4, index)
    }

    @Test
    @JsName("test8")
    fun `week indices count calculation works as expected`() {
        val may01 = may2019.onDay(2)
        val may31 = may2019.onDay(31)
        val adjustedWeekRange = getWeekCalendarAdjustedRange(may01, may31, firstDayOfWeek)
        val count = getWeekIndicesCount(adjustedWeekRange.startDateAdjusted, may31)

        assertEquals(5, count)
    }
}
