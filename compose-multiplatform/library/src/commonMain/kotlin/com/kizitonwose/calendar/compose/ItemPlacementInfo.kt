package com.kizitonwose.calendar.compose

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Stable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.round
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

@Stable
internal class ItemPlacementInfo {
    private var calendarCoordinates: LayoutCoordinates? = null
    private var firstDayCoordinates: LayoutCoordinates? = null

    fun onCalendarPlaced(coordinates: LayoutCoordinates) {
        calendarCoordinates = coordinates
    }

    fun onFirstDayPlaced(coordinates: LayoutCoordinates) {
        firstDayCoordinates = coordinates
    }

    suspend fun awaitFistDayOffsetAndSize(orientation: Orientation): DayOffsetSize? {
        var calendarCoord: LayoutCoordinates? = null
        var firstDayCoord: LayoutCoordinates? = null
        while (coroutineContext.isActive &&
            (calendarCoord == null || firstDayCoord == null)
        ) {
            calendarCoord = calendarCoordinates
            firstDayCoord = firstDayCoordinates
            if (calendarCoord == null || firstDayCoord == null) {
                withFrameNanos {}
            }
        }
        if (calendarCoord == null ||
            firstDayCoord == null ||
            !calendarCoord.isAttached ||
            !firstDayCoord.isAttached
        ) {
            return null
        }
        val itemViewCoord = findItemViewCoordinates(firstDayCoord, calendarCoord)
        val daySize = firstDayCoord.size
        val dayOffset = itemViewCoord.localPositionOf(firstDayCoord, Offset.Zero).round()
        return when (orientation) {
            Orientation.Vertical -> DayOffsetSize(
                offset = dayOffset.y,
                size = daySize.height,
            )

            Orientation.Horizontal -> {
                DayOffsetSize(
                    offset = dayOffset.x,
                    size = daySize.width,
                )
            }
        }
    }

    internal data class DayOffsetSize(
        val offset: Int,
        val size: Int,
    )
}

internal fun findItemViewCoordinates(
    firstDayCoord: LayoutCoordinates,
    calendarCoord: LayoutCoordinates,
): LayoutCoordinates {
    var itemViewCoord = firstDayCoord
    var parent = itemViewCoord.parentLayoutCoordinates
    // Find the coordinates the match the index item layout
    while (parent != null &&
        parent.size != calendarCoord.size &&
        parent.positionInWindow() != calendarCoord.positionInWindow()
    ) {
        itemViewCoord = parent
        parent = itemViewCoord.parentLayoutCoordinates
    }
    return itemViewCoord
}
