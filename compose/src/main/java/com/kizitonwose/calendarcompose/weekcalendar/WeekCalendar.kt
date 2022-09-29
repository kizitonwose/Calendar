package com.kizitonwose.calendarcompose.weekcalendar

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendarcompose.CalendarDefaults
import com.kizitonwose.calendarcompose.boxcalendar.VisibleItemState
import com.kizitonwose.calendarcompose.internal.daysUntil
import com.kizitonwose.calendarcore.atStartOfMonth
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

@Stable
class WeekCalendarState internal constructor(
    startDate: LocalDate,
    endDate: LocalDate,
    firstDayOfWeek: DayOfWeek,
    visibleItemState: VisibleItemState
) : ScrollableState {
    internal val listState = LazyListState(
        firstVisibleItemIndex = visibleItemState.firstVisibleItemIndex,
        firstVisibleItemScrollOffset = visibleItemState.firstVisibleItemScrollOffset
    )

    var startDate by mutableStateOf(startDate)

    var endDate by mutableStateOf(endDate)

    var startDateAdjusted by mutableStateOf(startDate)
        internal set

    var endDateAdjusted by mutableStateOf(endDate)
        internal set

    var firstDayOfWeek by mutableStateOf(firstDayOfWeek)

    val firstVisibleDate: LocalDate get() = startDateAdjusted.plusWeeks(listState.firstVisibleItemIndex.toLong())

    val lastVisibleDate: LocalDate
        get() = startDateAdjusted.plusWeeks(
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.toLong() ?: 0
        )

    val layoutInfo: WeekCalendarLayoutInfo
        get() = WeekCalendarLayoutInfo(listState.layoutInfo, startDateAdjusted)

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
        block: suspend ScrollScope.() -> Unit
    ) = listState.scroll(scrollPriority, block)

    @SuppressLint("LogNotTimber")
    private fun getScrollIndex(date: LocalDate): Int? {
        if (date !in startDate..endDate) {
            Log.d("WeekCalendarState", "Attempting to scroll out of range")
            return null
        }
        return ChronoUnit.WEEKS.between(startDateAdjusted, date).toInt()
    }

    companion object {
        val Saver: Saver<WeekCalendarState, *> = listSaver(
            save = {
                val visibleItemState = VisibleItemState(
                    firstVisibleItemIndex = it.listState.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = it.listState.firstVisibleItemScrollOffset
                )
                listOf(it.startDate, it.endDate, it.firstDayOfWeek, visibleItemState)
            },
            restore = {
                WeekCalendarState(
                    startDate = it[0] as LocalDate,
                    endDate = it[1] as LocalDate,
                    firstDayOfWeek = it[2] as DayOfWeek,
                    visibleItemState = it[3] as VisibleItemState
                )
            }
        )
    }
}

@Composable
fun rememberWeekCalendarState(
    startMonth: LocalDate = YearMonth.now().atStartOfMonth(),
    endMonth: LocalDate = YearMonth.now().atEndOfMonth(),
    firstDayOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
): WeekCalendarState {
    return rememberSaveable(saver = WeekCalendarState.Saver) {
        WeekCalendarState(
            startDate = startMonth,
            endDate = endMonth,
            firstDayOfWeek = firstDayOfWeek,
            visibleItemState = VisibleItemState()
        )
    }
}


@Composable
private fun Day(date: LocalDate) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    Box(
        Modifier
//            .width(screenWidth / 7)
            .width(40.dp)
            .wrapContentHeight()
            .padding(0.5.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color = Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Text(
                text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = date.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                fontSize = 10.sp,
                fontWeight = FontWeight.Light
            )
            Text(
                text = date.year.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun WeekHeader(days: List<LocalDate>) {
    val get: (LocalDate) -> String = { date ->
        "${date.dayOfMonth} ${
            date.month.getDisplayName(
                TextStyle.SHORT,
                Locale.ENGLISH
            )
        } ${date.year}"
    }
    val title = "${get(days.first())} - ${get(days.last())}"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Cyan)
    ) {
        Text(
            text = title,
            fontSize = 10.sp
        )
    }
}

