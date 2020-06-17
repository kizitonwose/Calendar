package com.kizitonwose.calendarview.model

/**
 * Describes the month to which a [CalendarDay] belongs.
 */
enum class DayOwner {
    /**
     * Belongs to the previous month on the calendar.
     * Such days are referred to as outDates.
     */
    PREVIOUS_MONTH,

    /**
     * Belongs to the current month on the calendar.
     * Such days are referred to as monthDates.
     */
    THIS_MONTH,

    /**
     * Belongs to the next month on the calendar.
     * Such days are referred to as outDates.
     */
    NEXT_MONTH
}

/**
 * Determines how outDates are generated for each month on the calendar.
 */
enum class OutDateStyle {
    /**
     * The calendar will generate outDates until it reaches
     * the first end of a row. This means that if  a month
     * has 6 rows, it will display 6 rows and if a month
     * has 5 rows, it will display 5 rows.
     */
    END_OF_ROW,

    /**
     * The calendar will generate outDates until
     * it reaches the end of a 6 x 7 grid.
     * This means that all months will have 6 rows.
     */
    END_OF_GRID,

    /**
     * outDates will not be generated.
     */
    NONE
}

/**
 * Determines how inDates are generated for
 * each month on the calendar.
 */
enum class InDateStyle {
    /**
     * inDates will be generated for all months.
     */
    ALL_MONTHS,

    /**
     * inDates will be generated for the first month only.
     */
    FIRST_MONTH,

    /**
     * inDates will not be generated, this means that there
     * will be no offset on any month.
     */
    NONE
}

/**
 * The scrolling behavior of the calendar.
 */
enum class ScrollMode {
    /**
     * The calendar will snap to the nearest month
     * after a scroll or swipe action.
     */
    CONTINUOUS,

    /**
     * The calendar scrolls normally.
     */
    PAGED
}
