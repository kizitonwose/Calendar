package com.kizitonwose.calendar.compose.heatmapcalendar

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
import com.kizitonwose.calendar.compose.CalendarInfo
import com.kizitonwose.calendar.compose.CalendarLayoutInfo
import com.kizitonwose.calendar.compose.VisibleItemState
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.data.DataStore
import com.kizitonwose.calendar.data.checkDateRange
import com.kizitonwose.calendar.data.getHeatMapCalendarMonthData
import com.kizitonwose.calendar.data.getMonthIndex
import com.kizitonwose.calendar.data.getMonthIndicesCount
import java.time.DayOfWeek
import java.time.YearMonth

/**
 * Creates a [HeatMapCalendarState] that is remembered across compositions.
 *
 * @param startMonth the initial value for [HeatMapCalendarState.startMonth]
 * @param endMonth the initial value for [HeatMapCalendarState.endMonth]
 * @param firstDayOfWeek the initial value for [HeatMapCalendarState.firstDayOfWeek]
 * @param firstVisibleMonth the initial value for [HeatMapCalendarState.firstVisibleMonth]
 */
@Composable
fun rememberHeatMapCalendarState(
    startMonth: YearMonth = YearMonth.now(),
    endMonth: YearMonth = startMonth,
    firstVisibleMonth: YearMonth = startMonth,
    firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale(),
): HeatMapCalendarState {
    return rememberSaveable(saver = HeatMapCalendarState.Saver) {
        HeatMapCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstDayOfWeek = firstDayOfWeek,
            firstVisibleMonth = firstVisibleMonth,
            visibleItemState = null,
        )
    }
}

/**
 * A state object that can be hoisted to control and observe calendar properties.
 *
 * This should be created via [rememberHeatMapCalendarState].
 *
 * @param startMonth the first month on the calendar.
 * @param endMonth the last month on the calendar.
 * @param firstDayOfWeek the first day of week on the calendar.
 * @param firstVisibleMonth the initial value for [HeatMapCalendarState.firstVisibleMonth]
 */
@Stable
class HeatMapCalendarState internal constructor(
    startMonth: YearMonth,
    endMonth: YearMonth,
    firstVisibleMonth: YearMonth,
    firstDayOfWeek: DayOfWeek,
    visibleItemState: VisibleItemState?,
) : ScrollableState {

    /** Backing state for [startMonth] */
    private var _startMonth by mutableStateOf(startMonth)

    /** The first month on the calendar. */
    var startMonth: YearMonth
        get() = _startMonth
        set(value) {
            if (value != startMonth) {
                _startMonth = value
                monthDataChanged()
            }
        }

    /** Backing state for [endMonth] */
    private var _endMonth by mutableStateOf(endMonth)

    /** The last month on the calendar. */
    var endMonth: YearMonth
        get() = _endMonth
        set(value) {
            if (value != endMonth) {
                _endMonth = value
                monthDataChanged()
            }
        }

    /** Backing state for [firstDayOfWeek] */
    private var _firstDayOfWeek by mutableStateOf(firstDayOfWeek)

    /** The first day of week on the calendar. */
    var firstDayOfWeek: DayOfWeek
        get() = _firstDayOfWeek
        set(value) {
            if (value != firstDayOfWeek) {
                _firstDayOfWeek = value
                monthDataChanged()
            }
        }

    /**
     * The first month that is visible.
     *
     * @see [lastVisibleMonth]
     */
    val firstVisibleMonth: CalendarMonth by derivedStateOf {
        store[listState.firstVisibleItemIndex]
    }

    /**
     * The last month that is visible.
     *
     * @see [firstVisibleMonth]
     */
    val lastVisibleMonth: CalendarMonth by derivedStateOf {
        store[listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0]
    }

    /**
     * The object of [CalendarLayoutInfo] calculated during the last layout pass. For example,
     * you can use it to calculate what items are currently visible.
     *
     * Note that this property is observable and is updated after every scroll or remeasure.
     * If you use it in the composable function it will be recomposed on every change causing
     * potential performance issues including infinity recomposition loop.
     * Therefore, avoid using it in the composition.
     *
     * If you need to use it in the composition then consider wrapping the calculation into a
     * derived state in order to only have recompositions when the derived value changes.
     * See Example6Page in the sample app for usage.
     *
     * If you want to run some side effects like sending an analytics event or updating a state
     * based on this value consider using "snapshotFlow".
     */
    val layoutInfo: CalendarLayoutInfo
        get() = CalendarLayoutInfo(listState.layoutInfo) { index -> store[index] }

    /**
     * [InteractionSource] that will be used to dispatch drag events when this
     * calendar is being dragged. If you want to know whether the fling (or animated scroll) is in
     * progress, use [isScrollInProgress].
     */
    val interactionSource: InteractionSource
        get() = listState.interactionSource

    internal val listState = LazyListState(
        firstVisibleItemIndex = visibleItemState?.firstVisibleItemIndex
            ?: getScrollIndex(firstVisibleMonth) ?: 0,
        firstVisibleItemScrollOffset = visibleItemState?.firstVisibleItemScrollOffset ?: 0,
    )

    internal var calendarInfo by mutableStateOf(CalendarInfo(indexCount = 0))

    internal val store = DataStore { offset ->
        getHeatMapCalendarMonthData(
            startMonth = this.startMonth,
            offset = offset,
            firstDayOfWeek = this.firstDayOfWeek,
        ).calendarMonth
    }

    init {
        monthDataChanged() // Update monthIndexCount initially.
    }

    private fun monthDataChanged() {
        store.clear()
        checkDateRange(startMonth, endMonth)
        calendarInfo = CalendarInfo(
            indexCount = getMonthIndicesCount(startMonth, endMonth),
            firstDayOfWeek = firstDayOfWeek,
        )
    }

    /**
     * Instantly brings the [month] to the top of the viewport.
     *
     * @param month the month to which to scroll. Must be within the
     * range of [startMonth] and [endMonth].
     *
     * @see [animateScrollToMonth]
     */
    suspend fun scrollToMonth(month: YearMonth) {
        listState.scrollToItem(getScrollIndex(month) ?: return)
    }

    /**
     * Animate (smooth scroll) to the given [month].
     *
     * @param month the month to which to scroll. Must be within the
     * range of [startMonth] and [endMonth].
     */
    suspend fun animateScrollToMonth(month: YearMonth) {
        listState.animateScrollToItem(getScrollIndex(month) ?: return)
    }

    private fun getScrollIndex(month: YearMonth): Int? {
        if (month !in startMonth..endMonth) {
            Log.d("CalendarState", "Attempting to scroll out of range: $month")
            return null
        }
        return getMonthIndex(startMonth, month)
    }

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

    companion object {
        internal val Saver: Saver<HeatMapCalendarState, Any> = listSaver(
            save = {
                val visibleItemState = VisibleItemState(
                    firstVisibleItemIndex = it.listState.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = it.listState.firstVisibleItemScrollOffset,
                )
                listOf(
                    it.startMonth,
                    it.endMonth,
                    it.firstVisibleMonth.yearMonth,
                    it.firstDayOfWeek,
                    visibleItemState,
                )
            },
            restore = {
                HeatMapCalendarState(
                    startMonth = it[0] as YearMonth,
                    endMonth = it[1] as YearMonth,
                    firstVisibleMonth = it[2] as YearMonth,
                    firstDayOfWeek = it[3] as DayOfWeek,
                    visibleItemState = it[4] as VisibleItemState,
                )
            },
        )
    }
}
