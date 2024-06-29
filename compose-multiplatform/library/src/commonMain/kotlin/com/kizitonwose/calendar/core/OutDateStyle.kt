package com.kizitonwose.calendar.core

/**
 * Determines how [DayPosition.OutDate] are
 * generated for each month on the calendar.
 */
enum class OutDateStyle {
    /**
     * The calendar will generate outDates until it reaches
     * the end of the month row. This means that if a month
     * has 5 rows, it will display 5 rows and if a month
     * has 6 rows, it will display 6 rows.
     */
    EndOfRow,

    /**
     * The calendar will generate outDates until it
     * reaches the end of a 6 x 7 grid on each month.
     * This means that all months will have 6 rows.
     */
    EndOfGrid,
}
