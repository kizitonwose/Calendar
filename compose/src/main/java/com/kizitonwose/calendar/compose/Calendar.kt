package com.kizitonwose.calendar.compose

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.CalendarDefaults.flingBehavior
import com.kizitonwose.calendar.compose.heatmapcalendar.HeatMapCalendarImpl
import com.kizitonwose.calendar.compose.heatmapcalendar.HeatMapCalendarState
import com.kizitonwose.calendar.compose.heatmapcalendar.HeatMapWeek
import com.kizitonwose.calendar.compose.heatmapcalendar.HeatMapWeekHeaderPosition
import com.kizitonwose.calendar.compose.heatmapcalendar.rememberHeatMapCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.WeekCalendarImpl
import com.kizitonwose.calendar.compose.weekcalendar.WeekCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.WeekDay
import java.time.DayOfWeek

/**
 * A horizontally scrolling calendar.
 *
 * @param modifier the modifier to apply to this calendar.
 * @param state the state object to be used to control or observe the calendar's properties.
 * Examples: `startMonth`, `endMonth`, `firstDayOfWeek`, `firstVisibleMonth`, `outDateStyle`.
 * @param calendarScrollPaged the scrolling behavior of the calendar. When `true`, the calendar will
 * snap to the nearest month after a scroll or swipe action. When `false`, the calendar scrolls normally.
 * @param userScrollEnabled whether the scrolling via the user gestures or accessibility actions
 * is allowed. You can still scroll programmatically using the state even when it is disabled.
 * @param reverseLayout reverse the direction of scrolling and layout. When `true`, months will be
 * composed from the end to the start and [CalendarState.startMonth] will be located at the end.
 * @param contentPadding a padding around the whole calendar. This will add padding for the
 * content after it has been clipped, which is not possible via [modifier] param. You can use it
 * to add a padding before the first month or after the last one. If you want to add a spacing
 * between each month use the [monthContainer] composable.
 * @param contentHeightMode Determines how the height of the day content is calculated.
 * @param dayContent a composable block which describes the day content.
 * @param monthHeader a composable block which describes the month header content. The header is
 * placed above each month on the calendar.
 * @param monthBody a composable block which describes the month body content. This is the container
 * where all the month days are placed, excluding the header and footer. This is useful if you
 * want to customize the day container, for example, with a background color or other effects.
 * The actual body content is provided in the block and must be called after your desired
 * customisations are rendered.
 * @param monthFooter a composable block which describes the month footer content. The footer is
 * placed below each month on the calendar.
 * @param monthContainer a composable block which describes the entire month content. This is the
 * container where all the month contents are placed (header => days => footer). This is useful if
 * you want to customize the month container, for example, with a background color or other effects.
 * The actual container content is provided in the block and must be called after your desired
 * customisations are rendered.
 */
@Composable
fun HorizontalCalendar(
    modifier: Modifier = Modifier,
    state: CalendarState = rememberCalendarState(),
    calendarScrollPaged: Boolean = true,
    userScrollEnabled: Boolean = true,
    reverseLayout: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    contentHeightMode: ContentHeightMode = ContentHeightMode.Wrap,
    dayContent: @Composable BoxScope.(CalendarDay) -> Unit,
    monthHeader: (@Composable ColumnScope.(CalendarMonth) -> Unit)? = null,
    monthBody: (@Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit)? = null,
    monthFooter: (@Composable ColumnScope.(CalendarMonth) -> Unit)? = null,
    monthContainer: (@Composable LazyItemScope.(CalendarMonth, container: @Composable () -> Unit) -> Unit)? = null,
) = Calendar(
    modifier = modifier,
    state = state,
    calendarScrollPaged = calendarScrollPaged,
    userScrollEnabled = userScrollEnabled,
    isHorizontal = true,
    reverseLayout = reverseLayout,
    contentHeightMode = contentHeightMode,
    dayContent = dayContent,
    monthHeader = monthHeader,
    monthBody = monthBody,
    monthFooter = monthFooter,
    monthContainer = monthContainer,
    contentPadding = contentPadding,
)

