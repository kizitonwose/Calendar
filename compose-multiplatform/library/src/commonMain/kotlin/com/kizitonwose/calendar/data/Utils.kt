package com.kizitonwose.calendar.data

import com.kizitonwose.calendar.core.YearMonth
import kotlinx.datetime.LocalDate

internal fun checkDateRange(startMonth: YearMonth, endMonth: YearMonth) {
    check(endMonth >= startMonth) {
        "startMonth: $startMonth is greater than endMonth: $endMonth"
    }
}

internal fun checkDateRange(startDate: LocalDate, endDate: LocalDate) {
    check(endDate >= startDate) {
        "startDate: $startDate is greater than endDate: $endDate"
    }
}
