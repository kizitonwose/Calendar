package com.kizitonwose.calendar.view

/**
 * Determines how the height of each month row on the year-based
 * calendar is calculated.
 *
 * **This class is only relevant for [YearCalendarView].**
 */
public enum class MonthHeight {
    /**
     * Each month row height is determined by the [DaySize] value set on the calendar.
     * Effectively, this is `wrap-content` if the value is [DaySize.Square],
     * [DaySize.SeventhWidth], or [DaySize.FreeForm], and will be equal to the calendar height
     * divided by the number of rows if the value is [DaySize.Rectangle].
     *
     * When used together with [DaySize.Rectangle], the calendar months and days will
     * uniformly stretch to fill the parent's height.
     */
    FollowDaySize,

    /**
     * Each month row height will be the calendar height divided by the number
     * of rows on the calendar. This means that the calendar months will be distributed
     * uniformly to fill the parent's height. However, the day content height will
     * independently determine its height.
     *
     * This allows you to spread the calendar months evenly across the screen while
     * using a [DaySize] value of [DaySize.Square] if you want square day content
     * or [DaySize.SeventhWidth] if you want to set a specific height value for
     * the day content.
     */
    Fill,
}
