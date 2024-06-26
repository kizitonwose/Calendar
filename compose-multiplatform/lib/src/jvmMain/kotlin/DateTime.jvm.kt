@file:Suppress("NewApi")

import java.time.LocalDate as jtLocalDate
import java.time.Month as jtMonth
import java.time.YearMonth as jtYearMonth

actual typealias LocalDate = jtLocalDate
actual typealias YearMonth = jtYearMonth
actual typealias Month = jtMonth

actual class DateTime {
    actual companion object {

    }
}

actual object CalLocalDate {
    actual fun now(): jtLocalDate = jtLocalDate.now()

    actual fun of(year: Int, month: Month, dayOfMonth: Int): jtLocalDate = jtLocalDate.of(year, month, dayOfMonth)
}

actual object CalYearMonth {
    actual fun now(): jtYearMonth = jtYearMonth.now()
}

actual val LocalDate.yearMonth: YearMonth get() = jtYearMonth.of(year, month)

actual val YearMonth.lengthOfMonth: Int get() = lengthOfMonth()

actual val LocalDate.month: Month get() = month

actual val LocalDate.year: Int get() = year

actual val YearMonth.month: Month get() = month

actual val YearMonth.year: Int get() = year

actual fun LocalDate.plusDays(value: Int): LocalDate = plusDays(value.toLong())

actual fun LocalDate.minusDays(value: Int): LocalDate = minusDays(value.toLong())

actual fun LocalDate.plusMonths(value: Int): LocalDate = plusMonths(value.toLong())

actual fun LocalDate.minusMonths(value: Int): LocalDate = minusMonths(value.toLong())

actual fun YearMonth.plusMonths(value: Int): YearMonth = plusMonths(value.toLong())

actual fun YearMonth.minusMonths(value: Int): YearMonth = minusMonths(value.toLong())
