package com.kizitonwose.calendarsample

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

fun YearMonth.displayText(): String {
    return "${this.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${this.year}"
}

fun DayOfWeek.displayText(): String {
    return getDisplayName(TextStyle.SHORT, Locale.getDefault())
}

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}