/**
 * A vertically scrolling calendar.
 *
 * @param modifier the modifier to apply to this calendar.
 * @param state the state object to be used to control or observe the calendar's properties.
 * Examples: `startMonth`, `endMonth`, `firstDayOfWeek`, `firstVisibleMonth`, `outDateStyle`.
 * @param calendarScrollPaged the scrolling behavior of the calendar. When `true`, the calendar will
 * snap to the nearest month after a scroll or swipe action. When `false`, the calendar scrolls normally.
 * @param userScrollEnabled whether the scrolling via the user gestures or accessibility actions
 * is allowed. You can still scroll programmatically using the state even when it is disabled.
 * @param reverseLayout reverse the direction of scrolling and layout. When `true`, months will be
 * composed from the end to the start and [CalendarState.startMonth] will be located at the end.
 * @param contentPadding a padding around the whole calendar. This will add padding for the
 * content after it has been clipped, which is not possible via [modifier] param. You can use it
 * to add a padding before the first month or after the last one. If you want to add a spacing
 * between each month use the [monthContainer] composable.
 * @param contentHeightMode Determines how the height of the day content is calculated.
 * @param dayContent a composable block which describes the day content.
 * @param monthHeader a composable block which describes the month header content. The header is
 * placed above each month on the calendar.
 * @param monthBody a composable block which describes the month body content. This is the container
 * where all the month days are placed, excluding the header and footer. This is useful if you
 * want to customize the day container, for example, with a background color or other effects.
 * The actual body content is provided in the block and must be called after your desired
 * customisations are rendered.
 * @param monthFooter a composable block which describes the month footer content. The footer is
 * placed below each month on the calendar.
 * @param monthContainer a composable block which describes the entire month content. This is the
 * container where all the month contents are placed (header => days => footer). This is useful if
 * you want to customize the month container, for example, with a background color or other effects.
 * The actual container content is provided in the block and must be called after your desired
 * customisations are rendered.
 */
@Composable
fun VerticalCalendar(
    modifier: Modifier = Modifier,
    state: CalendarState = rememberCalendarState(),
    calendarScrollPaged: Boolean = false,
    userScrollEnabled: Boolean = true,
    reverseLayout: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    contentHeightMode: ContentHeightMode = ContentHeightMode.Wrap,
    dayContent: @Composable BoxScope.(CalendarDay) -> Unit,
    monthHeader: (@Composable ColumnScope.(CalendarMonth) -> Unit)? = null,
    monthBody: (@Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit)? = null,
    monthFooter: (@Composable ColumnScope.(CalendarMonth) -> Unit)? = null,
    monthContainer: (@Composable LazyItemScope.(CalendarMonth, container: @Composable () -> Unit) -> Unit)? = null,
) = Calendar(
    modifier = modifier,
    state = state,
    calendarScrollPaged = calendarScrollPaged,
    userScrollEnabled = userScrollEnabled,
    isHorizontal = false,
    reverseLayout = reverseLayout,
    contentHeightMode = contentHeightMode,
    dayContent = dayContent,
    monthHeader = monthHeader,
    monthBody = monthBody,
    monthFooter = monthFooter,
    monthContainer = monthContainer,
    contentPadding = contentPadding,
)