@Composable
fun WeekCalendar(
    modifier: Modifier = Modifier,
    state: WeekCalendarState = rememberWeekCalendarState(),
    calendarScrollPaged: Boolean = false,
    dayContent: @Composable RowScope.(LocalDate) -> Unit = { },
    weekHeader: @Composable ColumnScope.(List<LocalDate>) -> Unit = { },
    weekFooter: @Composable ColumnScope.(List<LocalDate>) -> Unit = { },
) {
    val calendarData = getWeekIndexData(state.startDate, state.endDate, state.firstDayOfWeek)
    state.startDateAdjusted = calendarData.startDateAdjusted
    state.endDateAdjusted = calendarData.endDateAdjusted
    val cache = mutableMapOf<Int, WeekData>()
    DisposableEffect(calendarData) {
        cache.clear() // Key changed.
        onDispose { cache.clear() } // Composition disposed.
    }
    fun getData(offset: Int) =
        cache.getOrPut(offset) { getWeekData(calendarData.startDateAdjusted, offset) }

    val flingBehavior = CalendarDefaults.flingBehavior(calendarScrollPaged, state.listState)

    LazyRow(
        modifier = modifier,
        state = state.listState,
        flingBehavior = flingBehavior
    ) {
        items(
            count = calendarData.weekCount,
            key = { offset -> getData(offset) }) { offset ->
            val columnModifier = if (calendarScrollPaged) {
                Modifier.fillParentMaxWidth()
            } else Modifier.width(IntrinsicSize.Max)
            val data = getData(offset)
            Column(modifier = columnModifier) {
                weekHeader(data.days)
                Row {
                    for (date in data.days) {
                        dayContent(date)
                    }
                }
                weekFooter(data.days)
            }
        }
    }
}

private data class WeekIndexData(
    val startDateAdjusted: LocalDate,
    val endDateAdjusted: LocalDate,
    val weekCount: Int
)

private fun getWeekIndexData(
    startDate: LocalDate,
    endDate: LocalDate,
    firstDayOfWeek: DayOfWeek
): WeekIndexData {
    val inDays = firstDayOfWeek.daysUntil(startDate.dayOfWeek)
    val startDateAdjusted = startDate.minusDays(inDays.toLong())
    val weeksBetween =
        ChronoUnit.WEEKS.between(startDateAdjusted.atStartOfDay(), endDate.atStartOfDay()).toInt()
    val endDateAdjusted = startDateAdjusted.plusWeeks(weeksBetween.toLong()).plusDays(6)
    return WeekIndexData(
        startDateAdjusted = startDateAdjusted,
        endDateAdjusted = endDateAdjusted,
        weekCount = weeksBetween + 1 // Add one to include the start week itself!
    )
}

private fun getWeekData(startDate: LocalDate, offset: Int): WeekData {
    val firstDayInWeek = startDate.plusWeeks(offset.toLong())
    return WeekData(firstDayInWeek)
}

@Parcelize // Parcelize because it is used as LazyRow key.
private data class WeekData(val firstDayInWeek: LocalDate) : Parcelable {
    @IgnoredOnParcel
    val days = (0 until 7).map { firstDayInWeek.plusDays(it.toLong()) }
}

@Preview(heightDp = 300)
@Composable
fun WeekCalendarPreview() {
    val state = rememberWeekCalendarState(
        startMonth = YearMonth.now().atStartOfMonth(),
        endMonth = YearMonth.now().plusMonths(2).atEndOfMonth(),
        firstDayOfWeek = DayOfWeek.SUNDAY
    )

    val coroutineScope = rememberCoroutineScope()

    Column {
        WeekCalendar(state = state,
            dayContent = { day -> Day(day) },
            weekHeader = { days -> WeekHeader(days) })

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                state.startDate = state.startDate.minusMonths(1)
            }) {
                Text("Add Start")
            }
            OutlinedButton(onClick = {
                state.endDate = state.endDate.plusMonths(1)
            }) {
                Text("Add End")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                state.startDate = state.startDate.plusMonths(1)
            }) {
                Text("Remove Start")
            }
            OutlinedButton(onClick = {
                state.endDate = state.endDate.minusMonths(1)
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
                    val count = ChronoUnit.MONTHS.between(state.startDate, state.endDate)
                    state.animateScrollToDate(state.startDate.plusMonths((0..count).random()))
                }
            }) {
                Text("Scroll To min")
            }
        }
    }
}