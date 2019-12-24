package com.kizitonwose.calendarview.utils.persian

import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.ZoneOffset
import java.util.*

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

fun LocalDate.toPersianCalendar(): PersianCalendar {
    return PersianCalendar(atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
}

fun YearMonth.persianlMonthLength(): Int = this.atDay(1).toPersianCalendar().monthLength


val PersianCalendar.monthLength: Int
    get() {
        return if (persianMonth < 6) { // Farvardin ... Shahrivar
            31
        } else if ((persianMonth in 6..10) || (isPersianLeapYear && persianMonth == 11)) { // Mehr ... Bahman and leap year Esfand
            30
        } else 29 // Normal Esfand
    }

fun PersianCalendar.toLocalDate(): LocalDate {
    return LocalDate.of(get(Calendar.YEAR), get(Calendar.MONTH)+1, get(Calendar.DAY_OF_MONTH))
}

fun PersianCalendar.addDays(days: Int): PersianCalendar {
    val persianCalendar = PersianCalendar(timeInMillis)
    persianCalendar.add(Calendar.DAY_OF_MONTH, days)
    return persianCalendar
}

fun PersianCalendar.addMonths(months: Int): PersianCalendar {
    val persianCalendar = PersianCalendar(timeInMillis)
    persianCalendar.addPersianDate(Calendar.MONTH, months)
    return persianCalendar
}

fun PersianCalendar.addYears(years: Int): PersianCalendar {
    val persianCalendar = PersianCalendar(timeInMillis)
    persianCalendar.add(Calendar.DAY_OF_MONTH, years)
    return persianCalendar
}

fun PersianCalendar.withDay(day: Int): PersianCalendar {
    val diffDays = day - persianDay
    return PersianCalendar(timeInMillis).addDays(diffDays)
}

fun PersianCalendar.withMonth(month: Int): PersianCalendar {
    val diffMonths = month - persianMonth
    return PersianCalendar(timeInMillis).addMonths(diffMonths)
}

fun PersianCalendar.withYear(year: Int): PersianCalendar {
    val diffYears = year - persianYear
    return PersianCalendar(timeInMillis).addYears(diffYears)
}
