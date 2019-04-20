package com.kizitonwose.calendarview.model

import org.threeten.bp.LocalDate
import java.io.Serializable


enum class DayOwner {
    PREVIOUS_MONTH, THIS_MONTH, NEXT_MONTH
}

data class CalendarDay internal constructor(val date: LocalDate, val owner: DayOwner) :
    Comparable<CalendarDay>, Serializable {

    override fun toString(): String {
        return "CalendarDay { date =  $date, owner = $owner}"
    }

    override fun compareTo(other: CalendarDay): Int {
        return date.compareTo(other.date)
    }
}
