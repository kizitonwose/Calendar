package com.kizitonwose.calendar.compose

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.round
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

@Immutable
internal data class ItemCoordinates(
    val itemRootCoordinates: LayoutCoordinates,
    val firstDayCoordinates: LayoutCoordinates,
)

@Stable
internal class ItemPlacementInfo {
    private var itemCoordinates: ItemCoordinates? = null

    fun onItemPlaced(itemCoordinates: ItemCoordinates) {
        this.itemCoordinates = itemCoordinates
    }

    suspend fun awaitFistDayOffsetAndSize(orientation: Orientation): OffsetSize? {
        var itemCoordinates = this.itemCoordinates
        while (coroutineContext.isActive && itemCoordinates == null) {
            withFrameNanos {}
            itemCoordinates = this.itemCoordinates
        }
        if (itemCoordinates == null) {
            return null
        }
        val (itemRootCoordinates, firstDayCoordinates) = itemCoordinates
        val daySize = firstDayCoordinates.size
        val dayOffset = itemRootCoordinates.localPositionOf(firstDayCoordinates).round()
        return when (orientation) {
            Orientation.Vertical -> OffsetSize(
                offset = dayOffset.y,
                size = daySize.height,
            )

            Orientation.Horizontal -> {
                OffsetSize(
                    offset = dayOffset.x,
                    size = daySize.width,
                )
            }
        }
    }

    @Immutable
    internal data class OffsetSize(
        val offset: Int,
        val size: Int,
    )
}
