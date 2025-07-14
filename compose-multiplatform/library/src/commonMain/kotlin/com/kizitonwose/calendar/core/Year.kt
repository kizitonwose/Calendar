package com.kizitonwose.calendar.core

import androidx.compose.runtime.Immutable
import com.kizitonwose.calendar.core.format.fromIso8601Year
import com.kizitonwose.calendar.core.format.toIso8601String
import com.kizitonwose.calendar.core.serializers.YearIso8601Serializer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.onDay
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Immutable
@Serializable(with = YearIso8601Serializer::class)
public data class Year(val value: Int) : Comparable<Year> {
    internal val year = value

    init {
        try {
            onMonth(Month.JANUARY)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Year value $value is out of range", e)
        }
    }

    /**
     * Same as java.time.Year.compareTo()
     */
    override fun compareTo(other: Year): Int {
        return value - other.value
    }

    /**
     * Converts this year to the ISO 8601 string representation.
     */
    override fun toString(): String = toIso8601String()

    public companion object {
        /**
         * Obtains the current [Year] from the specified [clock] and [timeZone].
         *
         * Using this method allows the use of an alternate clock or timezone for testing.
         */
        public fun now(
            clock: Clock = Clock.System,
            timeZone: TimeZone = TimeZone.currentSystemDefault(),
        ): Year = Year(LocalDate.now(clock, timeZone).year)

        /**
         * Checks if the year is a leap year, according to the ISO proleptic calendar system rules.
         *
         * This method applies the current rules for leap years across the whole time-line.
         * In general, a year is a leap year if it is divisible by four without remainder.
         * However, years divisible by 100, are not leap years, with the exception of years
         * divisible by 400 which are.
         *
         * For example, 1904 was a leap year it is divisible by 4. 1900 was not a leap year
         * as it is divisible by 100, however 2000 was a leap year as it is divisible by 400.
         *
         * The calculation is proleptic - applying the same rules into the far future and far past.
         * This is historically inaccurate, but is correct for the ISO-8601 standard.
         */
        public fun isLeap(year: Int): Boolean {
            val prolepticYear: Long = year.toLong()
            return prolepticYear and 3 == 0L && (prolepticYear % 100 != 0L || prolepticYear % 400 == 0L)
        }

        /**
         * Obtains an instance of [Year] from a text string such as `2020`.
         *
         * The string format must be `yyyy`, ideally obtained from calling [Year.toString].
         *
         * @throws IllegalArgumentException if the text cannot be parsed or the boundaries of [Year] are exceeded.
         *
         * @see Year.toString
         */
        public fun parseIso8601(string: String): Year {
            return try {
                string.fromIso8601Year()
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid Year value $string", e)
            }
        }
    }
}

/**
 * Checks if the year is a leap year, according to the ISO proleptic calendar system rules.
 *
 * This method applies the current rules for leap years across the whole time-line.
 * In general, a year is a leap year if it is divisible by four without remainder.
 * However, years divisible by 100, are not leap years, with the exception of years
 * divisible by 400 which are.
 *
 * For example, 1904 was a leap year it is divisible by 4. 1900 was not a leap year
 * as it is divisible by 100, however 2000 was a leap year as it is divisible by 400.
 *
 * The calculation is proleptic - applying the same rules into the far future and far past.
 * This is historically inaccurate, but is correct for the ISO-8601 standard.
 */
public fun Year.isLeap(): Boolean = Year.isLeap(year)

/**
 * Returns the number of days in this year.
 *
 * The result is 366 if this is a leap year and 365 otherwise.
 */
public fun Year.length(): Int = if (isLeap()) 366 else 365

/**
 * Returns the [LocalDate] at the specified [dayOfYear] in this year.
 *
 * The day-of-year value 366 is only valid in a leap year
 *
 * @throws IllegalArgumentException if [dayOfYear] value is invalid in this year.
 */
public fun Year.onDay(dayOfYear: Int): LocalDate {
    require(
        dayOfYear >= 1 &&
            (dayOfYear <= 365 || isLeap() && dayOfYear <= 366),
    ) {
        "Invalid dayOfYear value '$dayOfYear' for year '$year"
    }
    for (month in Month.entries) {
        val yearMonth = onMonth(month)
        if (yearMonth.lastDay.dayOfYear >= dayOfYear) {
            return yearMonth.onDay((dayOfYear - yearMonth.firstDay.dayOfYear) + 1)
        }
    }
    throw IllegalArgumentException("Invalid dayOfYear value '$dayOfYear' for year '$year")
}

/**
 * Returns the [LocalDate] at the specified [monthNumber] and [day] in this year.
 *
 * @throws IllegalArgumentException if either [monthNumber] is invalid or the [day] value
 * is invalid in the resolved calendar [Month].
 */
public fun Year.onMonthDay(monthNumber: Int, day: Int): LocalDate = LocalDate(year, monthNumber, day)

/**
 * Returns the [LocalDate] at the specified [month] and [day] in this year.
 *
 * @throws IllegalArgumentException if the [day] value is invalid in the resolved calendar [Month].
 */
public fun Year.onMonthDay(month: Month, day: Int): LocalDate = LocalDate(year, month, day)

/**
 * Returns the [YearMonth] at the specified [month] in this year.
 */
public fun Year.onMonth(month: Month): YearMonth = YearMonth(year, month)

/**
 * Returns the [YearMonth] at the specified [monthNumber] in this year.
 *
 * @throws IllegalArgumentException if either [monthNumber] is invalid.
 */
public fun Year.onMonth(monthNumber: Int): YearMonth = YearMonth(year, monthNumber)

/**
 * Returns the number of whole years between two year values.
 */
public fun Year.yearsUntil(other: Year): Int = other.year - year

/**
 * Returns a [Year] that results from adding the [value] number of years to this year.
 */
public fun Year.plusYears(value: Int): Year = Year(year + value)

/**
 * Returns a [Year] that results from subtracting the [value] number of years to this year.
 */
public fun Year.minusYears(value: Int): Year = Year(year - value)
