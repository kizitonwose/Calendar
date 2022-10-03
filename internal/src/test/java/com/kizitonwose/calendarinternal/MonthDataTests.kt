package com.kizitonwose.calendarinternal

import org.junit.Test

import java.time.DayOfWeek
import java.time.YearMonth

class MonthDataTests {

    // You can see what May and November 2019 with Monday as the first day of
    // week look like in the included May2019.png and November2019.png files.
    private val may2019 = YearMonth.of(2019, 5)
    private val nov2019 = may2019.plusMonths(6)
    private val firstDayOfWeek = DayOfWeek.MONDAY

    @Test
    fun `test all month in date generation works as expected`() {
//        val monthData = getCalendarMonthData(may2019, 0, firstDayOfWeek, OutDateStyle.EndOfRow)
//
//        val validInDateIndices = (0..1)
//        val inDatesInMonth =
//            monthData.inDays.flatten().filterIndexed { index, _ -> validInDateIndices.contains(index) }
//
//        // inDates are in appropriate indices and have accurate count.
//        Assert.assertTrue(inDatesInMonth.all { it.owner == DayOwner.PREVIOUS_MONTH })
    }
}