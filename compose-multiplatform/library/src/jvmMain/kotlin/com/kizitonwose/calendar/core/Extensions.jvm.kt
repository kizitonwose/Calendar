@file:Suppress("NewApi")

package com.kizitonwose.calendar.core

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.toKotlinDayOfWeek
import java.time.temporal.WeekFields as JavaWeekFields
import java.util.Locale as JavaLocale

public actual fun firstDayOfWeekFromLocale(locale: Locale): DayOfWeek =
    JavaWeekFields.of(JavaLocale.forLanguageTag(locale.toLanguageTag()))
        .firstDayOfWeek
        .toKotlinDayOfWeek()
