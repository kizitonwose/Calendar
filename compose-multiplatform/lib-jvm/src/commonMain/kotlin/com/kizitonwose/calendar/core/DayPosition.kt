package com.kizitonwose.calendar.core

/**
 * Describes the position of a [CalendarDay] in the month.
 */
enum class DayPosition {
    /**
     * The day is positioned at the start of the month to
     * ensure proper alignment of the first day of the week.
     * The day belongs to the previous month on the calendar.
     */
    InDate,

    /**
     * The day belongs to the current month on the calendar.
     */
    MonthDate,

    /**
     * The day is positioned at the end of the month to
     * to fill the remaining days after the days in the month.
     * The day belongs to the next month on the calendar.
     * @see [OutDateStyle]
     */
    OutDate,
}
