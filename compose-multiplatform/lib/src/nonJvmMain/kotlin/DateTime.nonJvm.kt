import calendar.data.now
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import calendar.core.YearMonth as ktYearMonth
import kotlinx.datetime.LocalDate as ktLocalDate
import kotlinx.datetime.Month as ktMonth

actual typealias LocalDate = ktLocalDate
actual typealias YearMonth = ktYearMonth
actual typealias Month = ktMonth

actual class DateTime {
    actual companion object {

    }
}

actual object CalLocalDate {
    actual fun now(): ktLocalDate = ktLocalDate.now()

    actual fun of(year: Int, month: Month, dayOfMonth: Int): ktLocalDate = ktLocalDate(year, month, dayOfMonth)
}

actual object CalYearMonth {
    actual fun now(): ktYearMonth = ktLocalDate.now().yearMonth
}

actual val LocalDate.yearMonth: YearMonth
    get() = ktYearMonth(year, month)

actual val YearMonth.lengthOfMonth: Int
    get() {
        val thisMonthStart = atStartOfMonth()
        val nextMonthStart = thisMonthStart.plusMonths(1)
        return thisMonthStart.daysUntil(nextMonthStart)
    }

actual val LocalDate.month: Month get() = month

actual val LocalDate.year: Int get() = year

actual val YearMonth.month: Month get() = month

actual val YearMonth.year: Int get() = year

actual fun LocalDate.plusDays(value: Int): LocalDate = plus(value, DateTimeUnit.DAY)

actual fun LocalDate.minusDays(value: Int): LocalDate = minus(value, DateTimeUnit.DAY)

actual fun LocalDate.plusMonths(value: Int): LocalDate = plus(value, DateTimeUnit.MONTH)

actual fun LocalDate.minusMonths(value: Int): LocalDate = minus(value, DateTimeUnit.MONTH)

actual fun YearMonth.plusMonths(value: Int): YearMonth =
    atStartOfMonth().plusMonths(value).yearMonth

actual fun YearMonth.minusMonths(value: Int): YearMonth =
    atStartOfMonth().minusMonths(value).yearMonth
