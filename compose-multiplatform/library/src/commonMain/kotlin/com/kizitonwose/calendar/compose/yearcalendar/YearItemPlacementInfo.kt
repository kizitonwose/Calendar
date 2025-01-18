package com.kizitonwose.calendar.compose.yearcalendar

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.round
import com.kizitonwose.calendar.core.CalendarMonth
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

@Immutable
internal data class YearItemCoordinates(
    val firstMonth: CalendarMonth,
    val itemRootCoordinates: LayoutCoordinates,
    val firstMonthCoordinates: LayoutCoordinates,
    val firstDayCoordinates: LayoutCoordinates,
)

@Stable
internal class YearItemPlacementInfo {
    private var itemCoordinates: YearItemCoordinates? = null

    internal var isMonthVisible: ((month: CalendarMonth) -> Boolean)? = null
    internal var monthVerticalSpacingPx = 0
    internal var monthHorizontalSpacingPx = 0
    internal var monthColumns = 0
    internal var contentHeightMode = YearContentHeightMode.Wrap

    fun onItemPlaced(itemCoordinates: YearItemCoordinates) {
        this.itemCoordinates = itemCoordinates
    }

    suspend fun awaitFistMonthDayOffsetAndSize(orientation: Orientation): OffsetSize? {
        var itemCoordinates = this.itemCoordinates
        while (coroutineContext.isActive && itemCoordinates == null) {
            withFrameNanos {}
            itemCoordinates = this.itemCoordinates
        }
        if (itemCoordinates == null) {
            return null
        }
        val (firstMonth, itemRootCoordinates, firstMonthCoordinates, firstDayCoordinates) = itemCoordinates
        val daySize = firstDayCoordinates.size
        val monthOffset = itemRootCoordinates.localPositionOf(firstMonthCoordinates).round()
        val dayOffsetInMonth = firstMonthCoordinates.localPositionOf(firstDayCoordinates).round()
        val monthSize = firstMonthCoordinates.size
        return when (orientation) {
            Orientation.Vertical -> OffsetSize(
                monthOffsetInContainer = monthOffset.y,
                monthSize = monthSize.height,
                monthSpacing = monthVerticalSpacingPx,
                dayOffsetInMonth = dayOffsetInMonth.y,
                daySize = daySize.height,
                dayBodyCount = firstMonth.weekDays.size,
            )

            Orientation.Horizontal -> {
                OffsetSize(
                    monthOffsetInContainer = monthOffset.x,
                    monthSize = monthSize.width,
                    monthSpacing = monthHorizontalSpacingPx,
                    dayOffsetInMonth = dayOffsetInMonth.x,
                    daySize = daySize.width,
                    dayBodyCount = firstMonth.weekDays.first().size,
                )
            }
        }
    }

    @Immutable
    internal data class OffsetSize(
        val monthSize: Int,
        val monthOffsetInContainer: Int,
        val monthSpacing: Int,
        val dayOffsetInMonth: Int,
        val daySize: Int,
        val dayBodyCount: Int,
    )
}
