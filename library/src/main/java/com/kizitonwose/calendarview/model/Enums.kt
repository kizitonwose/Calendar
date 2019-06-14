package com.kizitonwose.calendarview.model


enum class DayOwner {
    PREVIOUS_MONTH, THIS_MONTH, NEXT_MONTH
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
     * The calendar will generate inDates for all months.
     */
    ALL_MONTHS,

    /**
     * inDates will not be generated, meaning that there
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