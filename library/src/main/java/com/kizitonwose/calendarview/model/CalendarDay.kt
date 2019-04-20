package com.kizitonwose.calendarview.model

import org.threeten.bp.LocalDate
import java.io.Serializable


enum class DayOwner {
    PREVIOUS_MONTH, THIS_MONTH, NEXT_MONTH
}

data class CalendarDay internal constructor(val day: Int, val month: Int, val year: Int, val owner: DayOwner) :
    Comparable<CalendarDay>, Serializable {

    val date: LocalDate = LocalDate.of(year, month, day)

    var isSelected = false

    override fun toString(): String {
        return "CalendarDay { date =  $date, owner = $owner, isSelected = $isSelected}"
    }

    override fun compareTo(other: CalendarDay): Int {
        return date.compareTo(other.date)
    }
}
