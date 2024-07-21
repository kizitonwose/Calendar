package com.kizitonwose.calendar.compose.yearcalendar

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier

/**
 * Determines how the height of the month content is calculated.
 */
public enum class YearContentHeightMode {
    /**
     * The calendar months and days will wrap content height. This allows
     * you to use [Modifier.aspectRatio] if you want square day content
     * or [Modifier.height] if you want a specific height value
     * for the day content.
     */
    Wrap,

    /**
     * The calendar months will be distributed uniformly to fill the
     * parent's height. However, the days within the calendar months will
     * wrap content height. This allows you to spread the calendar months
     * evenly across the screen while using [Modifier.aspectRatio] if you
     * want square day content or [Modifier.height] if you want a specific
     * height value for the day content.
     */
    Fill,

    /**
     * The calendar months and days will uniformly stretch to fill the
     * parent's height. This allows you to use [Modifier.fillMaxHeight] for
     * the day content height. With this option, your Calendar composable should
     * also be created with [Modifier.fillMaxHeight] or [Modifier.height].
     */
    Stretch,
}
