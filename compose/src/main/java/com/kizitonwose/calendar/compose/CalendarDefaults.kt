package com.kizitonwose.calendar.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Density

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
            val provider = SnapLayoutInfoProvider(state) { _, _ -> 0f }
            CalendarSnapLayoutInfoProvider(provider)
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

@ExperimentalFoundationApi
@Suppress("FunctionName")
private fun CalendarSnapLayoutInfoProvider(snapLayoutInfoProvider: SnapLayoutInfoProvider):
    SnapLayoutInfoProvider = object : SnapLayoutInfoProvider by snapLayoutInfoProvider {

    /**
     * In compose 1.3, the default was single page snapping (zero), but this changed
     * in compose 1.4 to decayed page snapping which is not great for calendar usage.
     */
    override fun Density.calculateApproachOffset(initialVelocity: Float): Float = 0f
}
