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

    private val endOfRowConfig = CalendarConfig(OutDateStyle.END_OF_ROW, ScrollMode.PAGED, RecyclerView.HORIZONTAL, null)

    private val endOfGridConfig = CalendarConfig(OutDateStyle.END_OF_GRID, ScrollMode.PAGED, RecyclerView.HORIZONTAL, null)


    @Test
    fun `test end of row date generation works as expected`() {
        val month = CalendarMonth(may2019, endOfRowConfig, DayOfWeek.MONDAY)

        assertTrue(month.weekDays.flatten().count { it.owner == DayOwner.PREVIOUS_MONTH } == 2) // InDates
        assertTrue(month.weekDays.flatten().count { it.owner == DayOwner.NEXT_MONTH } == 2) // OutDates
    }

    @Test
    fun `test end of grid date generation works as expected`() {

        val month = CalendarMonth(may2019, endOfGridConfig, DayOfWeek.MONDAY)

        assertTrue(month.weekDays.flatten().count { it.owner == DayOwner.PREVIOUS_MONTH } == 2) // InDates
        assertTrue(month.weekDays.flatten().count { it.owner == DayOwner.NEXT_MONTH } == 9) // OutDates
    }
}
