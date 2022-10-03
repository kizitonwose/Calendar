package com.kizitonwose.calendarcompose.weekcalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendarcompose.WeekCalendar
import com.kizitonwose.calendarcore.atStartOfMonth
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*

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

@Preview(heightDp = 300)
@Composable
private fun WeekCalendarPreview() {
    val state = rememberWeekCalendarState(
        startDate = YearMonth.now().minusMonths(1).atStartOfMonth(),
        endDate = YearMonth.now().atEndOfMonth(),
        firstVisibleDate = LocalDate.now(),
        firstDayOfWeek = DayOfWeek.SATURDAY
    )

    val coroutineScope = rememberCoroutineScope()

    Column {
        WeekCalendar(state = state,
            calendarScrollPaged = true,
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