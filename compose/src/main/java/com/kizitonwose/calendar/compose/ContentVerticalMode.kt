package com.kizitonwose.calendar.compose

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Modifier

enum class ContentVerticalMode {
    /** TODO
     *
     */
    Wrap,

    /** TODO
     *
     */
    Fill,
    ;
}

internal fun Modifier.calendarParentHeight(contentVerticalMode: ContentVerticalMode): Modifier =
    when (contentVerticalMode) {
        ContentVerticalMode.Wrap -> this.wrapContentHeight()
        ContentVerticalMode.Fill -> this.fillMaxHeight()
    }

internal fun Modifier.weekRowHeight(
    contentVerticalMode: ContentVerticalMode,
    columnScope: ColumnScope,
): Modifier = with(columnScope) {
    when (contentVerticalMode) {
        ContentVerticalMode.Wrap -> this@weekRowHeight.wrapContentHeight()
        ContentVerticalMode.Fill -> this@weekRowHeight.weight(1f)
    }
}
