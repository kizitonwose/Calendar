package com.kizitonwose.calendarsample

import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

fun YearMonth.displayText(): String {
    return "${this.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${this.year}"
}

fun DayOfWeek.displayText(): String {
    return getDisplayName(TextStyle.SHORT, Locale.getDefault())
}
