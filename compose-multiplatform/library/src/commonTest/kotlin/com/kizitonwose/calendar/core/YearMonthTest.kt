package com.kizitonwose.calendar.core

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals

class YearMonthTest {
    @Test
    fun lengthOfMonth() {
        val leapYearValues = Month.entries.map { it.atYear(2024) }
        val nonLeapYearValues = Month.entries.map { it.atYear(2023) }

        for (value in leapYearValues) {
            val expectedLength = when (value.month) {
                Month.FEBRUARY -> 29
                Month.APRIL,
                Month.JUNE,
                Month.SEPTEMBER,
                Month.NOVEMBER,
                -> 30

                else -> 31
            }
            assertEquals(expectedLength, value.lengthOfMonth())
        }

        for (value in nonLeapYearValues) {
            val expectedLength = when (value.month) {
                Month.FEBRUARY -> 28
                Month.APRIL,
                Month.JUNE,
                Month.SEPTEMBER,
                Month.NOVEMBER,
                -> 30

                else -> 31
            }
            assertEquals(expectedLength, value.lengthOfMonth())
        }
    }

    @Test
    fun atStartOfMonth() {
        val values = listOf(
            YearMonth(2025, Month.JANUARY) to LocalDate(2025, Month.JANUARY, 1),
            YearMonth(2020, Month.JUNE) to LocalDate(2020, Month.JUNE, 1),
        )

        for ((yearMonth, firstDay) in values) {
            assertEquals(yearMonth.atStartOfMonth(), firstDay)
        }
    }

    @Test
    fun atEndOfMonth() {
        val values = listOf(
            YearMonth(2025, Month.JANUARY) to LocalDate(2025, Month.JANUARY, 31),
            YearMonth(2024, Month.JUNE) to LocalDate(2024, Month.JUNE, 30),
            YearMonth(2025, Month.FEBRUARY) to LocalDate(2025, Month.FEBRUARY, 28),
            YearMonth(2024, Month.FEBRUARY) to LocalDate(2024, Month.FEBRUARY, 29),
        )

        for ((yearMonth, lastDay) in values) {
            assertEquals(yearMonth.atEndOfMonth(), lastDay)
        }
    }

    @Test
    fun atDay() {
        val yearMonthValues = Month.entries.map { it.atYear(2024) }

        for (yearMonth in yearMonthValues) {
            for (day in 1..yearMonth.lengthOfMonth()) {
                assertEquals(LocalDate(yearMonth.year, yearMonth.month, day), yearMonth.atDay(day))
            }
        }
    }

    @Test
    fun monthsUntil() {
        val values = listOf(
            YearMonth(2024, Month.JANUARY) to YearMonth(2024, Month.NOVEMBER) toResult 10,
            YearMonth(2024, Month.JANUARY) to YearMonth(2024, Month.DECEMBER) toResult 11,
            YearMonth(2026, Month.MARCH) to YearMonth(2028, Month.FEBRUARY) toResult 23,
            YearMonth(2047, Month.OCTOBER) to YearMonth(2051, Month.APRIL) toResult 42,
            YearMonth(2065, Month.JUNE) to YearMonth(2071, Month.JUNE) toResult 72,
            YearMonth(2020, Month.MAY) to YearMonth(2023, Month.JULY) toResult 38,
            YearMonth(2022, Month.AUGUST) to YearMonth(2022, Month.AUGUST) toResult 0,
            YearMonth(2022, Month.AUGUST) to YearMonth(2022, Month.SEPTEMBER) toResult 1,
        )

        for ((start, end, result) in values) {
            assertEquals(result, start.monthsUntil(end))
            assertEquals(-result, end.monthsUntil(start))
        }
    }

    @Test
    fun plus() {
        val plusMonth = listOf(
            YearMonth(2024, Month.JANUARY) to 10 toResult YearMonth(2024, Month.NOVEMBER),
            YearMonth(2020, Month.MAY) to 38 toResult YearMonth(2023, Month.JULY),
            YearMonth(2022, Month.AUGUST) to 0 toResult YearMonth(2022, Month.AUGUST),
            YearMonth(2022, Month.AUGUST) to 1 toResult YearMonth(2022, Month.SEPTEMBER),
        )
        val plusYear = listOf(
            YearMonth(2024, Month.JANUARY) to 10 toResult YearMonth(2034, Month.JANUARY),
            YearMonth(2020, Month.MAY) to 38 toResult YearMonth(2058, Month.MAY),
            YearMonth(2022, Month.AUGUST) to 0 toResult YearMonth(2022, Month.AUGUST),
            YearMonth(2022, Month.SEPTEMBER) to 1 toResult YearMonth(2023, Month.SEPTEMBER),
        )

        for ((start, value, result) in plusMonth) {
            assertEquals(result, start.plus(value, DateTimeUnit.MONTH))
            assertEquals(start, result.plus(-value, DateTimeUnit.MONTH))
        }

        for ((start, value, result) in plusYear) {
            assertEquals(result, start.plus(value, DateTimeUnit.YEAR))
            assertEquals(start, result.plus(-value, DateTimeUnit.YEAR))
        }
    }

    @Test
    fun minus() {
        val minusMonth = listOf(
            YearMonth(2024, Month.JANUARY) to 10 toResult YearMonth(2023, Month.MARCH),
            YearMonth(2020, Month.MAY) to 38 toResult YearMonth(2017, Month.MARCH),
            YearMonth(2022, Month.AUGUST) to 0 toResult YearMonth(2022, Month.AUGUST),
            YearMonth(2022, Month.AUGUST) to 1 toResult YearMonth(2022, Month.JULY),
        )
        val minusYear = listOf(
            YearMonth(2024, Month.JANUARY) to 10 toResult YearMonth(2014, Month.JANUARY),
            YearMonth(2020, Month.MAY) to 38 toResult YearMonth(1982, Month.MAY),
            YearMonth(2022, Month.AUGUST) to 0 toResult YearMonth(2022, Month.AUGUST),
            YearMonth(2022, Month.SEPTEMBER) to 1 toResult YearMonth(2021, Month.SEPTEMBER),
        )

        for ((start, value, result) in minusMonth) {
            assertEquals(result, start.minus(value, DateTimeUnit.MONTH))
            assertEquals(start, result.minus(-value, DateTimeUnit.MONTH))
        }

        for ((start, value, result) in minusYear) {
            assertEquals(result, start.minus(value, DateTimeUnit.YEAR))
            assertEquals(start, result.minus(-value, DateTimeUnit.YEAR))
        }
    }

    @Test
    fun next() {
        val values = listOf(
            YearMonth(2024, Month.DECEMBER) to YearMonth(2025, Month.JANUARY),
            YearMonth(2020, Month.MAY) to YearMonth(2020, Month.JUNE),
        )

        for ((start, next) in values) {
            assertEquals(next, start.next)
        }
    }

    @Test
    fun previous() {
        val values = listOf(
            YearMonth(2025, Month.JANUARY) to YearMonth(2024, Month.DECEMBER),
            YearMonth(2020, Month.JUNE) to YearMonth(2020, Month.MAY),
        )

        for ((start, previous) in values) {
            assertEquals(previous, start.previous)
        }
    }
}
