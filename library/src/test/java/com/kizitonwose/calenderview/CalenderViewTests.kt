package com.kizitonwose.calenderview

import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.adapter.CalendarConfig
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.OutDateStyle
import com.kizitonwose.calendarview.model.ScrollMode
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.DayOfWeek
import org.threeten.bp.YearMonth


class CalenderViewTests {

    private val april2019 = YearMonth.of(2019, 4)
    private val may2019 = YearMonth.of(2019, 5)
    private val june2019 = YearMonth.of(2019, 6)

    private val endOfRowConfig =
        CalendarConfig(OutDateStyle.END_OF_ROW, ScrollMode.PAGED, RecyclerView.HORIZONTAL, null)

    private val endOfGridConfig =
        CalendarConfig(OutDateStyle.END_OF_GRID, ScrollMode.PAGED, RecyclerView.HORIZONTAL, null)


    @Test
    fun `test in date generation works as expected`() {
        val month = CalendarMonth(may2019, endOfRowConfig, DayOfWeek.MONDAY)

        val validInDateIndices = (0..1)
        val inDatesInMonth = month.weekDays.flatten().filterIndexed { index, _ -> validInDateIndices.contains(index) }

        // In dates are in appropriate indices and have accurate count.
        assertTrue(inDatesInMonth.all { it.owner == DayOwner.PREVIOUS_MONTH })
    }

    @Test
    fun `test end of row out date generation works as expected`() {
        val month = CalendarMonth(may2019, endOfRowConfig, DayOfWeek.MONDAY)

        val monthIndices = month.weekDays.flatten().indices
        val validOutDateIndices = monthIndices.toList().takeLast(2)
        val outDatesInMonth = month.weekDays.flatten().filterIndexed { index, _ -> validOutDateIndices.contains(index) }

        // Out dates are in appropriate indices and have accurate count.
        assertTrue(outDatesInMonth.all { it.owner == DayOwner.NEXT_MONTH })
    }

    @Test
    fun `test end of grid out date generation works as expected`() {
        val month = CalendarMonth(may2019, endOfGridConfig, DayOfWeek.MONDAY)

        val monthIndices = month.weekDays.flatten().indices
        val validOutDateIndices = monthIndices.toList().takeLast(9)
        val outDatesInMonth = month.weekDays.flatten().filterIndexed { index, _ -> validOutDateIndices.contains(index) }

        // Out dates are in appropriate indices and have accurate count.
        assertTrue(outDatesInMonth.all { it.owner == DayOwner.NEXT_MONTH })
    }

    @Test
    fun `test first day of week is in correct position`() {
        val month = CalendarMonth(may2019, endOfRowConfig, DayOfWeek.MONDAY)

        assertTrue(month.weekDays.first().first().date.dayOfWeek == DayOfWeek.MONDAY)
    }
}
