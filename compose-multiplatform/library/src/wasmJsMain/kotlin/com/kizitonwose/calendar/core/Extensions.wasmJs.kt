package com.kizitonwose.calendar.core

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.DayOfWeek

/**
 * Returns the first day of the week from the provided locale.
 */
actual fun firstDayOfWeekFromLocale(locale: Locale): DayOfWeek {
    return try {
        val firstDay = jsFirstDayFromTag(locale.toLanguageTag())
        daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)[firstDay - 1]
    } catch (e: Exception) { // Unavailable on Firefox
        firstDayFromMap(locale)
    }
}

// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Intl/Locale/getWeekInfo#firstday
private fun jsFirstDayFromTag(languageTag: String): Int = js("new Intl.Locale(languageTag).weekInfo?.firstDay")
