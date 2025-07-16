package com.kizitonwose.calendar.data

import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth

// E.g DayOfWeek.SATURDAY.daysUntil(DayOfWeek.TUESDAY) = 3
internal fun DayOfWeek.daysUntil(other: DayOfWeek) = (7 + (other.ordinal - ordinal)) % 7

// E.g DayOfWeek.SATURDAY.plusDays(3) = DayOfWeek.TUESDAY
internal fun DayOfWeek.plusDays(days: Int): DayOfWeek {
    val amount = (days % 7)
    return DayOfWeek.entries[(ordinal + (amount + 7)) % 7]
}

// Find the actual month on the calendar where this date is shown.
internal val CalendarDay.positionYearMonth: YearMonth
    get() = when (position) {
        DayPosition.InDate -> date.yearMonth.plusMonths(1)
        DayPosition.MonthDate -> date.yearMonth
        DayPosition.OutDate -> date.yearMonth.minusMonths(1)
    }

internal inline fun <T> Iterable<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? {
    val result = indexOfFirst(predicate)
    return if (result == -1) null else result
}
