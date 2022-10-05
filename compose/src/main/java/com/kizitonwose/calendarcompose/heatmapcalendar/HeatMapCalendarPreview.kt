package com.kizitonwose.calendarcompose.heatmapcalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendarcompose.CalendarLayoutInfo
import com.kizitonwose.calendarcompose.HeatMapCalendar
import com.kizitonwose.calendarcore.CalendarDay
import com.kizitonwose.calendarcore.CalendarMonth
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*

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
private fun MonthHeader(calendarMonth: CalendarMonth, state: HeatMapCalendarState) {
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
private fun getMonthWithYear(layoutInfo: CalendarLayoutInfo, density: Density): YearMonth? {
    val visibleItemsInfo = layoutInfo.visibleMonthsInfo
    return when {
        visibleItemsInfo.isEmpty() -> null
        visibleItemsInfo.count() == 1 -> visibleItemsInfo.first().month.yearMonth
        else -> {
            val firstItem = visibleItemsInfo.first()
            if (firstItem.offset < layoutInfo.viewportStartOffset &&
                (layoutInfo.viewportStartOffset - firstItem.offset > with(density) { daySize.toPx() })
            ) {
                visibleItemsInfo[1].month.yearMonth
            } else {
                firstItem.month.yearMonth
            }
        }
    }
}

@Preview
@Composable
private fun HeatMapCalendarPreview() {
    val state = rememberHeatMapCalendarState(
        startMonth = YearMonth.now(),
        endMonth = YearMonth.now().plusMonths(10),
        firstVisibleMonth = YearMonth.now().plusMonths(2),
        firstDayOfWeek = DayOfWeek.MONDAY,
    )

    val coroutineScope = rememberCoroutineScope()

    Column {
        HeatMapCalendar(state = state,
            dayContent = { day, _ -> Day(day) },
            weekHeader = { WeekHeader(it) },
            monthHeader = { MonthHeader(it, state) }
        )
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
