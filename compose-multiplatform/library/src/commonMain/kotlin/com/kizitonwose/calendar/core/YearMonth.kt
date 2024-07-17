package com.kizitonwose.calendar.core

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeArithmeticException
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.number
import kotlinx.datetime.plus

@Immutable
public data class YearMonth(val year: Int, val month: Month) : Comparable<YearMonth>, JvmSerializable {
    public constructor(year: Int, monthNumber: Int) :
        this(year = year, month = Month(monthNumber))

    init {
        try {
            atStartOfMonth()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("YearMonth value ${month.number}-$year is out of range", e)
        }
    }

    /**
     * Same as java.time.YearMonth.compareTo()
     */
    override fun compareTo(other: YearMonth): Int {
        var cmp = (year - other.year)
        if (cmp == 0) {
            cmp = (month.number - other.month.number)
        }
        return cmp
    }

    public companion object {
        /**
         * Obtains the current [YearMonth] from the specified [clock] and [timeZone].
         *
         * Using this method allows the use of an alternate clock or timezone for testing.
         */
        public fun now(
            clock: Clock = Clock.System,
            timeZone: TimeZone = TimeZone.currentSystemDefault(),
        ): YearMonth = LocalDate.now(clock, timeZone).yearMonth
    }
}

/**
 * Returns the first [LocalDate] in this year-month.
 *
 * @see YearMonth.atDay
 */
public fun YearMonth.atStartOfMonth(): LocalDate = atDay(1)

/**
 * Returns the last [LocalDate] in this year-month.
 *
 * @see YearMonth.atDay
 */
public fun YearMonth.atEndOfMonth(): LocalDate = atDay(lengthOfMonth())

/**
 * Returns the [LocalDate] at the specified [day] in this year-month.
 */
public fun YearMonth.atDay(day: Int): LocalDate = LocalDate(year, month, day)

/**
 * Returns the number of days in this month, taking account of the year.
 *
 * For example, a date in February would return 29 in a leap year and 28 otherwise.
 */
public fun YearMonth.lengthOfMonth(): Int {
    val thisMonthStart = atStartOfMonth()
    val nextMonthStart = thisMonthStart.plusMonths(1)
    return thisMonthStart.daysUntil(nextMonthStart)
}

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
public fun YearMonth.monthsUntil(other: YearMonth): Int =
    atStartOfMonth().monthsUntil(other.atStartOfMonth())

/**
 * Returns a [YearMonth] that results from adding the [value] number of the
 * specified [unit] to this year-month.
 *
 * If the [value] is positive, the returned year-month is later than this year-month.
 * If the [value] is negative, the returned year-month is earlier than this year-month.
 *
 * @throws DateTimeArithmeticException if the result exceeds the boundaries
 * of [YearMonth] which is essentially the [LocalDate] boundaries.
 */
public fun YearMonth.plus(value: Int, unit: DateTimeUnit.MonthBased): YearMonth =
    atStartOfMonth().plus(value, unit).yearMonth

/**
 * Returns a [YearMonth] that results from subtracting the [value] number of the
 * specified [unit] from this year-month.
 *
 * If the [value] is positive, the returned year-month is earlier than this year-month.
 * If the [value] is negative, the returned year-month is later than this year-month.
 *
 * @throws DateTimeArithmeticException if the result exceeds the boundaries
 * of [YearMonth] which is essentially the [LocalDate] boundaries.
 */
public fun YearMonth.minus(value: Int, unit: DateTimeUnit.MonthBased): YearMonth =
    atStartOfMonth().minus(value, unit).yearMonth
