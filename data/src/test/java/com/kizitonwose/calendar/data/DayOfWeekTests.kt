package com.kizitonwose.calendar.data

import com.kizitonwose.calendar.core.daysOfWeek
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek

class DayOfWeekTests {
    @Test
    fun `days until works as expected`() {
        assertEquals(5, DayOfWeek.FRIDAY.daysUntil(DayOfWeek.WEDNESDAY))
        assertEquals(2, DayOfWeek.TUESDAY.daysUntil(DayOfWeek.THURSDAY))
        assertEquals(0, DayOfWeek.SUNDAY.daysUntil(DayOfWeek.SUNDAY))
        assertEquals(3, DayOfWeek.SATURDAY.daysUntil(DayOfWeek.TUESDAY))
        assertEquals(5, DayOfWeek.WEDNESDAY.daysUntil(DayOfWeek.MONDAY))
        assertEquals(1, DayOfWeek.THURSDAY.daysUntil(DayOfWeek.FRIDAY))
        assertEquals(6, DayOfWeek.MONDAY.daysUntil(DayOfWeek.SUNDAY))
        assertEquals(6, DayOfWeek.SUNDAY.daysUntil(DayOfWeek.SATURDAY))
    }

    @Test
    fun `first day of the week works as expected`() {
        DayOfWeek.values().forEach { dayOfWeek ->
            assertEquals(dayOfWeek, daysOfWeek(firstDayOfWeek = dayOfWeek).first())
        }
    }
}
