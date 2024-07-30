package com.kizitonwose.calendar.view

import android.view.ViewGroup

/**
 * Determines how the height of each month row on the year-based calendar is calculated.
 */
public enum class MonthHeight {
    /** TODO DOC
     * Each day will have both width and height matching
     * the width of the calendar divided by 7.
     */
    FollowDaySize,

    /** TODO DOC
     * Each day will have its width matching the width of
     * the calendar divided by 7. This day is allowed to
     * determine its height by setting a specific value
     * or using [ViewGroup.LayoutParams.WRAP_CONTENT].
     */
    Fill,

}
