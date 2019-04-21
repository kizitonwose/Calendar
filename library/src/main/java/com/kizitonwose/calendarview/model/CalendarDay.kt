package com.kizitonwose.calendarview.model

import org.threeten.bp.LocalDate
import java.io.Serializable


enum class DayOwner {
    PREVIOUS_MONTH, THIS_MONTH, NEXT_MONTH
}

data class CalendarDay internal constructor(val date: LocalDate, val owner: DayOwner) :
    Comparable<CalendarDay>, Serializable {

    val day = date.dayOfMonth

    override fun toString(): String {
        return "CalendarDay { date =  $date, owner = $owner}"
    }


    override fun compareTo(other: CalendarDay): Int {
        return date.compareTo(other.date)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CalendarDay
        return date == other.date && owner == other.owner
    }

    override fun hashCode(): Int {
        return 31 * (date.hashCode() + owner.hashCode())
    }
}
