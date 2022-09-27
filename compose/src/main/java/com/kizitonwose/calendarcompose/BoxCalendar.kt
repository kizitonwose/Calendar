package com.kizitonwose.calendarcompose

import android.annotation.SuppressLint
import android.os.Parcelable
import android.util.Log
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendarcore.atStartOfMonth
import com.kizitonwose.calendarcore.daysOfWeek
import com.kizitonwose.calendarcore.yearMonth
import kotlinx.coroutines.launch
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.*

@Parcelize
internal class VisibleItemState(
    val firstVisibleItemIndex: Int = 0,
    val firstVisibleItemScrollOffset: Int = 0
) : Parcelable

class BoxCalendarLayoutInfo(info: LazyListLayoutInfo, private val startMonth: YearMonth) :
    LazyListLayoutInfo by info {
    val visibleMonthsInfo: List<BoxCalendarItemInfo>
        get() = visibleItemsInfo.map {
            BoxCalendarItemInfo(
                it,
                startMonth.plusMonths(it.index.toLong())
            )
        }
}

class BoxCalendarItemInfo(info: LazyListItemInfo, val month: YearMonth) : LazyListItemInfo by info

@Stable
class BoxCalendarState internal constructor(
    startMonth: YearMonth,
    endMonth: YearMonth,
    firstDayOfWeek: DayOfWeek,
    visibleItemState: VisibleItemState
) : ScrollableState {
    internal val listState = LazyListState(
        firstVisibleItemIndex = visibleItemState.firstVisibleItemIndex,
        firstVisibleItemScrollOffset = visibleItemState.firstVisibleItemScrollOffset
    )

    var startMonth by mutableStateOf(startMonth)

    var endMonth by mutableStateOf(endMonth)

    var firstDayOfWeek by mutableStateOf(firstDayOfWeek)

    val firstVisibleMonth: YearMonth get() = startMonth.plusMonths(listState.firstVisibleItemIndex.toLong())

    val lastVisibleMonth: YearMonth
        get() = startMonth.plusMonths(
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.toLong() ?: 0
        )

    val layoutInfo: BoxCalendarLayoutInfo
        get() = BoxCalendarLayoutInfo(
            listState.layoutInfo,
            startMonth
        )

    suspend fun scrollToMonth(month: YearMonth) {
        listState.scrollToItem(getScrollIndex(month) ?: return)
    }

    suspend fun animateScrollToMonth(month: YearMonth) {
        listState.animateScrollToItem(getScrollIndex(month) ?: return)
    }

    @SuppressLint("LogNotTimber")
    private fun getScrollIndex(month: YearMonth): Int? {
        if (month !in startMonth..endMonth) {
            Log.d("BoxCalendarState", "Attempting to scroll out of range")
            return null
        }
        return ChronoUnit.MONTHS.between(startMonth, month).toInt()
    }

    override val isScrollInProgress: Boolean get() = listState.isScrollInProgress

    override fun dispatchRawDelta(delta: Float): Float = listState.dispatchRawDelta(delta)

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit
    ) =
        listState.scroll(scrollPriority, block)

    companion object {
        val Saver: Saver<BoxCalendarState, *> = listSaver(
            save = {
                val visibleItemState = VisibleItemState(
                    firstVisibleItemIndex = it.listState.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = it.listState.firstVisibleItemScrollOffset
                )
                listOf(it.startMonth, it.endMonth, it.firstDayOfWeek, visibleItemState)
            },
            restore = {
                BoxCalendarState(
                    startMonth = it[0] as YearMonth,
                    endMonth = it[1] as YearMonth,
                    firstDayOfWeek = it[2] as DayOfWeek,
                    visibleItemState = it[3] as VisibleItemState
                )
            }
        )
    }
}

@Composable
fun rememberBoxCalendarState(
    startMonth: YearMonth = YearMonth.now(),
    endMonth: YearMonth = YearMonth.now(),
    firstDayOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
): BoxCalendarState {
    return rememberSaveable(saver = BoxCalendarState.Saver) {
        BoxCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstDayOfWeek = firstDayOfWeek,
            visibleItemState = VisibleItemState()
        )
    }
}

private val daySize = 20.dp

