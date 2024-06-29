package com.kizitonwose.calendar.core

/**
 * Describes the position of a [WeekDay] on the calendar.
 */
enum class WeekDayPosition {
    /**
     * The day is positioned at the start of the calendar to
     * ensure proper alignment of the first day of the week.
     * The day is before the provided start date.
     */
    InDate,

    /**
     * The day is in the range of the provided start and end dates.
     */
    RangeDate,

    /**
     * The day is positioned at the end of the calendar to to fill the
     * remaining days in the last week after the provided end date.
     * The day is after the provided end date.
     */
    OutDate,
}
