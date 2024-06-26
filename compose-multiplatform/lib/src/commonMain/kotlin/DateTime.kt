expect class LocalDate
expect class YearMonth
expect enum class Month

expect class DateTime {
    companion object {

    }
}

expect object CalLocalDate {
    fun now(): LocalDate
    fun of(year: Int, month: Month, dayOfMonth: Int): LocalDate
}

expect object CalYearMonth {
    fun now(): YearMonth
}

expect val LocalDate.year: Int
expect val LocalDate.month: Month
expect val LocalDate.yearMonth: YearMonth
expect val YearMonth.lengthOfMonth: Int
expect val YearMonth.year: Int
expect val YearMonth.month: Month

fun YearMonth.atStartOfMonth(): LocalDate = CalLocalDate.of(year, month, 1)

fun YearMonth.atEndOfMonth(): LocalDate = CalLocalDate.of(year, month, lengthOfMonth)

val YearMonth.nextMonth: YearMonth
    get() = this.plusMonths(1)

val YearMonth.previousMonth: YearMonth
    get() = this.minusMonths(1)

expect fun LocalDate.plusDays(value: Int): LocalDate

expect fun LocalDate.minusDays(value: Int): LocalDate

expect fun LocalDate.plusMonths(value: Int): LocalDate

expect fun LocalDate.minusMonths(value: Int): LocalDate

expect fun YearMonth.plusMonths(value: Int): YearMonth

expect fun YearMonth.minusMonths(value: Int): YearMonth
