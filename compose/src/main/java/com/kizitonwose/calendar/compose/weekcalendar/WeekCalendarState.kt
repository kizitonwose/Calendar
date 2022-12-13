package com.kizitonwose.calendar.compose.weekcalendar

import android.util.Log
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.kizitonwose.calendar.compose.VisibleItemState
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.WeekDayPosition
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.data.DataStore
import com.kizitonwose.calendar.data.getWeekCalendarAdjustedRange
import com.kizitonwose.calendar.data.getWeekCalendarData
import com.kizitonwose.calendar.data.getWeekIndex
import com.kizitonwose.calendar.data.getWeekIndicesCount
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * Creates a [WeekCalendarState] that is remembered across compositions.
 *
 * @param startDate the initial value for [WeekCalendarState.startDate]
 * @param endDate the initial value for [WeekCalendarState.endDate]
 * @param firstDayOfWeek the initial value for [WeekCalendarState.firstDayOfWeek]
 * @param firstVisibleWeekDate the date which will have its week visible initially.
 */
@Composable
fun rememberWeekCalendarState(
    startDate: LocalDate = YearMonth.now().atStartOfMonth(),
    endDate: LocalDate = YearMonth.now().atEndOfMonth(),
    firstVisibleWeekDate: LocalDate = LocalDate.now(),
    firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale(),
): WeekCalendarState {
    return rememberSaveable(saver = WeekCalendarState.Saver) {
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
class WeekCalendarState internal constructor(
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
    var startDate: LocalDate
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
    var endDate: LocalDate
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
    var firstDayOfWeek: DayOfWeek
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
    val firstVisibleWeek: Week by derivedStateOf {
        store[listState.firstVisibleItemIndex]
    }

    /**
     * The last week that is visible.
     */
    val lastVisibleWeek: Week by derivedStateOf {
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
    val layoutInfo: WeekCalendarLayoutInfo
        get() = WeekCalendarLayoutInfo(listState.layoutInfo) { index -> store[index] }

    internal val store = DataStore { offset ->
        getWeekCalendarData(
            startDateAdjusted = this.startDateAdjusted,
            offset = offset,
            desiredStartDate = this.startDate,
            desiredEndDate = this.endDate,
        ).week
    }

    internal var weekIndexCount by mutableStateOf(0)

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
    suspend fun scrollToWeek(date: LocalDate) {
        listState.scrollToItem(getScrollIndex(date) ?: return)
    }

    /**
     * Animate (smooth scroll) to the week containing the given [date].
     *
     * @param date the week to which to scroll.
     */
    suspend fun animateScrollToWeek(date: LocalDate) {
        listState.animateScrollToItem(getScrollIndex(date) ?: return)
    }

    /**
     * [InteractionSource] that will be used to dispatch drag events when this
     * calendar is being dragged. If you want to know whether the fling (or animated scroll) is in
     * progress, use [isScrollInProgress].
     */
    val interactionSource: InteractionSource
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
    ) = listState.scroll(scrollPriority, block)

    private fun getScrollIndex(date: LocalDate): Int? {
        if (date !in startDateAdjusted..endDateAdjusted) {
            Log.d("WeekCalendarState", "Attempting to scroll out of range; $date")
            return null
        }
        return getWeekIndex(startDateAdjusted, date)
    }

    companion object {
        val Saver: Saver<WeekCalendarState, Any> = listSaver(
            save = {
                val visibleItemState = VisibleItemState(
                    firstVisibleItemIndex = it.listState.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = it.listState.firstVisibleItemScrollOffset,
                )
                listOf(
                    it.startDate,
                    it.endDate,
                    it.firstVisibleWeek.days.first().date,
                    it.firstDayOfWeek,
                    visibleItemState,
                )
            },
            restore = {
                WeekCalendarState(
                    startDate = it[0] as LocalDate,
                    endDate = it[1] as LocalDate,
                    firstVisibleWeekDate = it[2] as LocalDate,
                    firstDayOfWeek = it[3] as DayOfWeek,
                    visibleItemState = it[4] as VisibleItemState,
                )
            },
        )
    }
}
