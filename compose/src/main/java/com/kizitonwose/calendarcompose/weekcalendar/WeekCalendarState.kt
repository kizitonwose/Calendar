package com.kizitonwose.calendarcompose.weekcalendar

import android.util.Log
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.kizitonwose.calendarcompose.VisibleItemState
import com.kizitonwose.calendarcore.WeekDay
import com.kizitonwose.calendarcore.atStartOfMonth
import com.kizitonwose.calendarcore.firstDayOfWeekFromLocale
import com.kizitonwose.calendarinternal.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Stable
class WeekCalendarState internal constructor(
    startDate: LocalDate,
    endDate: LocalDate,
    firstVisibleWeekDate: LocalDate,
    firstDayOfWeek: DayOfWeek,
    visibleItemState: VisibleItemState?,
) : ScrollableState {

    private var startDateAdjusted by mutableStateOf(startDate)
    private var endDateAdjusted by mutableStateOf(endDate)

    private var _startDate by mutableStateOf(startDate)
    var startDate: LocalDate
        get() = _startDate
        set(value) {
            if (value != _startDate) {
                _startDate = value
                adjustDateRange()
            }
        }

    private var _endDate by mutableStateOf(endDate)
    var endDate: LocalDate
        get() = _endDate
        set(value) {
            if (value != _endDate) {
                _endDate = value
                adjustDateRange()
            }
        }

    private var _firstDayOfWeek by mutableStateOf(firstDayOfWeek)
    var firstDayOfWeek: DayOfWeek
        get() = _firstDayOfWeek
        set(value) {
            if (value != _firstDayOfWeek) {
                _firstDayOfWeek = value
                adjustDateRange()
            }
        }

    internal val store = DataStore { offset ->
        getWeekCalendarData(startDateAdjusted, offset, startDate, endDate)
    }

    internal var weekIndexCount by mutableStateOf(0)

    internal val listState = run {
        adjustDateRange() // Update date range and weekIndexCount initially.
        val item = visibleItemState ?: run {
            VisibleItemState(firstVisibleItemIndex = getScrollIndex(firstVisibleWeekDate) ?: 0)
        }
        LazyListState(
            firstVisibleItemIndex = item.firstVisibleItemIndex,
            firstVisibleItemScrollOffset = item.firstVisibleItemScrollOffset
        )
    }

    private fun adjustDateRange() {
        val data = getWeekCalendarAdjustedRange(startDate, endDate, firstDayOfWeek)
        startDateAdjusted = data.startDateAdjusted
        endDateAdjusted = data.endDateAdjusted
        store.clear()
        weekIndexCount = getWeekIndicesCount(startDateAdjusted, endDateAdjusted)
    }

    val firstVisibleWeek: List<WeekDay>
        get() = store[listState.firstVisibleItemIndex].days

    val layoutInfo: WeekCalendarLayoutInfo
        get() = WeekCalendarLayoutInfo(listState.layoutInfo) { index -> store[index].days }

    suspend fun scrollToDate(date: LocalDate) {
        listState.scrollToItem(getScrollIndex(date) ?: return)
    }

    suspend fun animateScrollToDate(date: LocalDate) {
        listState.animateScrollToItem(getScrollIndex(date) ?: return)
    }

    override val isScrollInProgress: Boolean get() = listState.isScrollInProgress

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
        val Saver: Saver<WeekCalendarState, *> = listSaver(
            save = {
                val visibleItemState = VisibleItemState(
                    firstVisibleItemIndex = it.listState.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = it.listState.firstVisibleItemScrollOffset
                )
                listOf(
                    it.startDate,
                    it.endDate,
                    it.firstVisibleWeek.first(),
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
            }
        )
    }
}

class WeekCalendarLayoutInfo(
    info: LazyListLayoutInfo,
    private val getIndexData: (Int) -> List<WeekDay>,
) :
    LazyListLayoutInfo by info {
    val visibleWeeksInfo: List<WeekCalendarItemInfo>
        get() = visibleItemsInfo.map { info ->
            WeekCalendarItemInfo(info, getIndexData(info.index))
        }
}

class WeekCalendarItemInfo(info: LazyListItemInfo, val dates: List<WeekDay>) :
    LazyListItemInfo by info

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
