package com.kizitonwose.calendar.core

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.DayOfWeek

/**
 * Returns the first day of the week from the provided locale.
 */
public actual fun firstDayOfWeekFromLocale(locale: Locale): DayOfWeek {
    return try {
        val firstDay = jsFirstDayFromTag(locale.toLanguageTag())
        daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)[firstDay - 1]
        // Unavailable on Firefox
    } catch (e: Exception) {
        firstDayFromMap(locale)
    }
}

// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Intl/Locale/getWeekInfo#firstday
private fun jsFirstDayFromTag(languageTag: String): Int = js("new Intl.Locale(languageTag).weekInfo?.firstDay")
