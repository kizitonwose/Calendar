package com.kizitonwose.calendarinternal

import java.time.YearMonth

fun checkDateRange(startMonth: YearMonth, endMonth: YearMonth) {
    check(endMonth >= startMonth) {
        "startMonth: $startMonth is greater than endMonth: $endMonth"
    }
}
