package com.kizitonwose.calendar.core

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.until

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
 * Returns the [YearMonth] value for this date.
 */
public val LocalDate.yearMonth: YearMonth
    get() = YearMonth(year, month)

public fun Month.atYear(year: Year): YearMonth = YearMonth(year.value, this)

public fun Month.atYear(year: Int): YearMonth = YearMonth(year, this)

internal fun YearMonth.plusMonths(value: Int): YearMonth = plus(value, DateTimeUnit.MONTH)

internal fun YearMonth.minusMonths(value: Int): YearMonth = minus(value, DateTimeUnit.MONTH)

internal fun LocalDate.plusDays(value: Int): LocalDate = plus(value, DateTimeUnit.DAY)

internal fun LocalDate.minusDays(value: Int): LocalDate = minus(value, DateTimeUnit.DAY)

internal fun LocalDate.plusWeeks(value: Int): LocalDate = plus(value, DateTimeUnit.WEEK)

internal fun LocalDate.minusWeeks(value: Int): LocalDate = minus(value, DateTimeUnit.WEEK)

internal fun LocalDate.plusMonths(value: Int): LocalDate = plus(value, DateTimeUnit.MONTH)

internal fun LocalDate.minusMonths(value: Int): LocalDate = minus(value, DateTimeUnit.MONTH)

internal fun LocalDate.weeksUntil(other: LocalDate): Int =
    until(other, DateTimeUnit.WEEK)

// E.g DayOfWeek.SATURDAY.daysUntil(DayOfWeek.TUESDAY) = 3
internal fun DayOfWeek.daysUntil(other: DayOfWeek) = (7 + (other.isoDayNumber - isoDayNumber)) % 7
