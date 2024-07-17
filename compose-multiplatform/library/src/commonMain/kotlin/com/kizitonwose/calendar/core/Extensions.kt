package com.kizitonwose.calendar.core

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
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

public fun YearMonth.atStartOfMonth(): LocalDate = atDay(1)

public fun YearMonth.atEndOfMonth(): LocalDate = atDay(lengthOfMonth())

public fun YearMonth.atDay(day: Int): LocalDate = LocalDate(year, month, day)

public val YearMonth.nextMonth: YearMonth
    get() = this.plusMonths(1)

public val YearMonth.previousMonth: YearMonth
    get() = this.minusMonths(1)

public fun LocalDate.Companion.now(): LocalDate = Clock.System.now()
    .toLocalDateTime(TimeZone.currentSystemDefault())
    .date

public fun YearMonth.Companion.now(): YearMonth = LocalDate.now().yearMonth

public fun YearMonth.plusMonths(value: Int): YearMonth =
    atStartOfMonth().plusMonths(value).yearMonth

public fun YearMonth.minusMonths(value: Int): YearMonth =
    atStartOfMonth().minusMonths(value).yearMonth

public val LocalDate.yearMonth: YearMonth
    get() = YearMonth(year, month)

internal fun LocalDate.plusDays(value: Int): LocalDate = plus(value, DateTimeUnit.DAY)

internal fun LocalDate.minusDays(value: Int): LocalDate = minus(value, DateTimeUnit.DAY)

internal fun LocalDate.plusWeeks(value: Int): LocalDate = plus(value, DateTimeUnit.WEEK)

internal fun LocalDate.minusWeeks(value: Int): LocalDate = minus(value, DateTimeUnit.WEEK)

internal fun LocalDate.plusMonths(value: Int): LocalDate = plus(value, DateTimeUnit.MONTH)

internal fun LocalDate.minusMonths(value: Int): LocalDate = minus(value, DateTimeUnit.MONTH)

internal fun YearMonth.lengthOfMonth(): Int {
    val thisMonthStart = atStartOfMonth()
    val nextMonthStart = thisMonthStart.plusMonths(1)
    return thisMonthStart.daysUntil(nextMonthStart)
}

internal fun YearMonth.monthsUntil(other: YearMonth): Int =
    atStartOfMonth().monthsUntil(other.atStartOfMonth())

internal fun LocalDate.weeksUntil(other: LocalDate): Int =
    until(other, DateTimeUnit.WEEK)

// E.g DayOfWeek.SATURDAY.daysUntil(DayOfWeek.TUESDAY) = 3
internal fun DayOfWeek.daysUntil(other: DayOfWeek) = (7 + (other.isoDayNumber - isoDayNumber)) % 7
