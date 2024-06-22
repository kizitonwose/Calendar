package calendar.data

import calendar.core.YearMonth
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus

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

internal fun LocalDate.plusDays(value: Int): LocalDate = plus(value, DateTimeUnit.DAY)

internal fun LocalDate.minusDays(value: Int): LocalDate = minus(value, DateTimeUnit.DAY)

internal fun LocalDate.plusMonths(value: Int): LocalDate = plus(value, DateTimeUnit.MONTH)

internal fun LocalDate.minusMonths(value: Int): LocalDate = minus(value, DateTimeUnit.MONTH)

internal fun YearMonth.plusMonths(value: Int): YearMonth = atStartOfMonth().plusMonths(value).yearMonth

internal fun YearMonth.minusMonths(value: Int): YearMonth = atStartOfMonth().minusMonths(value).yearMonth

internal fun YearMonth.lengthOfMonth(): Int {
    val thisMonthStart = atStartOfMonth()
    val nextMonthStart = thisMonthStart.plusMonths(1)
    return thisMonthStart.daysUntil(nextMonthStart)
}

internal fun YearMonth.monthsUntil(other: YearMonth): Int = atStartOfMonth().monthsUntil(other.atStartOfMonth())

// E.g DayOfWeek.SATURDAY.daysUntil(DayOfWeek.TUESDAY) = 3
internal fun DayOfWeek.daysUntil(other: DayOfWeek) = (7 + (other.isoDayNumber - isoDayNumber)) % 7
