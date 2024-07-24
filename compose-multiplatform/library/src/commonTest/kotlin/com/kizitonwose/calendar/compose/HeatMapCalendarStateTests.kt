package com.kizitonwose.calendar.compose

import com.kizitonwose.calendar.compose.heatmapcalendar.HeatMapCalendarState
import com.kizitonwose.calendar.core.YearMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.now
import com.kizitonwose.calendar.core.plusDays
import com.kizitonwose.calendar.core.plusMonths
import com.kizitonwose.calendar.data.VisibleItemState
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class HeatMapCalendarStateTests {
    @Test
    fun startMonthUpdateIsReflectedInTheState() {
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
    fun endMonthUpdateIsReflectedInTheState() {
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
    fun firstDayOfTheWeekUpdateIsReflectedInTheState() {
        val firstDayOfWeek = LocalDate.now().dayOfWeek

        val state = createState(firstDayOfWeek = firstDayOfWeek)

        state.store[0].weekDays.forEach { week ->
            assertEquals(week.first().date.dayOfWeek, firstDayOfWeek)
        }

        do {
            val updatedFirstDayOfWeek = state.firstDayOfWeek.plusDays(1)
            state.firstDayOfWeek = updatedFirstDayOfWeek

            state.store[0].weekDays.forEach { week ->
                assertEquals(week.first().date.dayOfWeek, updatedFirstDayOfWeek)
            }
        } while (firstDayOfWeek != state.firstDayOfWeek)
    }

    private fun createState(
        startMonth: YearMonth = YearMonth.now(),
        endMonth: YearMonth = startMonth,
        firstVisibleMonth: YearMonth = startMonth,
        firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale(),
        visibleItemState: VisibleItemState = VisibleItemState(),
    ) = HeatMapCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = firstVisibleMonth,
        firstDayOfWeek = firstDayOfWeek,
        visibleItemState = visibleItemState,
    )
}
