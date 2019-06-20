package com.kizitonwose.calenderview

import com.kizitonwose.calendarview.model.DayOwner
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

    // You can see what May 2019 with Monday as the first
    // day of week looks like here: https://bit.ly/2HwWZ7o
    private val may2019 = YearMonth.of(2019, 5)
    private val nov2019 = may2019.plusMonths(6)
    private val firstDayOfWeek = DayOfWeek.MONDAY

    @Test
    fun `test in date generation works as expected`() {
        val weekDays = MonthConfig.generateWeekDays(may2019, firstDayOfWeek, true, OutDateStyle.END_OF_ROW)

        val validInDateIndices = (0..1)
        val inDatesInMonth = weekDays.flatten().filterIndexed { index, _ -> validInDateIndices.contains(index) }

        // In dates are in appropriate indices and have accurate count.
        assertTrue(inDatesInMonth.all { it.owner == DayOwner.PREVIOUS_MONTH })
    }

    @Test
    fun `test end of row out date generation works as expected`() {
        val weekDays = MonthConfig.generateWeekDays(may2019, firstDayOfWeek, true, OutDateStyle.END_OF_ROW)

        val validOutDateIndices = weekDays.flatten().indices.toList().takeLast(2)
        val outDatesInMonth = weekDays.flatten().filterIndexed { index, _ -> validOutDateIndices.contains(index) }

        // Out dates are in appropriate indices and have accurate count.
        assertTrue(outDatesInMonth.all { it.owner == DayOwner.NEXT_MONTH })
    }

    @Test
    fun `test end of grid out date generation works as expected`() {
        val weekDays = MonthConfig.generateWeekDays(may2019, firstDayOfWeek, true, OutDateStyle.END_OF_GRID)

        val validOutDateIndices = weekDays.flatten().indices.toList().takeLast(9)
        val outDatesInMonth = weekDays.flatten().filterIndexed { index, _ -> validOutDateIndices.contains(index) }

        // Out dates are in appropriate indices and have accurate count.
        assertTrue(outDatesInMonth.all { it.owner == DayOwner.NEXT_MONTH })
    }

    @Test
    fun `test first day of week is in correct position`() {
        val weekDays = MonthConfig.generateWeekDays(may2019, firstDayOfWeek, true, OutDateStyle.END_OF_GRID)

        assertTrue(weekDays.first().first().date.dayOfWeek == firstDayOfWeek)
    }

}
