package com.kizitonwose.calendar.compose.yearcalendar

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.round
import com.kizitonwose.calendar.compose.findItemViewCoordinates
import com.kizitonwose.calendar.core.CalendarMonth
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

@Stable
internal class YearItemPlacementInfo {
    private var calendarCoordinates: LayoutCoordinates? = null
    private var firstDayCoordinates: LayoutCoordinates? = null
    private var firstMonthCoordinates: LayoutCoordinates? = null

    internal var isMonthVisible: ((month: CalendarMonth) -> Boolean)? = null
    internal var monthVerticalSpacingPx: Int = 0
    internal var monthHorizontalSpacingPx: Int = 0
    internal var monthColumns: Int = 0

    fun onCalendarPlaced(coordinates: LayoutCoordinates) {
        calendarCoordinates = coordinates
    }

    fun onFirstMonthAndDayPlaced(month: LayoutCoordinates, day: LayoutCoordinates) {
        firstMonthCoordinates = month
        firstDayCoordinates = day
    }

    suspend fun awaitFistMonthAndDayOffsetAndSize(orientation: Orientation): OffsetSize? {
        var calendarCoord: LayoutCoordinates? = null
        var firstDayCoord: LayoutCoordinates? = null
        var firstMonthCoord: LayoutCoordinates? = null
        while (coroutineContext.isActive &&
            (calendarCoord == null ||
                firstDayCoord == null ||
                firstMonthCoord == null)
        ) {
            calendarCoord = calendarCoordinates
            firstDayCoord = firstDayCoordinates
            firstMonthCoord = firstMonthCoordinates
            // day and month coord are set at the same time but check anyway
            if (calendarCoord == null ||
                firstDayCoord == null ||
                firstMonthCoord == null
            ) {
                awaitFrame()
            }
        }
        if (calendarCoord == null ||
            firstDayCoord == null ||
            firstMonthCoord == null ||
            !calendarCoord.isAttached ||
            !firstDayCoord.isAttached ||
            !firstMonthCoord.isAttached
        ) {
            return null
        }
        val itemViewCoord = findItemViewCoordinates(firstDayCoord, calendarCoord)
        val daySize = firstDayCoord.size
        val monthOffset = itemViewCoord.localPositionOf(firstMonthCoord, Offset.Zero).round()
        val dayOffsetInMonth = firstMonthCoord.localPositionOf(firstDayCoord, Offset.Zero).round()
        val monthSize = firstMonthCoord.size
        return when (orientation) {
            Orientation.Vertical -> OffsetSize(
                monthOffsetInContainer = monthOffset.y,
                monthSize = monthSize.height,
                dayOffsetInMonth = dayOffsetInMonth.y,
                daySize = daySize.height,
            )

            Orientation.Horizontal -> {
                OffsetSize(
                    monthOffsetInContainer = monthOffset.x,
                    monthSize = monthSize.width,
                    dayOffsetInMonth = dayOffsetInMonth.x,
                    daySize = daySize.width,
                )
            }
        }
    }

    internal data class OffsetSize(
        val monthSize: Int,
        val monthOffsetInContainer: Int,
        val dayOffsetInMonth: Int,
        val daySize: Int,
    )
}
