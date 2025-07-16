package com.kizitonwose.calendar.core

import com.kizitonwose.calendar.utils.toTriple
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class YearTest {
    @Test
    fun isLeap() {
        assertEquals(false, Year.isLeap(1999))
        assertEquals(true, Year.isLeap(2000))
        assertEquals(false, Year.isLeap(2001))
        assertEquals(false, Year.isLeap(2007))
        assertEquals(true, Year.isLeap(2008))
        assertEquals(false, Year.isLeap(2009))
        assertEquals(false, Year.isLeap(2010))
        assertEquals(false, Year.isLeap(2011))
        assertEquals(true, Year.isLeap(2012))
        assertEquals(false, Year.isLeap(2095))
        assertEquals(true, Year.isLeap(2096))
        assertEquals(false, Year.isLeap(2097))
        assertEquals(false, Year.isLeap(2098))
        assertEquals(false, Year.isLeap(2099))
        assertEquals(false, Year.isLeap(2100))
        assertEquals(false, Year.isLeap(2101))
        assertEquals(false, Year.isLeap(2102))
        assertEquals(false, Year.isLeap(2103))
        assertEquals(true, Year.isLeap(2104))
        assertEquals(false, Year.isLeap(2105))
        assertEquals(false, Year.isLeap(-500))
        assertEquals(true, Year.isLeap(-400))
        assertEquals(false, Year.isLeap(-300))
        assertEquals(false, Year.isLeap(-200))
        assertEquals(false, Year.isLeap(-100))
        assertEquals(true, Year.isLeap(0))
        assertEquals(false, Year.isLeap(100))
        assertEquals(false, Year.isLeap(200))
        assertEquals(false, Year.isLeap(300))
        assertEquals(true, Year.isLeap(400))
        assertEquals(false, Year.isLeap(500))
    }

    @Test
    fun length() {
        val leapYears = listOf(2000, 2008, 2012, 2096, 0, -400)
        val nonLeapYears = listOf(2001, 2011, 2095, 500, 1, -500)

        for (year in leapYears) {
            assertEquals(366, Year(year).length())
        }

        for (year in nonLeapYears) {
            assertEquals(365, Year(year).length())
        }
    }

    @Test
    fun atMonth() {
        for (year in listOf(0, -400, 2024, 1, 1999)) {
            for (month in Month.entries) {
                assertEquals(YearMonth(year, month), Year(year).onMonth(month))
            }
        }
    }

    @Test
    fun atMonthNumber() {
        for (year in listOf(0, -400, 2024, 1, 1999)) {
            for (month in Month.entries) {
                assertEquals(YearMonth(year, month.number), Year(year).onMonth(month))
            }
        }
    }

    @Test
    fun atMonthDay() {
        val validDays = listOf(
            2024 to Month.FEBRUARY toTriple 29,
            1999 to Month.JUNE toTriple 30,
            2030 to Month.DECEMBER toTriple 31,
            1866 to Month.DECEMBER toTriple 1,
        )
        val invalidDays = listOf(
            2023 to Month.FEBRUARY toTriple 29,
            1999 to Month.JUNE toTriple 31,
            2030 to Month.DECEMBER toTriple -1,
            1866 to Month.DECEMBER toTriple 0,
        )

        for ((year, month, day) in validDays) {
            assertEquals(LocalDate(year, month, day), Year(year).onMonthDay(month, day))
        }

        for ((year, month, day) in invalidDays) {
            assertFailsWith(IllegalArgumentException::class) {
                Year(year).onMonthDay(month, day)
            }
        }
    }

    @Test
    fun atMonthNumberDay() {
        val validDays = listOf(
            2024 to 1 toTriple 29,
            1999 to 6 toTriple 30,
            2030 to 12 toTriple 31,
            1866 to 12 toTriple 1,
        )
        val invalidDays = listOf(
            2023 to 0 toTriple 29,
            1999 to 13 toTriple 31,
            2030 to 6 toTriple -1,
            1866 to 1 toTriple 0,
        )

        for ((year, monthNumber, day) in validDays) {
            assertEquals(LocalDate(year, monthNumber, day), Year(year).onMonthDay(monthNumber, day))
        }

        for ((year, monthNumber, day) in invalidDays) {
            assertFailsWith(IllegalArgumentException::class) {
                Year(year).onMonthDay(monthNumber, day)
            }
        }
    }

    @Test
    fun atDay() {
        val validDays = listOf(
            2024 to 366 toTriple LocalDate(2024, Month.DECEMBER, 31),
            2039 to 365 toTriple LocalDate(2039, Month.DECEMBER, 31),
            1999 to 30 toTriple LocalDate(1999, Month.JANUARY, 30),
            1866 to 59 toTriple LocalDate(1866, Month.FEBRUARY, 28),
        )
        val invalidDays = listOf(
            2034 to 367,
            2023 to 366,
            2030 to -1,
            2030 to 0,
        )

        for ((year, dayOfYear, date) in validDays) {
            assertEquals(date, Year(year).onDay(dayOfYear))
        }

        for ((year, dayOfYear) in invalidDays) {
            assertFailsWith(IllegalArgumentException::class) {
                Year(year).onDay(dayOfYear)
            }
        }
    }

    @Test
    fun yearsUntil() {
        for ((start, end, result) in listOf(
            2020 to 2024 toTriple 4,
            2024 to 2030 toTriple 6,
            1999 to 2028 toTriple 29,
            1300 to 1365 toTriple 65,
        )) {
            assertEquals(result, Year(start).yearsUntil(Year(end)))
            assertEquals(-result, Year(end).yearsUntil(Year(start)))
        }
    }

    @Test
    fun plus() {
        for ((start, value, result) in listOf(
            2020 to 4 toTriple 2024,
            2024 to 6 toTriple 2030,
            1999 to 29 toTriple 2028,
            1300 to 65 toTriple 1365,
        )) {
            assertEquals(Year(result), Year(start).plusYears(value))
            assertEquals(Year(start), Year(result).plusYears(-value))
        }
    }

    @Test
    fun minus() {
        for ((start, value, result) in listOf(
            2020 to 4 toTriple 2016,
            2024 to 6 toTriple 2018,
            1999 to 29 toTriple 1970,
            1300 to 65 toTriple 1235,
        )) {
            assertEquals(Year(result), Year(start).minusYears(value))
            assertEquals(Year(start), Year(result).minusYears(-value))
        }
    }

    @Test
    fun toIso8601String() {
        for ((value, result) in listOf(
            2024 to "2024",
            -1999 to "-1999",
            1 to "0001",
            0 to "0000",
        )) {
            assertEquals(result, Year(value).toString())
        }
    }

    @Test
    fun parseIso8601() {
        for ((value, result) in listOf(
            "2025" to Year(2025),
            "-1999" to Year(-1999),
            "0001" to Year(1),
            "0000" to Year(0),
        )) {
            assertEquals(result, Year.parseIso8601(value))
        }

        for (value in listOf("20", "-6")) {
            assertFailsWith(IllegalArgumentException::class) {
                Year.parseIso8601(value)
            }
        }
    }
}
