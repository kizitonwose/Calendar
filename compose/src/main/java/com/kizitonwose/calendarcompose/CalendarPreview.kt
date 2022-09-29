package com.kizitonwose.calendarcompose

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*

@Composable
private fun Day(day: CalendarDay) {
    Box(
        Modifier
            .aspectRatio(1f)
            .padding(0.5.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color = Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Light,
        )
    }
}

@Composable
private fun MonthHeader(calendarMonth: CalendarMonth) {
    val month = calendarMonth.yearMonth
    val title = "${month.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)} ${month.year}"
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = title,
            fontSize = 10.sp,
        )
    }
}

@Preview(heightDp = 600)
@Composable
private fun CalendarPreview() {
    val state = rememberCalendarState(
        startMonth = YearMonth.now(),
        endMonth = YearMonth.now().plusMonths(3),
        firstVisibleMonth = YearMonth.now().plusMonths(1),
        firstDayOfWeek = DayOfWeek.MONDAY,
    )

    val coroutineScope = rememberCoroutineScope()

    Column {
        HorizontalCalendar(
            state = state,
            dayContent = { Day(it) },
            monthHeader = { MonthHeader(it) }
        )
//        Calendar(state = state, properties = properties, monthContainer = { _, content ->
//            Box(modifier = Modifier.width(200.dp), content = { content() })
//        })
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
