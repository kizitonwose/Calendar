package com.kizitonwose.calendar.view

import android.view.ViewGroup

/**
 * Determines how the size of each day on the calendar is calculated.
 */
enum class DaySize {
    /**
     * Each day will have both width and height matching
     * the width of the calendar divided by 7.
     */
    Square,

    /**
     * Each day will have its width matching the width of the
     * calendar divided by 7, and its height matching the
     * height of the calendar divided by the number of weeks
     * in the index - could be 4, 5 or 6 for the month calendar,
     * and 1 for the week calendar. Use this if you want each
     * month or week to fill the parent's width and height.
     */
    Rectangle,

    /**
     * Each day will have its width matching the width of
     * the calendar divided by 7. This day is allowed to
     * determine its height by setting a specific value
     * or using [ViewGroup.LayoutParams.WRAP_CONTENT].
     */
    SeventhWidth,

    /**
     * The day is allowed to determine its width and height by
     * setting specific values or using [ViewGroup.LayoutParams.WRAP_CONTENT].
     */
    FreeForm,

    ;

    internal val parentDecidesWidth: Boolean
        get() = this == Square || this == SeventhWidth || this == Rectangle

    internal val parentDecidesHeight: Boolean
        get() = this == Rectangle
}
