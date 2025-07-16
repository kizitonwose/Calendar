package com.kizitonwose.calendar.core

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.DateTimeArithmeticException
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.until
import kotlinx.datetime.yearMonth
import kotlin.time.Clock

/**
 * Returns the days of week values such that the desired
 * [firstDayOfWeek] property is at the start position.
 *
 * @see [firstDayOfWeekFromLocale]
 */
public fun daysOfWeek(firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale()): List<DayOfWeek> {
    val pivot = 7 - firstDayOfWeek.ordinal
    val daysOfWeek = DayOfWeek.entries
    // Order `daysOfWeek` array so that firstDayOfWeek is at the start position.
    return daysOfWeek.takeLast(pivot) + daysOfWeek.dropLast(pivot)
}

/**
 * Returns the first day of the week from the provided locale.
 */
public expect fun firstDayOfWeekFromLocale(locale: Locale = Locale.current): DayOfWeek

/**
 * Obtains the current [LocalDate] from the specified [clock] and [timeZone].
 *
 * Using this method allows the use of an alternate clock or timezone for testing.
 */
public fun LocalDate.Companion.now(
    clock: Clock = Clock.System,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): LocalDate = clock.todayIn(timeZone)

/**
 * Obtains the current [YearMonth] from the specified [clock] and [timeZone].
 *
 * Using this method allows the use of an alternate clock or timezone for testing.
 */
public fun YearMonth.Companion.now(
    clock: Clock = Clock.System,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): YearMonth = clock.todayIn(timeZone).yearMonth

/**
 * Returns a [LocalDate] that results from adding the [value] number of
 * days to this date.
 *
 * If the [value] is positive, the returned date is later than this date.
 * If the [value] is negative, the returned date is earlier than this date.
 *
 * @throws DateTimeArithmeticException if the result exceeds the boundaries of [LocalDate].
 */
public fun LocalDate.plusDays(value: Int): LocalDate = plus(value, DateTimeUnit.DAY)

/**
 * Returns a [LocalDate] that results from subtracting the [value] number of
 * days from this date.
 *
 * If the [value] is positive, the returned date is later than this date.
 * If the [value] is negative, the returned date is earlier than this date.
 *
 * @throws DateTimeArithmeticException if the result exceeds the boundaries of [LocalDate].
 */
public fun LocalDate.minusDays(value: Int): LocalDate = minus(value, DateTimeUnit.DAY)

/**
 * Returns a [LocalDate] that results from adding the [value] number of
 * months to this date.
 *
 * If the [value] is positive, the returned date is later than this date.
 * If the [value] is negative, the returned date is earlier than this date.
 *
 * @throws DateTimeArithmeticException if the result exceeds the boundaries of [LocalDate].
 */
public fun LocalDate.plusMonths(value: Int): LocalDate = plus(value, DateTimeUnit.MONTH)

/**
 * Returns a [LocalDate] that results from subtracting the [value] number of
 * months from this date.
 *
 * If the [value] is positive, the returned date is later than this date.
 * If the [value] is negative, the returned date is earlier than this date.
 *
 * @throws DateTimeArithmeticException if the result exceeds the boundaries of [LocalDate].
 */
public fun LocalDate.minusMonths(value: Int): LocalDate = minus(value, DateTimeUnit.MONTH)

/**
 * Returns a [LocalDate] that results from adding the [value] number of
 * years to this date.
 *
 * If the [value] is positive, the returned date is later than this date.
 * If the [value] is negative, the returned date is earlier than this date.
 *
 * @throws DateTimeArithmeticException if the result exceeds the boundaries of [LocalDate].
 */
public fun LocalDate.plusYears(value: Int): LocalDate = plus(value, DateTimeUnit.YEAR)

/**
 * Returns a [LocalDate] that results from subtracting the [value] number of
 * years from this date.
 *
 * If the [value] is positive, the returned date is later than this date.
 * If the [value] is negative, the returned date is earlier than this date.
 *
 * @throws DateTimeArithmeticException if the result exceeds the boundaries of [LocalDate].
 */
public fun LocalDate.minusYears(value: Int): LocalDate = minus(value, DateTimeUnit.YEAR)

/**
 * Returns a [YearMonth] that results from adding the [value] number of months
 * to this year-month.
 *
 * If the [value] is positive, the returned year-month is later than this year-month.
 * If the [value] is negative, the returned year-month is earlier than this year-month.
 *
 * @throws DateTimeArithmeticException if the result exceeds the boundaries
 * of [YearMonth] which is essentially the [LocalDate] boundaries.
 */
public fun YearMonth.plusMonths(value: Int): YearMonth = plus(value, DateTimeUnit.MONTH)

/**
 * Returns a [YearMonth] that results from subtracting the [value] number of months
 * from this year-month.
 *
 * If the [value] is positive, the returned year-month is later than this year-month.
 * If the [value] is negative, the returned year-month is earlier than this year-month.
 *
 * @throws DateTimeArithmeticException if the result exceeds the boundaries
 * of [YearMonth] which is essentially the [LocalDate] boundaries.
 */
public fun YearMonth.minusMonths(value: Int): YearMonth = minus(value, DateTimeUnit.MONTH)

/**
 * Returns a [YearMonth] that results from adding the [value] number of years
 * to this year-month.
 *
 * If the [value] is positive, the returned year-month is later than this year-month.
 * If the [value] is negative, the returned year-month is earlier than this year-month.
 *
 * @throws DateTimeArithmeticException if the result exceeds the boundaries
 * of [YearMonth] which is essentially the [LocalDate] boundaries.
 */
public fun YearMonth.plusYears(value: Int): YearMonth = plus(value, DateTimeUnit.YEAR)

/**
 * Returns a [YearMonth] that results from subtracting the [value] number of years
 * from this year-month.
 *
 * If the [value] is positive, the returned year-month is later than this year-month.
 * If the [value] is negative, the returned year-month is earlier than this year-month.
 *
 * @throws DateTimeArithmeticException if the result exceeds the boundaries
 * of [YearMonth] which is essentially the [LocalDate] boundaries.
 */
public fun YearMonth.minusYears(value: Int): YearMonth = minus(value, DateTimeUnit.YEAR)

internal fun LocalDate.plusWeeks(value: Int): LocalDate = plus(value, DateTimeUnit.WEEK)

internal fun LocalDate.minusWeeks(value: Int): LocalDate = minus(value, DateTimeUnit.WEEK)

internal fun LocalDate.weeksUntil(other: LocalDate): Int = until(other, DateTimeUnit.WEEK).toInt()
