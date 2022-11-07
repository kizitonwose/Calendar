package com.kizitonwose.calendar.compose

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.listSaver
import com.kizitonwose.calendar.compose.heatmapcalendar.HeatMapCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.WeekCalendarState
import com.kizitonwose.calendar.core.OutDateStyle
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * The states use the [listSaver] type so these tests should catch when we move
 * things around without paying attention to the indices or the actual items
 * being saved. Such issues are typically not caught during development since
 * state restoration (e.g via rotation) will likely not happen often.
 */
class StateSaverTests {

    @Test
    fun `month calendar state can be restored`() {
        val now = YearMonth.now()
        val firstDayOfWeek = DayOfWeek.values().random()
        val state = CalendarState(
            startMonth = now,
            endMonth = now,
            firstVisibleMonth = now,
            outDateStyle = OutDateStyle.EndOfRow,
            firstDayOfWeek = firstDayOfWeek,
            visibleItemState = VisibleItemState(),
        )
        val restored = restore(state, CalendarState.Saver)
        assertEquals(state.startMonth, restored.startMonth)
        assertEquals(state.endMonth, restored.endMonth)
        assertEquals(state.firstVisibleMonth, restored.firstVisibleMonth)
        assertEquals(state.outDateStyle, restored.outDateStyle)
        assertEquals(state.firstDayOfWeek, restored.firstDayOfWeek)
    }

    @Test
    fun `week calendar state can be restored`() {
        val now = LocalDate.now()
        val firstDayOfWeek = DayOfWeek.values().random()
        val state = WeekCalendarState(
            startDate = now,
            endDate = now,
            firstVisibleWeekDate = now,
            firstDayOfWeek = firstDayOfWeek,
            visibleItemState = VisibleItemState(),
        )
        val restored = restore(state, WeekCalendarState.Saver)
        assertEquals(state.startDate, restored.startDate)
        assertEquals(state.endDate, restored.endDate)
        assertEquals(state.firstVisibleWeek, restored.firstVisibleWeek)
        assertEquals(state.firstDayOfWeek, restored.firstDayOfWeek)
    }

    @Test
    fun `heatmap calendar state can be restored`() {
        val now = YearMonth.now()
        val firstDayOfWeek = DayOfWeek.values().random()
        val state = HeatMapCalendarState(
            startMonth = now,
            endMonth = now,
            firstVisibleMonth = now,
            firstDayOfWeek = firstDayOfWeek,
            visibleItemState = VisibleItemState(),
        )
        val restored = restore(state, HeatMapCalendarState.Saver)
        assertEquals(state.startMonth, restored.startMonth)
        assertEquals(state.endMonth, restored.endMonth)
        assertEquals(state.firstVisibleMonth, restored.firstVisibleMonth)
        assertEquals(state.firstDayOfWeek, restored.firstDayOfWeek)
    }

    private fun <State, T : Any> restore(value: State, saver: Saver<State, T>): State {
        with(saver) {
            val saved = SaverScope { true }.save(value)!!
            return restore(saved)!!
        }
    }
}
