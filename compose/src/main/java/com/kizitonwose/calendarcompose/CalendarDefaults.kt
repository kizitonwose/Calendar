package com.kizitonwose.calendarcompose

import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import dev.chrisbanes.snapper.*

internal object CalendarDefaults {
    /**
     * The implementation for the `snapIndex` parameter of
     * [pagedFlingBehavior] which limits the fling distance to a single page.
     */
    @OptIn(ExperimentalSnapperApi::class)
    private val singlePageSnapIndex: (SnapperLayoutInfo, startIndex: Int, targetIndex: Int) -> Int =
        { layoutInfo, startIndex, targetIndex ->
            targetIndex
                .coerceIn(startIndex - 1, startIndex + 1)
                .coerceIn(0, layoutInfo.totalItemsCount - 1)
        }

    @Composable
    @ExperimentalSnapperApi
    private fun pagedFlingBehavior(state: LazyListState): FlingBehavior =
        rememberSnapperFlingBehavior(
            lazyListState = state,
            snapOffsetForItem = SnapOffsets.Start,
            springAnimationSpec = SnapperFlingBehaviorDefaults.SpringAnimationSpec,
            decayAnimationSpec = rememberSplineBasedDecay(),
            snapIndex = singlePageSnapIndex,
        )

    @Composable
    private fun continuousFlingBehavior(): FlingBehavior = ScrollableDefaults.flingBehavior()

    @OptIn(ExperimentalSnapperApi::class)
    @Composable
    fun flingBehavior(isPaged: Boolean, state: LazyListState): FlingBehavior {
        return if (isPaged) pagedFlingBehavior(state) else continuousFlingBehavior()
    }
}
