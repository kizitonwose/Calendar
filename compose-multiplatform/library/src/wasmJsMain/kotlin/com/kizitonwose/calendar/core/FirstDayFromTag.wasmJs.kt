package com.kizitonwose.calendar.core

// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Intl/Locale/getWeekInfo#firstday
internal actual fun jsFirstDayFromTag(languageTag: String): Int = js("new Intl.Locale(languageTag).weekInfo?.firstDay")
