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
import com.kizitonwose.calendarcompose.atStartOfMonth
import com.kizitonwose.calendarcompose.firstDayOfWeekFromLocale
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Stable
class WeekCalendarState internal constructor(
    startDate: LocalDate,
    endDate: LocalDate,
    firstVisibleDate: LocalDate,
    firstDayOfWeek: DayOfWeek,
    visibleItemState: VisibleItemState?,
) : ScrollableState {

    private var _startDate by mutableStateOf(startDate)
    var startDate: LocalDate
        get() = _startDate
        set(value) {
            if (value != _startDate) {
                adjustDateRange(startDate = value, endDate = endDate)
            }
        }

    private var _endDate by mutableStateOf(endDate)
    var endDate: LocalDate
        get() = _endDate
        set(value) {
            if (value != _endDate) {
                adjustDateRange(startDate = startDate, endDate = value)
            }
        }

    private var _firstDayOfWeek by mutableStateOf(firstDayOfWeek)
    var firstDayOfWeek: DayOfWeek
        get() = _firstDayOfWeek
        set(value) {
            if (value != _firstDayOfWeek) {
                _firstDayOfWeek = value
                adjustDateRange(startDate = startDate, endDate = endDate)
            }
        }

    internal val listState = run {
        val item = visibleItemState ?: run {
            adjustDateRange(startDate = startDate, endDate = endDate)
            VisibleItemState(firstVisibleItemIndex = getScrollIndex(firstVisibleDate) ?: 0)
        }
        LazyListState(
            firstVisibleItemIndex = item.firstVisibleItemIndex,
            firstVisibleItemScrollOffset = item.firstVisibleItemScrollOffset
        )
    }

    private fun adjustDateRange(startDate: LocalDate, endDate: LocalDate) {
        val data = getWeekCalendarAdjustedRange(startDate, endDate, firstDayOfWeek)
        _startDate = data.startDateAdjusted
        _endDate = data.endDateAdjusted
    }

    val firstVisibleDate: LocalDate
        get() = startDate.plusWeeks(listState.firstVisibleItemIndex.toLong())

    val lastVisibleDate: LocalDate
        get() = startDate.plusWeeks(
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.toLong() ?: 0
        )

    val layoutInfo: WeekCalendarLayoutInfo
        get() = WeekCalendarLayoutInfo(listState.layoutInfo, startDate)

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
        if (date !in startDate..endDate) {
            Log.d("WeekCalendarState", "Attempting to scroll out of range; $date")
            return null
        }
        return ChronoUnit.WEEKS.between(startDate, date).toInt()
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
                    it.firstVisibleDate,
                    it.firstDayOfWeek,
                    visibleItemState,
                )
            },
            restore = {
                WeekCalendarState(
                    startDate = it[0] as LocalDate,
                    endDate = it[1] as LocalDate,
                    firstVisibleDate = it[2] as LocalDate,
                    firstDayOfWeek = it[3] as DayOfWeek,
                    visibleItemState = it[4] as VisibleItemState,
                )
            }
        )
    }
}

class WeekCalendarLayoutInfo(info: LazyListLayoutInfo, private val startDate: LocalDate) :
    LazyListLayoutInfo by info {
    val visibleWeeksInfo: List<WeekCalendarItemInfo>
        get() = visibleItemsInfo.map { info ->
            val start = startDate.plusWeeks(info.index.toLong())
            WeekCalendarItemInfo(info, (0 until 7).map { start.plusDays(it.toLong()) })
        }
}

class WeekCalendarItemInfo(info: LazyListItemInfo, val dates: List<LocalDate>) :
    LazyListItemInfo by info

@Composable
fun rememberWeekCalendarState(
    startDate: LocalDate = YearMonth.now().atStartOfMonth(),
    endDate: LocalDate = YearMonth.now().atEndOfMonth(),
    firstVisibleDate: LocalDate = LocalDate.now(),
    firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale(),
): WeekCalendarState {
    return rememberSaveable(saver = WeekCalendarState.Saver) {
        WeekCalendarState(
            startDate = startDate,
            endDate = endDate,
            firstVisibleDate = firstVisibleDate,
            firstDayOfWeek = firstDayOfWeek,
            visibleItemState = null,
        )
    }
}