@Composable
private fun Day(day: CalendarDay) {
    Box(
        Modifier
            .size(daySize)
            .padding(0.5.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color = Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
private fun WeekHeader(dayOfWeek: DayOfWeek) {
    Box(
        modifier = Modifier
            .height(daySize)
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
            modifier = Modifier
                .align(Alignment.Center),
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun MonthHeader(calendarMonth: CalendarMonth, state: BoxCalendarState) {
    val density = LocalDensity.current
    val firstFullyVisibleMonth by remember {
        derivedStateOf { getMonthWithYear(state.layoutInfo, density) }
    }
    val month = calendarMonth.yearMonth
    val title = if (month == firstFullyVisibleMonth) {
        "${month.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)} ${month.year}"
    } else {
        month.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    }
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 10.sp
        )
    }
}

// Find the first index visible with desired offset
private fun getMonthWithYear(layoutInfo: BoxCalendarLayoutInfo, density: Density): YearMonth? {
    val visibleItemsInfo = layoutInfo.visibleMonthsInfo
    return when {
        visibleItemsInfo.isEmpty() -> null
        visibleItemsInfo.count() == 1 -> visibleItemsInfo.first().month
        else -> {
            val firstItem = visibleItemsInfo.first()
            if (firstItem.offset < layoutInfo.viewportStartOffset &&
                (layoutInfo.viewportStartOffset - firstItem.offset > with(density) { daySize.toPx() })
            ) {
                visibleItemsInfo[1].month
            } else {
                firstItem.month
            }
        }
    }
}

enum class WeekHeaderPosition {
    Start, End
}

// TODO: Equals and HashCode as in CalendarView
data class CalendarMonth internal constructor(
    val yearMonth: YearMonth,
    val weekDays: List<List<CalendarDay>>
)

data class CalendarDay(val date: LocalDate, val owner: DayOwner)

@Composable
fun BoxCalendar(
    modifier: Modifier = Modifier,
    weekHeaderPosition: WeekHeaderPosition = WeekHeaderPosition.Start,
    state: BoxCalendarState = rememberBoxCalendarState(),
    dayContent: @Composable ColumnScope.(CalendarDay) -> Unit = { day -> Day(day) },
    weekHeader: @Composable ColumnScope.(DayOfWeek) -> Unit = { dayOfWeek -> WeekHeader(dayOfWeek) },
    monthHeader: @Composable ColumnScope.(CalendarMonth) -> Unit = { month ->
        MonthHeader(
            month,
            state
        )
    },
) {
    val startMonth = state.startMonth
    val endMonth = state.endMonth
    val firstDayOfWeek = state.firstDayOfWeek
    val itemsCount = getMonthIndicesCount(startMonth, endMonth)
    val cache = mutableMapOf<Int, MonthData>()
    DisposableEffect(startMonth, endMonth, firstDayOfWeek) {
        cache.clear() // Key changed.
        onDispose { cache.clear() } // Composition disposed.
    }
    fun getData(offset: Int) =
        cache.getOrPut(offset) { getMonthData(startMonth, offset, firstDayOfWeek) }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        @Composable
        fun Header(horizontalAlignment: Alignment.Horizontal) {
            Column(
                modifier = Modifier.width(IntrinsicSize.Max),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = horizontalAlignment
            ) {
                for (dayOfWeek in daysOfWeek(firstDayOfWeek)) {
                    weekHeader(dayOfWeek)
                }
            }
        }
        if (weekHeaderPosition == WeekHeaderPosition.Start) {
            Header(horizontalAlignment = Alignment.End)
        }
        LazyRow(
            state = state.listState,
            modifier = Modifier.weight(1f)
        ) {
            items(
                count = itemsCount,
                key = { offset -> getData(offset) }) { offset ->
                val data = getData(offset)
                Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                    monthHeader(data.calendarMonth)
                    Row {
                        for (week in data.calendarMonth.weekDays) {
                            Column {
                                for (day in week) {
                                    dayContent(day)
                                }
                            }
                        }
                    }
                }
            }
        }
        if (weekHeaderPosition == WeekHeaderPosition.End) {
            Header(horizontalAlignment = Alignment.Start)
        }
    }
}

private fun DayOfWeek.daysUntil(other: DayOfWeek) = (7 + (other.value - value)) % 7

private fun getMonthIndicesCount(startMonth: YearMonth, endMonth: YearMonth): Int {
    // Add one to include the start month itself!
    return ChronoUnit.MONTHS.between(startMonth, endMonth).toInt() + 1
}

private fun getMonthData(startMonth: YearMonth, offset: Int, firstDayOfWeek: DayOfWeek): MonthData {
    val month = startMonth.plusMonths(offset.toLong())
    val firstDay = month.atStartOfMonth()
    val inDays = if (offset == 0) {
        firstDayOfWeek.daysUntil(firstDay.dayOfWeek)
    } else {
        -firstDay.dayOfWeek.daysUntil(firstDayOfWeek)
    }
    val outDays = (inDays + month.lengthOfMonth()).let { totalDays ->
        if (totalDays % 7 != 0) 7 - (totalDays % 7) else 0
    }
    return MonthData(month, inDays, outDays)
}

@Parcelize // Parcelize because it is used as LazyRow key.
private data class MonthData(val month: YearMonth, val inDays: Int, val outDays: Int) : Parcelable {

    @IgnoredOnParcel
    private val totalDays = inDays + month.lengthOfMonth() + outDays

    @IgnoredOnParcel
    private val firstDay = month.atStartOfMonth().minusDays(inDays.toLong())

    @IgnoredOnParcel
    private val rows = (0 until totalDays).chunked(7)

    @IgnoredOnParcel
    private val cache = mutableMapOf<Int, CalendarDay>()

    @IgnoredOnParcel
    val calendarMonth =
        CalendarMonth(month, rows.map { week -> week.map { dayOffset -> getDay(dayOffset) } })

    private fun getDay(columnOffset: Int): CalendarDay {
        return cache.getOrPut(columnOffset) {
            val date = firstDay.plusDays(columnOffset.toLong())
            val owner = when (date.yearMonth) {
                month -> DayOwner.ThisMonth
                month.minusMonths(1) -> DayOwner.PreviousMonth
                month.plusMonths(1) -> DayOwner.NextMonth
                else -> throw IllegalArgumentException("Invalid date: $date in month: $month")
            }
            return@getOrPut CalendarDay(date, owner)
        }
    }
}

enum class DayOwner {
    PreviousMonth, ThisMonth, NextMonth
}

// TODO: Mode this function to utils in sample project.
@Composable
fun InitialCalendarScroll(action: suspend () -> Unit) {
    var initialScroll by rememberSaveable { mutableStateOf(false) }
    if (!initialScroll) {
        LaunchedEffect(Unit) {
            action()
            initialScroll = true
        }
    }
}

@Preview(heightDp = 300)
@Composable
fun BoxCalendarPreview() {
    val state = rememberBoxCalendarState(
        startMonth = YearMonth.now(),
        endMonth = YearMonth.now().plusMonths(1),
        firstDayOfWeek = DayOfWeek.MONDAY
    )

    InitialCalendarScroll {
//        state.scrollToMonth(YearMonth.now().plusMonths(2))
    }

    val coroutineScope = rememberCoroutineScope()

    Column {
        BoxCalendar(state = state)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                state.startMonth = state.startMonth.minusMonths(1)
            }) {
                Text("Add Start")
            }
            OutlinedButton(onClick = {
                state.endMonth = state.endMonth.plusMonths(1)
            }) {
                Text("Add End")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                state.startMonth = state.startMonth.plusMonths(1)
            }) {
                Text("Remove Start")
            }
            OutlinedButton(onClick = {
                state.endMonth = state.endMonth.minusMonths(1)
            }) {
                Text("Remove End")
            }
        }
        Row {
            OutlinedButton(onClick = {
                state.firstDayOfWeek = state.firstDayOfWeek.plus(1)
            }) {
                Text("Move First Day Of Week")
            }
        }
        Row {
            OutlinedButton(onClick = {
                coroutineScope.launch {
                    val count = ChronoUnit.MONTHS.between(state.startMonth, state.endMonth)
                    state.animateScrollToMonth(state.startMonth.plusMonths((0..count).random()))
                }
            }) {
                Text("Scroll To min")
            }
        }
        val firstVisibleItem by remember {
            derivedStateOf { state.firstVisibleMonth }
        }
        val lastVisibleItem by remember {
            derivedStateOf { state.lastVisibleMonth }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "FirstVisible: ${
                    firstVisibleItem.month.getDisplayName(
                        TextStyle.SHORT,
                        Locale.ENGLISH
                    )
                }, ${firstVisibleItem.year}"
            )
            Text(
                "LastVisible: ${
                    lastVisibleItem.month.getDisplayName(
                        TextStyle.SHORT,
                        Locale.ENGLISH
                    )
                }, ${lastVisibleItem.year}"
            )
        }
    }
}