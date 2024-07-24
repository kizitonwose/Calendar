package com.kizitonwose.calendar.core

import androidx.compose.runtime.Immutable
import com.kizitonwose.calendar.core.format.fromIso8601YearMonth
import com.kizitonwose.calendar.core.format.toIso8601String
import com.kizitonwose.calendar.core.serializers.YearMonthIso8601Serializer
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeArithmeticException
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable

@Immutable
@Serializable(with = YearMonthIso8601Serializer::class)
public data class YearMonth(val year: Int, val month: Month) : Comparable<YearMonth> {
    public constructor(year: Int, monthNumber: Int) :
        this(year = year, month = Month(monthNumber))

    /**
     * Returns the number-of-the-month (1..12) component of the year-month.
     *
     * Shortcut for `month.number`.
     */
    public val monthNumber: Int get() = month.number

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

        /**
         * Obtains an instance of [YearMonth] from a text string such as `2020-01`.
         *
         * The string format must be `yyyy-MM`, ideally obtained from calling [YearMonth.toString].
         *
         * @throws IllegalArgumentException if the text cannot be parsed or the boundaries of [YearMonth] are exceeded.
         *
         * @see YearMonth.toString
         */
        public fun parseIso8601(string: String): YearMonth {
            return try {
                string.fromIso8601YearMonth()
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid YearMonth value $string", e)
            }
        }
    }

    /**
     * Converts this year-month to the ISO 8601 string representation.
     *
     * Example: `2020-01`
     */
    override fun toString(): String = toIso8601String()
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
    return when (month) {
        Month.FEBRUARY -> if (Year.isLeap(year)) 29 else 28
        Month.APRIL,
        Month.JUNE,
        Month.SEPTEMBER,
        Month.NOVEMBER,
        -> 30

        else -> 31
    }
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

/**
 * Returns a [YearMonth] that results from adding the 1 month this year-month.
 *
 * @throws DateTimeArithmeticException if the result exceeds the boundaries
 * of [YearMonth] which is essentially the [LocalDate] boundaries.
 *
 * @see YearMonth.plus
 */
public val YearMonth.next: YearMonth
    get() = this.plus(1, DateTimeUnit.MONTH)

/**
 * Returns a [YearMonth] that results from subtracting the 1 month this year-month.
 *
 * @throws DateTimeArithmeticException if the result exceeds the boundaries
 * of [YearMonth] which is essentially the [LocalDate] boundaries.
 *
 * @see YearMonth.minus
 */
public val YearMonth.previous: YearMonth
    get() = this.minus(1, DateTimeUnit.MONTH)
