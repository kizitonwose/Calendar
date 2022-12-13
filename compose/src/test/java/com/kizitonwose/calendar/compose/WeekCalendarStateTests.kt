package com.kizitonwose.calendar.compose

import com.kizitonwose.calendar.compose.weekcalendar.WeekCalendarState
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class WeekCalendarStateTests {

    @Test
    fun `start date update is reflected in the state`() {
        val now = LocalDate.now()
        val updatedStartDate = now.minusDays(7)
        val state = createState(
            startDate = now,
            endDate = now,
        )

        assertTrue(state.store[0].days.map { it.date }.contains(now))

        state.startDate = updatedStartDate

        assertTrue(state.store[0].days.map { it.date }.contains(updatedStartDate))
    }

    @Test
    fun `end date update is reflected in the state`() {
        val now = LocalDate.now()
        val updatedEndDate = now.plusDays(7)
        val state = createState(
            startDate = now,
            endDate = now,
        )

        assertTrue(state.store[0].days.map { it.date }.contains(now))

        state.endDate = updatedEndDate

        assertTrue(state.store[1].days.map { it.date }.contains(updatedEndDate))
    }

    @Test
    fun `first day of the week update is reflected in the state`() {
        val firstDayOfWeek = LocalDate.now().dayOfWeek

        val state = createState(firstDayOfWeek = firstDayOfWeek)

        assertEquals(state.store[0].days.first().date.dayOfWeek, firstDayOfWeek)

        do {
            val updatedFirstDayOfWeek = state.firstDayOfWeek.plus(1)
            state.firstDayOfWeek = updatedFirstDayOfWeek

            assertEquals(state.store[0].days.first().date.dayOfWeek, updatedFirstDayOfWeek)
        } while (firstDayOfWeek != state.firstDayOfWeek)
    }

    private fun createState(
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate = startDate,
        firstVisibleWeekDate: LocalDate = startDate,
        firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale(),
        visibleItemState: VisibleItemState = VisibleItemState(),
    ) = WeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstVisibleWeekDate = firstVisibleWeekDate,
        firstDayOfWeek = firstDayOfWeek,
        visibleItemState = visibleItemState,
    )
}
