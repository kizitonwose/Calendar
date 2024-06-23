package calendar.data

import calendar.core.YearMonth
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

/**
 * Returns the days of week values such that the desired
 * [firstDayOfWeek] property is at the start position.
 */
fun daysOfWeek(firstDayOfWeek: DayOfWeek): List<DayOfWeek> {
    val pivot = 7 - firstDayOfWeek.ordinal
    val daysOfWeek = DayOfWeek.entries
    // Order `daysOfWeek` array so that firstDayOfWeek is at the start position.
    return (daysOfWeek.takeLast(pivot) + daysOfWeek.dropLast(pivot))
}

// /**
// * Returns the first day of the week from the provided locale.
// */
// fun firstDayOfWeekFromLocale(locale: Locale = Locale.current): DayOfWeek = ??

fun YearMonth.atStartOfMonth(): LocalDate = LocalDate(year, month, 1)

fun YearMonth.atEndOfMonth(): LocalDate = LocalDate(year, month, lengthOfMonth())

val LocalDate.yearMonth: YearMonth
    get() = YearMonth(year, month)

val YearMonth.nextMonth: YearMonth
    get() = this.plusMonths(1)

val YearMonth.previousMonth: YearMonth
    get() = this.minusMonths(1)

fun LocalDate.plusDays(value: Int): LocalDate = plus(value, DateTimeUnit.DAY)

fun LocalDate.minusDays(value: Int): LocalDate = minus(value, DateTimeUnit.DAY)

fun LocalDate.plusMonths(value: Int): LocalDate = plus(value, DateTimeUnit.MONTH)

fun LocalDate.minusMonths(value: Int): LocalDate = minus(value, DateTimeUnit.MONTH)

fun YearMonth.plusMonths(value: Int): YearMonth =
    atStartOfMonth().plusMonths(value).yearMonth

fun YearMonth.minusMonths(value: Int): YearMonth =
    atStartOfMonth().minusMonths(value).yearMonth

fun YearMonth.lengthOfMonth(): Int {
    val thisMonthStart = atStartOfMonth()
    val nextMonthStart = thisMonthStart.plusMonths(1)
    return thisMonthStart.daysUntil(nextMonthStart)
}

// TODO KMP Maybe restrict to group
val YearMonth.Companion.current: YearMonth
    get() = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date.yearMonth

internal fun YearMonth.monthsUntil(other: YearMonth): Int =
    atStartOfMonth().monthsUntil(other.atStartOfMonth())

// E.g DayOfWeek.SATURDAY.daysUntil(DayOfWeek.TUESDAY) = 3
internal fun DayOfWeek.daysUntil(other: DayOfWeek) = (7 + (other.isoDayNumber - isoDayNumber)) % 7
