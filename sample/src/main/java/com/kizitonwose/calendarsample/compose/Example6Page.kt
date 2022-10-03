package com.kizitonwose.calendarsample.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendarcompose.CalendarLayoutInfo
import com.kizitonwose.calendarcompose.CalendarState
import com.kizitonwose.calendarcompose.HeatMapCalendar
import com.kizitonwose.calendarcompose.rememberCalendarState
import com.kizitonwose.calendarcore.CalendarDay
import com.kizitonwose.calendarcore.CalendarMonth
import com.kizitonwose.calendarcore.firstDayOfWeekFromLocale
import com.kizitonwose.calendarcore.yearMonth
import com.kizitonwose.calendarsample.displayText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

private val level4 = Color(0xFF216E3A)
private val level3 = Color(0xFF30A14E)
private val level2 = Color(0xFF40C463)
private val level1 = Color(0xFF9BE9A8)
private val level0 = Color(0xFFEBEDF0)

private val allLevels = listOf(level0, level1, level2, level3, level4)
private fun generateRandomData(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, Color> {
    val data = hashMapOf<LocalDate, Color>()
    (0..ChronoUnit.DAYS.between(startDate, endDate)).map { count ->
        data.put(startDate.plusDays(count), allLevels.random())
    }
    return data
}

@Composable
fun Example6Page() {
    var refreshKey by remember { mutableStateOf(1) }
    val endDate = remember { LocalDate.now() }
    // GitHub only shows contributions for the past 12 months
    val startDate = remember { endDate.minusMonths(12) }
    val data = remember { mutableStateOf<Map<LocalDate, Color>>(emptyMap()) }
    var selection by remember { mutableStateOf<Pair<LocalDate, Color>?>(null) }
    LaunchedEffect(startDate, endDate, refreshKey) {
        val value = withContext(Dispatchers.IO) {
            generateRandomData(startDate, endDate)
        }
        data.value = value
        selection = null
    }
    Column(modifier = Modifier.fillMaxSize()) {
        val state = rememberCalendarState(
            startMonth = startDate.yearMonth,
            endMonth = endDate.yearMonth,
            firstVisibleMonth = endDate.yearMonth,
            firstDayOfWeek = firstDayOfWeekFromLocale(),
        )
        HeatMapCalendar(
            modifier = Modifier.padding(vertical = 10.dp),
            state = state,
            contentPadding = PaddingValues(end = 6.dp),
            dayContent = {
                Day(day = it,
                    startDate = startDate,
                    endDate = endDate,
                    color = data.value[it.date] ?: level0) { day ->
                    selection = Pair(day, data.value[it.date] ?: level0)
                }
            },
            weekHeader = { WeekHeader(it) },
            monthHeader = { MonthHeader(it, endDate, state) }
        )
        CalendarInfo(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 44.dp))
        Box(modifier = Modifier.weight(1f)) {
            BottomContent(modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .align(Alignment.BottomCenter),
                selection = selection) { refreshKey += 1 }
        }
    }
}

private val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

@Composable
private fun BottomContent(
    modifier: Modifier = Modifier,
    selection: Pair<LocalDate, Color>? = null,
    refresh: (() -> Unit),
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (selection != null) {
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "Clicked: ${formatter.format(selection.first)}")
                Level(color = selection.second)
            }
        }
        Button(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp), onClick = refresh) {
            Text(
                text = "Generate random data",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }

}

@Composable
private fun CalendarInfo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(text = "Less", fontSize = 10.sp)
        allLevels.forEach { color ->
            Level(color)
        }
        Text(text = "More", fontSize = 10.sp)
    }
}

private val daySize = 18.dp

@Composable
private fun Day(
    day: CalendarDay,
    startDate: LocalDate,
    endDate: LocalDate,
    color: Color,
    onClick: (LocalDate) -> Unit,
) {
    // We only want to draw boxes on the days that are in the
    // past 12 months. Since the calendar is month-based, we ignore
    // the future dates in the current month and those in the start
    // month that are older than 12 months from today.
    if (day.date in startDate..endDate) {
        Level(color) { onClick(day.date) }
    }
}

@Composable
private fun Level(color: Color, onClick: (() -> Unit)? = null) {
    Box(Modifier
        .size(daySize) // Must set a size on the day.
        .padding(2.dp)
        .clip(RoundedCornerShape(2.dp))
        .background(color = color)
        .clickable(enabled = onClick != null) { onClick?.invoke() })
}

@Composable
private fun WeekHeader(dayOfWeek: DayOfWeek) {
    Box(
        modifier = Modifier
            .height(daySize) // Must set a height on the day of week so it aligns with the day.
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = dayOfWeek.displayText(),
            modifier = Modifier.align(Alignment.Center),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun MonthHeader(
    calendarMonth: CalendarMonth,
    endDate: LocalDate,
    state: CalendarState,
) {
    val density = LocalDensity.current
    val firstFullyVisibleMonth by remember {
        // Find the first index with at most one box out of bounds.
        derivedStateOf { getMonthWithYear(state.layoutInfo, daySize, density) }
    }
    if (calendarMonth.weekDays.first().first().date <= endDate) {
        val month = calendarMonth.yearMonth
        val title = if (month == firstFullyVisibleMonth) {
            month.displayText(short = true)
        } else {
            month.month.displayText()
        }
        Box(Modifier
            .fillMaxWidth()
            .padding(bottom = 1.dp, start = 2.dp)) {
            Text(text = title, fontSize = 10.sp)
        }
    }
}


// Find the first index with at most one box out of bounds.
private fun getMonthWithYear(
    layoutInfo: CalendarLayoutInfo,
    daySize: Dp,
    density: Density,
): YearMonth? {
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

@Preview(heightDp = 600)
@Composable
private fun Example6Preview() {
    Example6Page()
}
