package com.kizitonwose.calendar.compose

import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class CalendarStateTests {

    @Test
    fun `start month update is reflected in the state`() {
        val now = YearMonth.now()
        val updatedStartMonth = now.minusMonths(4)
        val state = createState(
            startMonth = now,
            endMonth = now,
        )

        assertEquals(state.store[0].yearMonth, now)

        state.startMonth = updatedStartMonth

        assertEquals(state.store[0].yearMonth, updatedStartMonth)
    }

    @Test
    fun `end month update is reflected in the state`() {
        val now = YearMonth.now()
        val updatedEndMonth = now.plusMonths(4)
        val state = createState(
            startMonth = now,
            endMonth = now,
        )

        assertEquals(state.store[0].yearMonth, now)

        state.endMonth = updatedEndMonth

        assertEquals(state.store[4].yearMonth, updatedEndMonth)
    }

    @Test
    fun `first day of the week update is reflected in the state`() {
        val firstDayOfWeek = LocalDate.now().dayOfWeek

        val state = createState(firstDayOfWeek = firstDayOfWeek)

        state.store[0].weekDays.forEach { week ->
            assertEquals(week.first().date.dayOfWeek, firstDayOfWeek)
        }

        do {
            val updatedFirstDayOfWeek = state.firstDayOfWeek.plus(1)
            state.firstDayOfWeek = updatedFirstDayOfWeek

            state.store[0].weekDays.forEach { week ->
                assertEquals(week.first().date.dayOfWeek, updatedFirstDayOfWeek)
            }
        } while (firstDayOfWeek != state.firstDayOfWeek)
    }

    @Test
    fun `out date style update is reflected in the state`() {
        val outDateStyle = OutDateStyle.EndOfRow
        // Nov 2022 has 5 weeks when Sun is the first day.
        val startMonth = YearMonth.of(2022, 11)
        val state = createState(
            startMonth = startMonth,
            endMonth = startMonth,
            outDateStyle = outDateStyle,
            firstDayOfWeek = DayOfWeek.SUNDAY,
        )

        assertEquals(state.store[0].weekDays.count(), 5)

        state.outDateStyle = OutDateStyle.EndOfGrid

        assertEquals(state.store[0].weekDays.count(), 6)
    }

    private fun createState(
        startMonth: YearMonth = YearMonth.now(),
        endMonth: YearMonth = startMonth,
        firstVisibleMonth: YearMonth = startMonth,
        outDateStyle: OutDateStyle = OutDateStyle.EndOfRow,
        firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale(),
        visibleItemState: VisibleItemState = VisibleItemState(),
    ) = CalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = firstVisibleMonth,
        outDateStyle = outDateStyle,
        firstDayOfWeek = firstDayOfWeek,
        visibleItemState = visibleItemState,
    )
}
