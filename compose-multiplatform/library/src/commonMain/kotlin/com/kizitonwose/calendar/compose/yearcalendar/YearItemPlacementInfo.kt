package com.kizitonwose.calendar.compose.yearcalendar

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Stable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.round
import com.kizitonwose.calendar.compose.findItemViewCoordinates
import com.kizitonwose.calendar.core.CalendarMonth
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

private typealias FirstMonthDayCoordinates = Triple<CalendarMonth, LayoutCoordinates, LayoutCoordinates>

@Stable
internal class YearItemPlacementInfo {
    private var calendarCoordinates: LayoutCoordinates? = null
    private var firstMonthDayCoordinates: FirstMonthDayCoordinates? = null

    internal var isMonthVisible: ((month: CalendarMonth) -> Boolean)? = null
    internal var monthVerticalSpacingPx = 0
    internal var monthHorizontalSpacingPx = 0
    internal var monthColumns = 0
    internal var contentHeightMode = YearContentHeightMode.Wrap

    fun onCalendarPlaced(coordinates: LayoutCoordinates) {
        calendarCoordinates = coordinates
    }

    fun onFirstMonthAndDayPlaced(
        month: CalendarMonth,
        monthCoordinates: LayoutCoordinates,
        dayCoordinates: LayoutCoordinates,
    ) {
        firstMonthDayCoordinates = Triple(
            first = month,
            second = monthCoordinates,
            third = dayCoordinates,
        )
    }

    suspend fun awaitFistMonthDayOffsetAndSize(orientation: Orientation): OffsetSize? {
        var calendarCoord: LayoutCoordinates? = null
        var firstMonthDayCoord: FirstMonthDayCoordinates? = null
        while (coroutineContext.isActive &&
            (calendarCoord == null || firstMonthDayCoord == null)
        ) {
            calendarCoord = calendarCoordinates
            firstMonthDayCoord = firstMonthDayCoordinates
            // day and month coord are set at the same time but check anyway
            if (calendarCoord == null || firstMonthDayCoord == null) {
                withFrameNanos {}
            }
        }
        if (calendarCoord == null ||
            firstMonthDayCoord == null ||
            !calendarCoord.isAttached ||
            !firstMonthDayCoord.second.isAttached ||
            !firstMonthDayCoord.third.isAttached
        ) {
            return null
        }
        val (month, firstMonthCoord, firstDayCoord) = firstMonthDayCoord
        val itemViewCoord = findItemViewCoordinates(firstDayCoord, calendarCoord)
        val daySize = firstDayCoord.size
        val monthOffset = itemViewCoord.localPositionOf(firstMonthCoord, Offset.Zero).round()
        val dayOffsetInMonth = firstMonthCoord.localPositionOf(firstDayCoord, Offset.Zero).round()
        val monthSize = firstMonthCoord.size
        return when (orientation) {
            Orientation.Vertical -> OffsetSize(
                monthOffsetInContainer = monthOffset.y,
                monthSize = monthSize.height,
                monthSpacing = monthVerticalSpacingPx,
                dayOffsetInMonth = dayOffsetInMonth.y,
                daySize = daySize.height,
                dayBodyCount = month.weekDays.size,
            )

            Orientation.Horizontal -> {
                OffsetSize(
                    monthOffsetInContainer = monthOffset.x,
                    monthSize = monthSize.width,
                    monthSpacing = monthHorizontalSpacingPx,
                    dayOffsetInMonth = dayOffsetInMonth.x,
                    daySize = daySize.width,
                    dayBodyCount = month.weekDays.first().size,
                )
            }
        }
    }

    internal data class OffsetSize(
        val monthSize: Int,
        val monthOffsetInContainer: Int,
        val monthSpacing: Int,
        val dayOffsetInMonth: Int,
        val daySize: Int,
        val dayBodyCount: Int,
    )
}
