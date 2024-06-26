package calendar.data

import YearMonth
import kotlinx.datetime.LocalDate

fun checkDateRange(startMonth: YearMonth, endMonth: YearMonth) {
    check(endMonth >= startMonth) {
        "startMonth: $startMonth is greater than endMonth: $endMonth"
    }
}

fun checkDateRange(startDate: LocalDate, endDate: LocalDate) {
    check(endDate >= startDate) {
        "startDate: $startDate is greater than endDate: $endDate"
    }
}
