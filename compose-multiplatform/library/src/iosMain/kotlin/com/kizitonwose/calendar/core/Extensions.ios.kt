package com.kizitonwose.calendar.core

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.DayOfWeek
import platform.Foundation.NSCalendar
import platform.Foundation.NSLocale

/**
 * Returns the first day of the week from the provided locale.
 */
public actual fun firstDayOfWeekFromLocale(locale: Locale): DayOfWeek {
    val firstWeekday = NSCalendar.currentCalendar.let {
        it.setLocale(NSLocale(locale.toLanguageTag()))
        // https://developer.apple.com/documentation/foundation/calendar/2293656-firstweekday
        // Value is one-based, starting from sunday
        it.firstWeekday.toInt()
    }
    // Get the index value from a sunday-based array.
    return daysOfWeek(firstDayOfWeek = DayOfWeek.SUNDAY)[firstWeekday - 1]
}
