package com.kizitonwose.calendarcompose

import com.kizitonwose.calendarcompose.shared.daysUntil
import com.kizitonwose.calendarcompose.shared.getMonthIndicesCount
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.YearMonth

class UtilsTests {

    @Test
    fun `days until works as expected`() {
        assertEquals(5, DayOfWeek.FRIDAY.daysUntil(DayOfWeek.WEDNESDAY))
        assertEquals(2, DayOfWeek.TUESDAY.daysUntil(DayOfWeek.THURSDAY))
        assertEquals(0, DayOfWeek.SUNDAY.daysUntil(DayOfWeek.SUNDAY))
    }

    @Test
    fun `month count includes start index`() {
        val startMonth = YearMonth.now()
        assertEquals(1, getMonthIndicesCount(startMonth, startMonth))
        assertEquals(3, getMonthIndicesCount(startMonth, startMonth.plusMonths(2)))
    }

    @Test
    fun `first day of the week works as expected`() {
        DayOfWeek.values().forEach { dayOfWeek ->
            assertEquals(dayOfWeek, daysOfWeek(firstDayOfWeek = dayOfWeek).first())
        }
    }
}
