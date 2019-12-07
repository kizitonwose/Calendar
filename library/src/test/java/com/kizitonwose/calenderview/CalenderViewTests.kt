package com.kizitonwose.calenderview

import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.InDateStyle
import com.kizitonwose.calendarview.model.MonthConfig
import com.kizitonwose.calendarview.model.OutDateStyle
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.DayOfWeek
import org.threeten.bp.YearMonth

/**
 * These are core functionality tests.
 * The UI behaviour tests are in the sample project.
 */
class CalenderViewTests {

    // You can see what May and November 2019 with Monday as the first day of
    // week look like in the included May2019.png and November2019.png files.
    private val may2019 = YearMonth.of(2019, 5)
    private val nov2019 = may2019.plusMonths(6)
    private val firstDayOfWeek = DayOfWeek.MONDAY

    @Test
    fun `test all month in date generation works as expected`() {
        val weekDays = MonthConfig.generateWeekDays(may2019, firstDayOfWeek, true, OutDateStyle.END_OF_ROW)

        val validInDateIndices = (0..1)
        val inDatesInMonth = weekDays.flatten().filterIndexed { index, _ -> validInDateIndices.contains(index) }

        // inDates are in appropriate indices and have accurate count.
        assertTrue(inDatesInMonth.all { it.owner == DayOwner.PREVIOUS_MONTH })
    }

    @Test
    fun `test no in date generation works as expected`() {
        val weekDays = MonthConfig.generateWeekDays(may2019, firstDayOfWeek, false, OutDateStyle.END_OF_ROW)
        assertTrue(weekDays.flatten().none { it.owner == DayOwner.PREVIOUS_MONTH })
    }

    @Test
    fun `test first month in date generation works as expected`() {
        val months = MonthConfig.generateBoundedMonths(
            may2019, nov2019, firstDayOfWeek, 6, InDateStyle.FIRST_MONTH, OutDateStyle.NONE
        )

        // inDates are in the first month.
        assertTrue(months.first().weekDays.flatten().any { it.owner == DayOwner.PREVIOUS_MONTH })

        // No inDates in other months.
        assertTrue(months.takeLast(months.size - 1).all {
            it.weekDays.flatten().none { it.owner == DayOwner.PREVIOUS_MONTH }
        })
    }

    @Test
    fun `test end of row out date generation works as expected`() {
        val weekDays = MonthConfig.generateWeekDays(may2019, firstDayOfWeek, true, OutDateStyle.END_OF_ROW)

        val validOutDateIndices = weekDays.flatten().indices.toList().takeLast(2)
        val outDatesInMonth = weekDays.flatten().filterIndexed { index, _ -> validOutDateIndices.contains(index) }

        // outDates are in appropriate indices and have accurate count.
        assertTrue(outDatesInMonth.all { it.owner == DayOwner.NEXT_MONTH })
    }

    @Test
    fun `test end of grid out date generation works as expected`() {
        val weekDays = MonthConfig.generateWeekDays(may2019, firstDayOfWeek, true, OutDateStyle.END_OF_GRID)

        val validOutDateIndices = weekDays.flatten().indices.toList().takeLast(9)
        val outDatesInMonth = weekDays.flatten().filterIndexed { index, _ -> validOutDateIndices.contains(index) }

        // outDates are in appropriate indices and have accurate count.
        assertTrue(outDatesInMonth.all { it.owner == DayOwner.NEXT_MONTH })
    }

    @Test
    fun `test no out date generation works as expected`() {
        val weekDays = MonthConfig.generateWeekDays(may2019, firstDayOfWeek, true, OutDateStyle.NONE)
        assertTrue(weekDays.flatten().none { it.owner == DayOwner.NEXT_MONTH })
    }

    @Test
    fun `test first day of week is in correct position`() {
        val weekDays = MonthConfig.generateWeekDays(may2019, firstDayOfWeek, true, OutDateStyle.END_OF_GRID)

        assertTrue(weekDays.first().first().date.dayOfWeek == firstDayOfWeek)
    }

    @Test
    fun `test max row count works with boundaries`() {
        val maxRowCount = 3
        val months = MonthConfig.generateBoundedMonths(
            may2019, may2019.plusMonths(20),
            firstDayOfWeek, maxRowCount, InDateStyle.ALL_MONTHS, OutDateStyle.END_OF_ROW
        )

        assertTrue(months.all { it.weekDays.count() <= maxRowCount })

        // With a bounded config, OutDateStyle of endOfRow and maxRowCount of 3,
        // there should be two CalendarMonth instances for may2019, the first
        // should have 3 weeks and the second should have 2 weeks.
        val mayCalendarMonths = months.filter { it.yearMonth == may2019 }
        assertTrue(mayCalendarMonths.count() == 2)

        assertTrue(mayCalendarMonths.first().weekDays.count() == 3)
        assertTrue(mayCalendarMonths.last().weekDays.count() == 2)

        assertTrue(mayCalendarMonths.first().indexInSameMonth == 0)
        assertTrue(mayCalendarMonths.last().indexInSameMonth == 1)

        assertTrue(mayCalendarMonths.first().numberOfSameMonth == 2)
        assertTrue(mayCalendarMonths.last().numberOfSameMonth == 2)
    }

    @Test
    fun `test max row count works without boundaries`() {
        val maxRowCount = 3
        val months = MonthConfig.generateUnboundedMonths(
            may2019.minusMonths(40), may2019.plusMonths(50),
            firstDayOfWeek, maxRowCount, InDateStyle.ALL_MONTHS, OutDateStyle.END_OF_GRID
        )

        // The number of weeks in all CalendarMonth instances except the last one must match
        // maxRowCount if the calendar has no boundaries. The number of weeks in the last
        // month must also match maxRowCount if OutDateStyle is endOfGrid, otherwise, it will
        // be the length(1 - maxRowCount) of whatever number of weeks remaining after grouping
        // all weeks by maxRowCount value.
        assertTrue(months.all { it.weekDays.count() == maxRowCount })
    }

    @Test
    fun `test unbounded month generation does not exceed number of days in each month`() {
        val maxRowCount = 6
        MonthConfig.generateUnboundedMonths(
            YearMonth.of(2019, 2), YearMonth.of(2021, 2),
            DayOfWeek.SUNDAY, maxRowCount, InDateStyle.ALL_MONTHS, OutDateStyle.END_OF_GRID
        )
        // No assertion necessary, as this particular range would throw an exception previously
        // when trying to build a day that is out of bounds (eg: December 32).
    }
}
