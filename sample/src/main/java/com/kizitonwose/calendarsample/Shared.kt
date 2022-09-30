package com.kizitonwose.calendarsample

import com.kizitonwose.calendarview.utils.yearMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*

fun YearMonth.displayText(): String {
    return "${this.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${this.year}"
}

fun DayOfWeek.displayText(): String {
    return getDisplayName(TextStyle.SHORT, Locale.getDefault())
}

data class DateSelection(val startDate: LocalDate? = null, val endDate: LocalDate? = null) {
    val daysBetween by lazy {
        if (startDate == null || endDate == null) null else {
            ChronoUnit.DAYS.between(startDate, endDate)
        }
    }
}

fun dateRangeDisplayText(startDate: LocalDate, endDate: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    return "Selected: ${formatter.format(startDate)} - ${formatter.format(endDate)}"
}

object ContinuousSelectionHelper {
    fun getSelection(
        clickedDate: LocalDate,
        selectionStartDate: LocalDate?,
        selectionEndDate: LocalDate?,
    ): DateSelection {
        return if (selectionStartDate != null) {
            if (clickedDate < selectionStartDate || selectionEndDate != null) {
                DateSelection(startDate = clickedDate, endDate = null)
            } else if (clickedDate != selectionStartDate) {
                DateSelection(startDate = selectionStartDate, endDate = clickedDate)
            } else {
                DateSelection(startDate = clickedDate, endDate = null)
            }
        } else {
            DateSelection(startDate = clickedDate, endDate = null)
        }
    }

    fun isInDateBetween(inDate: LocalDate, startDate: LocalDate, endDate: LocalDate): Boolean {
        if (startDate.yearMonth == endDate.yearMonth) return false
        if (inDate.yearMonth == startDate.yearMonth) return true
        val firstDateInThisMonth = inDate.plusMonths(1).yearMonth.atDay(1)
        return firstDateInThisMonth >= startDate && firstDateInThisMonth <= endDate && startDate != firstDateInThisMonth
    }

    fun isOutDateBetween(outDate: LocalDate, startDate: LocalDate, endDate: LocalDate): Boolean {
        if (startDate.yearMonth == endDate.yearMonth) return false
        if (outDate.yearMonth == endDate.yearMonth) return true
        val lastDateInThisMonth = outDate.minusMonths(1).yearMonth.atEndOfMonth()
        return lastDateInThisMonth >= startDate && lastDateInThisMonth <= endDate && endDate != lastDateInThisMonth
    }
}