@Composable
private fun Calendar(
    modifier: Modifier,
    state: CalendarState,
    calendarScrollPaged: Boolean,
    userScrollEnabled: Boolean,
    isHorizontal: Boolean,
    reverseLayout: Boolean,
    contentPadding: PaddingValues,
    contentHeightMode: ContentHeightMode,
    dayContent: @Composable BoxScope.(CalendarDay) -> Unit,
    monthHeader: (@Composable ColumnScope.(CalendarMonth) -> Unit)?,
    monthBody: (@Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit)?,
    monthFooter: (@Composable ColumnScope.(CalendarMonth) -> Unit)?,
    monthContainer: (@Composable LazyItemScope.(CalendarMonth, container: @Composable () -> Unit) -> Unit)?,
) {
    if (isHorizontal) {
        LazyRow(
            modifier = modifier,
            state = state.listState,
            flingBehavior = flingBehavior(calendarScrollPaged, state.listState),
            userScrollEnabled = userScrollEnabled,
            reverseLayout = reverseLayout,
            contentPadding = contentPadding,
        ) {
            CalendarMonths(
                monthCount = state.monthIndexCount,
                monthData = { offset -> state.store[offset] },
                contentHeightMode = contentHeightMode,
                dayContent = dayContent,
                monthHeader = monthHeader,
                monthBody = monthBody,
                monthFooter = monthFooter,
                monthContainer = monthContainer,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            state = state.listState,
            flingBehavior = flingBehavior(calendarScrollPaged, state.listState),
            userScrollEnabled = userScrollEnabled,
            reverseLayout = reverseLayout,
            contentPadding = contentPadding,
        ) {
            CalendarMonths(
                monthCount = state.monthIndexCount,
                monthData = { offset -> state.store[offset] },
                contentHeightMode = contentHeightMode,
                dayContent = dayContent,
                monthHeader = monthHeader,
                monthBody = monthBody,
                monthFooter = monthFooter,
                monthContainer = monthContainer,
            )
        }
    }
}

/**
 * A horizontally scrolling week calendar.
 *
 * @param modifier the modifier to apply to this calendar.
 * @param state the state object to be used to control or observe the calendar's properties.
 * Examples: `startDate`, `endDate`, `firstDayOfWeek`, `firstVisibleWeek`.
 * @param calendarScrollPaged the scrolling behavior of the calendar. When `true`, the calendar will
 * snap to the nearest week after a scroll or swipe action. When `false`, the calendar scrolls normally.
 * Note that when `false`, you should set the desired day width on the [dayContent] composable.
 * @param userScrollEnabled whether the scrolling via the user gestures or accessibility actions
 * is allowed. You can still scroll programmatically using the state even when it is disabled.
 * @param reverseLayout reverse the direction of scrolling and layout. When `true`, weeks will be
 * composed from the end to the start and [WeekCalendarState.startDate] will be located at the end.
 * @param contentPadding a padding around the whole calendar. This will add padding for the
 * content after it has been clipped, which is not possible via [modifier] param. You can use it
 * to add a padding before the first week or after the last one.
 * @param dayContent a composable block which describes the day content.
 * @param weekHeader a composable block which describes the week header content. The header is
 * placed above each week on the calendar.
 * @param weekFooter a composable block which describes the week footer content. The footer is
 * placed below each week on the calendar.
 */
@Composable
fun WeekCalendar(
    modifier: Modifier = Modifier,
    state: WeekCalendarState = rememberWeekCalendarState(),
    calendarScrollPaged: Boolean = true,
    userScrollEnabled: Boolean = true,
    reverseLayout: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    dayContent: @Composable BoxScope.(WeekDay) -> Unit,
    weekHeader: (@Composable ColumnScope.(Week) -> Unit)? = null,
    weekFooter: (@Composable ColumnScope.(Week) -> Unit)? = null,
) = WeekCalendarImpl(
    modifier = modifier,
    state = state,
    calendarScrollPaged = calendarScrollPaged,
    userScrollEnabled = userScrollEnabled,
    reverseLayout = reverseLayout,
    dayContent = dayContent,
    weekHeader = weekHeader,
    weekFooter = weekFooter,
    contentPadding = contentPadding,
)

/**
 * A horizontal scrolling heatmap calendar, useful for showing how data changes over time.
 * A popular example is the user contribution chart on GitHub.
 *
 * @param modifier the modifier to apply to this calendar.
 * @param state the state object to be used to control or observe the calendar's properties.
 * Examples: `startMonth`, `endMonth`, `firstDayOfWeek`, `firstVisibleMonth`.
 * @param weekHeaderPosition Determines the position for the [weekHeader] composable.
 *  @param userScrollEnabled whether the scrolling via the user gestures or accessibility actions
 * is allowed. You can still scroll programmatically using the state even when it is disabled.
 * @param contentPadding a padding around the whole calendar. This will add padding for the
 * content after it has been clipped, which is not possible via [modifier] param. You can use it
 * to add a padding before the first month or after the last one.
 * @param dayContent a composable block which describes the day content.
 * @param weekHeader a composable block which describes the day of week (Mon, Tue, Wed...) on the
 * horizontal axis of the calendar. The position is determined by the [weekHeaderPosition] property.
 * @param monthHeader a composable block which describes the month header content. The header is
 * placed above each month on the calendar.
 */
@Composable
fun HeatMapCalendar(
    modifier: Modifier = Modifier,
    state: HeatMapCalendarState = rememberHeatMapCalendarState(),
    weekHeaderPosition: HeatMapWeekHeaderPosition = HeatMapWeekHeaderPosition.Start,
    userScrollEnabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    dayContent: @Composable ColumnScope.(day: CalendarDay, week: HeatMapWeek) -> Unit,
    weekHeader: (@Composable ColumnScope.(DayOfWeek) -> Unit)? = null,
    monthHeader: (@Composable ColumnScope.(CalendarMonth) -> Unit)? = null,
) = HeatMapCalendarImpl(
    modifier = modifier,
    state = state,
    weekHeaderPosition = weekHeaderPosition,
    userScrollEnabled = userScrollEnabled,
    dayContent = dayContent,
    weekHeader = weekHeader,
    monthHeader = monthHeader,
    contentPadding = contentPadding,
)
