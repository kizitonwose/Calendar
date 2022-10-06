package com.kizitonwose.calendarinternal

import com.kizitonwose.calendarcore.daysOfWeek
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek

class DayOfWeekTests {
    @Test
    fun `days until works as expected`() {
        assertEquals(5, DayOfWeek.FRIDAY.daysUntil(DayOfWeek.WEDNESDAY))
        assertEquals(2, DayOfWeek.TUESDAY.daysUntil(DayOfWeek.THURSDAY))
        assertEquals(0, DayOfWeek.SUNDAY.daysUntil(DayOfWeek.SUNDAY))
    }

    @Test
    fun `first day of the week works as expected`() {
        DayOfWeek.values().forEach { dayOfWeek ->
            assertEquals(dayOfWeek, daysOfWeek(firstDayOfWeek = dayOfWeek).first())
        }
    }
}
