package com.kizitonwose.calendar.compose.weekcalendar

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.kizitonwose.calendar.compose.ItemPlacementInfo
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.WeekDayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.format.fromIso8601LocalDate
import com.kizitonwose.calendar.core.format.toIso8601String
import com.kizitonwose.calendar.core.now
import com.kizitonwose.calendar.data.DataStore
import com.kizitonwose.calendar.data.VisibleItemState
import com.kizitonwose.calendar.data.checkRange
import com.kizitonwose.calendar.data.daysUntil
import com.kizitonwose.calendar.data.getWeekCalendarAdjustedRange
import com.kizitonwose.calendar.data.getWeekCalendarData
import com.kizitonwose.calendar.data.getWeekIndex
import com.kizitonwose.calendar.data.getWeekIndicesCount
import com.kizitonwose.calendar.data.log
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/**
 * Creates a [WeekCalendarState] that is remembered across compositions.
 *
 * @param startDate the initial value for [WeekCalendarState.startDate]
 * @param endDate the initial value for [WeekCalendarState.endDate]
 * @param firstDayOfWeek the initial value for [WeekCalendarState.firstDayOfWeek]
 * @param firstVisibleWeekDate the date which will have its week visible initially.
 */
@Composable
public fun rememberWeekCalendarState(
    startDate: LocalDate = YearMonth.now().firstDay,
    endDate: LocalDate = YearMonth.now().lastDay,
    firstVisibleWeekDate: LocalDate = LocalDate.now(),
    firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale(),
): WeekCalendarState {
    return rememberSaveable(
        inputs = arrayOf<Any>(
            startDate,
            endDate,
            firstVisibleWeekDate,
            firstDayOfWeek,
        ),
        saver = WeekCalendarState.Saver,
    ) {
        WeekCalendarState(
            startDate = startDate,
            endDate = endDate,
            firstVisibleWeekDate = firstVisibleWeekDate,
            firstDayOfWeek = firstDayOfWeek,
            visibleItemState = null,
        )
    }
}

/**
 * A state object that can be hoisted to control and observe calendar properties.
 *
 * This should be created via [rememberWeekCalendarState].
 *
 * @param startDate the desired first date on the calendar. The actual first date will be the
 * first day in the week to which this date belongs, depending on the provided [firstDayOfWeek].
 * Such days will have their [WeekDayPosition] set to [WeekDayPosition.InDate].
 * @param endDate the desired last date on the calendar. The actual last date will be the last
 * day in the week to which this date belongs. Such days will have their [WeekDayPosition] set
 * to [WeekDayPosition.OutDate].
 * @param firstDayOfWeek the first day of week on the calendar.
 * @param firstVisibleWeekDate the date which will have its week visible initially.
 */
