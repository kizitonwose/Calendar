package com.kizitonwose.calendar.compose

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier

/**
 * Determines how the height of the day content is calculated.
 */
enum class ContentHeightMode {
    /**
     * The day container will wrap its height. This allows you to
     * use [Modifier.aspectRatio] if you want square day content
     * or [Modifier.height] if you want a specific height value
     * for the day content.
     */
    Wrap,

    /**
     * The days in each month will spread to fill the parent's height after
     * any available header and footer content height has been accounted for.
     * This allows you to use [Modifier.fillMaxHeight] for the day content
     * height. With this option, your Calendar composable should also
     * be created with [Modifier.fillMaxHeight] or [Modifier.height].
     */
    Fill,
}
