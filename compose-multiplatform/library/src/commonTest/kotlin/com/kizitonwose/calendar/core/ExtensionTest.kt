package com.kizitonwose.calendar.core

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals

class ExtensionTest {
    @Test
    fun generalExtensions() {
        assertEquals(
            YearMonth(2024, Month.JUNE),
            Month.JUNE.atYear(2024),
        )
        assertEquals(
            YearMonth(2024, Month.JUNE),
            Month.JUNE.atYear(Year(2024)),
        )
        assertEquals(
            YearMonth(2024, Month.JULY),
            YearMonth(2024, Month.JUNE).plusMonths(1),
        )
        assertEquals(
            YearMonth(2024, Month.MAY),
            YearMonth(2024, Month.JUNE).minusMonths(1),
        )
        assertEquals(
            LocalDate(2024, Month.MAY, 2),
            LocalDate(2024, Month.MAY, 1).plusDays(1),
        )
        assertEquals(
            LocalDate(2024, Month.APRIL, 30),
            LocalDate(2024, Month.MAY, 1).minusDays(1),
        )
        assertEquals(
            LocalDate(2024, Month.JUNE, 2),
            LocalDate(2024, Month.MAY, 2).plusMonths(1),
        )
        assertEquals(
            LocalDate(2024, Month.FEBRUARY, 29),
            LocalDate(2024, Month.MARCH, 30).minusMonths(1),
        )
        assertEquals(
            LocalDate(2024, Month.MAY, 9),
            LocalDate(2024, Month.MAY, 2).plusWeeks(1),
        )
        assertEquals(
            LocalDate(2024, Month.MARCH, 23),
            LocalDate(2024, Month.MARCH, 30).minusWeeks(1),
        )
        assertEquals(
            4,
            LocalDate(2024, Month.MARCH, 1).weeksUntil(LocalDate(2024, Month.MARCH, 30)),
        )
        assertEquals(
            YearMonth(2024, Month.MARCH),
            LocalDate(2024, Month.MARCH, 1).yearMonth,
        )
    }

    @Test
    fun daysUntil() {
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
    fun daysOfWeek() {
        DayOfWeek.entries.forEach { dayOfWeek ->
            assertEquals(dayOfWeek, daysOfWeek(firstDayOfWeek = dayOfWeek).first())
        }
    }
}