@Stable
public class WeekCalendarState internal constructor(
    startDate: LocalDate,
    endDate: LocalDate,
    firstVisibleWeekDate: LocalDate,
    firstDayOfWeek: DayOfWeek,
    visibleItemState: VisibleItemState?,
) : ScrollableState {
    /**
     * The adjusted first date on the calendar to ensure proper alignment
     * of the provided [firstDayOfWeek].
     */
    private var startDateAdjusted by mutableStateOf(startDate)

    /**
     * The adjusted last date on the calendar to fill the remaining days in the
     * last week after the provided end date.
     */
    private var endDateAdjusted by mutableStateOf(endDate)

    /** Backing state for [startDate] */
    private var _startDate by mutableStateOf(startDate)

    /**
     * The desired first date on the calendar. The actual first date will be the first day
     * in the week to which this date belongs, depending on the provided [firstDayOfWeek].
     * Such days will have their [WeekDayPosition] set to [WeekDayPosition.InDate]
     */
    public var startDate: LocalDate
        get() = _startDate
        set(value) {
            if (value != _startDate) {
                _startDate = value
                adjustDateRange()
            }
        }

    /** Backing state for [endDate] */
    private var _endDate by mutableStateOf(endDate)

    /**
     * The desired last date on the calendar. The actual last date will be the last day
     * in the week to which this date belongs. Such days will have their [WeekDayPosition]
     * set to [WeekDayPosition.OutDate]
     */
    public var endDate: LocalDate
        get() = _endDate
        set(value) {
            if (value != _endDate) {
                _endDate = value
                adjustDateRange()
            }
        }

    /** Backing state for [firstDayOfWeek] */
    private var _firstDayOfWeek by mutableStateOf(firstDayOfWeek)

    /** The first day of week on the calendar. */
    public var firstDayOfWeek: DayOfWeek
        get() = _firstDayOfWeek
        set(value) {
            if (value != _firstDayOfWeek) {
                _firstDayOfWeek = value
                adjustDateRange()
            }
        }

    /**
     * The first week that is visible.
     */
    public val firstVisibleWeek: Week by derivedStateOf {
        store[listState.firstVisibleItemIndex]
    }

    /**
     * The last week that is visible.
     */
    public val lastVisibleWeek: Week by derivedStateOf {
        store[listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0]
    }

    /**
     * The object of [WeekCalendarLayoutInfo] calculated during the last layout pass. For example,
     * you can use it to calculate what items are currently visible.
     *
     * Note that this property is observable and is updated after every scroll or remeasure.
     * If you use it in the composable function it will be recomposed on every change causing
     * potential performance issues including infinity recomposition loop.
     * Therefore, avoid using it in the composition.
     *
     * If you need to use it in the composition then consider wrapping the calculation into a
     * derived state in order to only have recompositions when the derived value changes.
     * See Example5Page in the sample app for usage.
     *
     * If you want to run some side effects like sending an analytics event or updating a state
     * based on this value consider using "snapshotFlow".
     */
    public val layoutInfo: WeekCalendarLayoutInfo
        get() = WeekCalendarLayoutInfo(listState.layoutInfo) { index -> store[index] }

    internal val placementInfo = ItemPlacementInfo()

    internal val store = DataStore { offset ->
        getWeekCalendarData(
            startDateAdjusted = this.startDateAdjusted,
            offset = offset,
            desiredStartDate = this.startDate,
            desiredEndDate = this.endDate,
        ).week
    }

    internal var weekIndexCount by mutableIntStateOf(0)

    internal val listState = run {
        // Update date range and weekIndexCount initially.
        // Since getScrollIndex requires the adjusted start date, it is necessary to do this
        // before finding the first visible index.
        adjustDateRange()
        val item = visibleItemState ?: run {
            VisibleItemState(firstVisibleItemIndex = getScrollIndex(firstVisibleWeekDate) ?: 0)
        }
        LazyListState(
            firstVisibleItemIndex = item.firstVisibleItemIndex,
            firstVisibleItemScrollOffset = item.firstVisibleItemScrollOffset,
        )
    }

    private fun adjustDateRange() {
        checkRange(startDate, endDate)
        val data = getWeekCalendarAdjustedRange(startDate, endDate, firstDayOfWeek)
        startDateAdjusted = data.startDateAdjusted
        endDateAdjusted = data.endDateAdjusted
        store.clear()
        weekIndexCount = getWeekIndicesCount(startDateAdjusted, endDateAdjusted)
    }

    /**
     * Instantly brings the week containing the given [date] to the top of the viewport.
     *
     * @param date the week to which to scroll.
     *
     * @see [animateScrollToWeek]
     */
    public suspend fun scrollToWeek(date: LocalDate) {
        listState.scrollToItem(getScrollIndex(date) ?: return)
    }

    /**
     * Animate (smooth scroll) to the week containing the given [date].
     *
     * @param date the week to which to scroll.
     *
     * @see [scrollToWeek]
     */
    public suspend fun animateScrollToWeek(date: LocalDate) {
        listState.animateScrollToItem(getScrollIndex(date) ?: return)
    }

    /**
     * Instantly brings the week containing the given [day] to the top of the viewport.
     *
     * @param day the week to which to scroll.
     *
     * @see [animateScrollToWeek]]
     */
    public suspend fun scrollToWeek(day: WeekDay): Unit = scrollToWeek(day.date)

    /**
     * Animate (smooth scroll) to the week containing the given [day].
     *
     * @param day the week to which to scroll.
     *
     * @see [scrollToWeek]
     */
    public suspend fun animateScrollToWeek(day: WeekDay): Unit = animateScrollToWeek(day.date)

    /**
     * Instantly brings the [date] to the top of the viewport.
     *
     * @param date the date to which to scroll.
     *
     * @see [animateScrollToDate]
     */
    public suspend fun scrollToDate(date: LocalDate): Unit = scrollToDate(date, animate = false)

    /**
     * Animate (smooth scroll) to the given [date].
     *
     * @param date the date to which to scroll.
     *
     * @see [scrollToDate]
     */
    public suspend fun animateScrollToDate(date: LocalDate): Unit = scrollToDate(date, animate = true)

    /**
     * Instantly brings the [day] to the top of the viewport.
     *
     * @param day the day to which to scroll.
     *
     * @see [animateScrollToDay]
     */
    public suspend fun scrollToDay(day: WeekDay): Unit = scrollToDate(day.date)

    /**
     * Animate (smooth scroll) to the given [day].
     *
     * @param day the day to which to scroll.
     *
     * @see [scrollToDay]
     */
    public suspend fun animateScrollToDay(day: WeekDay): Unit = animateScrollToDate(day.date)

    private suspend fun scrollToDate(date: LocalDate, animate: Boolean) {
        val weekIndex = getScrollIndex(date) ?: return
        val dayIndex = when (layoutInfo.orientation) {
            Orientation.Vertical -> 0
            Orientation.Horizontal -> firstDayOfWeek.daysUntil(date.dayOfWeek)
        }
        val dayInfo = placementInfo.awaitFistDayOffsetAndSize(layoutInfo.orientation) ?: return
        val scrollOffset = dayInfo.offset + dayInfo.size * dayIndex
        if (animate) {
            listState.animateScrollToItem(weekIndex, scrollOffset)
        } else {
            listState.scrollToItem(weekIndex, scrollOffset)
        }
    }

    /**
     * [InteractionSource] that will be used to dispatch drag events when this
     * calendar is being dragged. If you want to know whether the fling (or animated scroll) is in
     * progress, use [isScrollInProgress].
     */
    public val interactionSource: InteractionSource
        get() = listState.interactionSource

    /**
     * Whether this [ScrollableState] is currently scrolling by gesture, fling or programmatically.
     */
    override val isScrollInProgress: Boolean
        get() = listState.isScrollInProgress

    override fun dispatchRawDelta(delta: Float): Float = listState.dispatchRawDelta(delta)

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit,
    ): Unit = listState.scroll(scrollPriority, block)

    private fun getScrollIndex(date: LocalDate): Int? {
        if (date !in startDateAdjusted..endDateAdjusted) {
            log("WeekCalendarState", "Attempting to scroll out of range: $date")
            return null
        }
        return getWeekIndex(startDateAdjusted, date)
    }

    public companion object {
        internal val Saver: Saver<WeekCalendarState, Any> = listSaver(
            save = {
                listOf(
                    it.startDate.toIso8601String(),
                    it.endDate.toIso8601String(),
                    it.firstVisibleWeek.days.first().date.toIso8601String(),
                    it.firstDayOfWeek,
                    it.listState.firstVisibleItemIndex,
                    it.listState.firstVisibleItemScrollOffset,
                )
            },
            restore = {
                WeekCalendarState(
                    startDate = (it[0] as String).fromIso8601LocalDate(),
                    endDate = (it[1] as String).fromIso8601LocalDate(),
                    firstVisibleWeekDate = (it[2] as String).fromIso8601LocalDate(),
                    firstDayOfWeek = it[3] as DayOfWeek,
                    visibleItemState = VisibleItemState(
                        firstVisibleItemIndex = it[4] as Int,
                        firstVisibleItemScrollOffset = it[5] as Int,
                    ),
                )
            },
        )
    }
}
