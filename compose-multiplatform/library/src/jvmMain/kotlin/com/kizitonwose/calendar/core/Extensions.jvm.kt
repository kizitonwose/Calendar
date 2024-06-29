package com.kizitonwose.calendar.core

import androidx.compose.ui.text.intl.Locale
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.Locale as JavaLocale

actual fun firstDayOfWeekFromLocale(locale: Locale): DayOfWeek =
    WeekFields.of(JavaLocale.forLanguageTag(locale.toLanguageTag())).firstDayOfWeek
