package com.kizitonwose.calendar.core

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeArithmeticException
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.monthsUntil

@Immutable
public data class Year(val value: Int) : Comparable<Year>, JvmSerializable {
    internal val year = value

    init {
        try {
            atMonth(Month.JANUARY).atStartOfMonth()
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

        public fun isLeap(year: Int): Boolean {
            val prolepticYear: Long = year.toLong()
            return prolepticYear and 3 == 0L && (prolepticYear % 100 != 0L || prolepticYear % 400 == 0L)
        }
    }
}


public fun Year.atDay(dayOfYear: Int): LocalDate {
    for (month in Month.entries) {
        val yearMonth = atMonth(month)
        if (yearMonth.atEndOfMonth().dayOfYear >= dayOfYear) {
            return yearMonth.atDay((dayOfYear - yearMonth.atStartOfMonth().dayOfYear) + 1)
        }
    }
    throw IllegalArgumentException("Invalid dayOfYear value '$dayOfYear' for year '$year")
}

public fun Year.isLeap(): Boolean = Year.isLeap(year)

public fun Year.length(): Int = if (isLeap()) 366 else 365

public fun Year.atMonthDay(month: Int, day: Int): LocalDate = LocalDate(year, month, day)

public fun Year.atMonthDay(month: Month, day: Int): LocalDate = LocalDate(year, month, day)

public fun Year.atMonth(month: Month): YearMonth = YearMonth(year, month)

public fun Year.atMonth(month: Int): YearMonth = YearMonth(year, month)

/**
 * Returns the number of whole months between two year-month values.
 *
 * The value is rounded toward zero.
 *
 * If the result does not fit in [Int], returns [Int.MAX_VALUE] for a
 * positive result or [Int.MIN_VALUE] for a negative result.
 *
 * @see LocalDate.monthsUntil
 */
public fun Year.yearsUntil(other: Year): Int = other.year - year

/**
 * Returns a [Year] that results from adding the [value] number of the
 * specified [unit] to this year-month.
 *
 * If the [value] is positive, the returned year-month is later than this year-month.
 * If the [value] is negative, the returned year-month is earlier than this year-month.
 *
 * @throws DateTimeArithmeticException if the result exceeds the boundaries
 * of [Year] which is essentially the [LocalDate] boundaries.
 */
public fun Year.plusYears(value: Int): Year = Year(year + value)

/**
 * Returns a [Year] that results from subtracting the [value] number of the
 * specified [unit] from this year-month.
 *
 * If the [value] is positive, the returned year-month is earlier than this year-month.
 * If the [value] is negative, the returned year-month is later than this year-month.
 *
 * @throws DateTimeArithmeticException if the result exceeds the boundaries
 * of [Year] which is essentially the [LocalDate] boundaries.
 */
public fun Year.minusYears(value: Int): Year = Year(year - value)
