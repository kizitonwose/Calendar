package com.kizitonwose.calendarview.utils.persian

import org.threeten.bp.DayOfWeek
import java.util.*

fun String.getPersianNumbers(): String {
    var string = this
    string = string.replace("0", "۰")
    string = string.replace("1", "١")
    string = string.replace("2", "۲")
    string = string.replace("3", "۳")
    string = string.replace("4", "۴")
    string = string.replace("5", "۵")
    string = string.replace("6", "۶")
    string = string.replace("7", "۷")
    string = string.replace("8", "۸")
    string = string.replace("9", "۹")
    return string
}

fun getPersianNumbers(strings: Array<String>): Array<String> {
    for (i in strings.indices) {
        strings[i] = strings[i].getPersianNumbers()
    }
    return strings
}

fun getPersianNumbers(strings: ArrayList<String>): ArrayList<String> {
    for (i in strings.indices) {
        strings[i] = strings[i].getPersianNumbers()
    }
    return strings
}

fun getLatinNumbers(string: String): String {
    var string = string
    string = string.replace("۰", "0")
    string = string.replace("١", "1")
    string = string.replace("۲", "2")
    string = string.replace("۳", "3")
    string = string.replace("۴", "4")
    string = string.replace("۵", "5")
    string = string.replace("۶", "6")
    string = string.replace("۷", "7")
    string = string.replace("۸", "8")
    string = string.replace("۹", "9")
    return string
}

fun DayOfWeek.getPersianDisplayName(): String {
    return when (this.ordinal) {
        DayOfWeek.MONDAY.ordinal -> PersianCalendarConstants.persianWeekDays[2]
        DayOfWeek.TUESDAY.ordinal -> PersianCalendarConstants.persianWeekDays[3]
        DayOfWeek.WEDNESDAY.ordinal -> PersianCalendarConstants.persianWeekDays[4]
        DayOfWeek.THURSDAY.ordinal -> PersianCalendarConstants.persianWeekDays[5]
        DayOfWeek.FRIDAY.ordinal -> PersianCalendarConstants.persianWeekDays[6]
        DayOfWeek.SATURDAY.ordinal -> PersianCalendarConstants.persianWeekDays[0]
        DayOfWeek.SUNDAY.ordinal -> PersianCalendarConstants.persianWeekDays[1]
        else -> PersianCalendarConstants.persianWeekDays[0]
    }
}

fun DayOfWeek.getPersianDisplayFirstCharString(): String {
    return this.getPersianDisplayName()[0].toString()
}
