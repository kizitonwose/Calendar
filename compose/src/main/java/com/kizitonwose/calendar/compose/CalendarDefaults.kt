package com.kizitonwose.calendar.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

internal object CalendarDefaults {

    /**
     * The default implementation in [rememberSnapFlingBehavior] snaps to the center of the layout
     * but we want to snap to the start. For example, in a vertical calendar, when the layout size
     * is larger than the item size(e.g two or more visible months), we don't want the item's
     * center to be at the center of the layout when it snaps, instead we want the item's top
     * to be at the top of the layout.
     */
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun pagedFlingBehavior(state: LazyListState): FlingBehavior {
        val snappingLayout = remember(state) {
            SnapLayoutInfoProvider(state) { _, _ -> 0f }
        }
        return rememberSnapFlingBehavior(snappingLayout)
    }

    @Composable
    private fun continuousFlingBehavior(): FlingBehavior = ScrollableDefaults.flingBehavior()

    @Composable
    fun flingBehavior(isPaged: Boolean, state: LazyListState): FlingBehavior {
        return if (isPaged) pagedFlingBehavior(state) else continuousFlingBehavior()
    }
}
