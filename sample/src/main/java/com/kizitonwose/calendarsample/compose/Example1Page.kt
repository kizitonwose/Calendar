package com.kizitonwose.calendarsample.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendarcompose.*
import com.kizitonwose.calendarsample.R
import com.kizitonwose.calendarsample.displayText
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.YearMonth

@Composable
fun Example1Page() {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(500) }
    val endMonth = remember { currentMonth.plusMonths(500) }
    val selections = remember { mutableStateListOf<CalendarDay>() }
    val daysOfWeek = remember { daysOfWeek() }
    Column {
        val state = rememberCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstVisibleMonth = currentMonth,
            firstDayOfWeek = daysOfWeek.first(),
        )
        val coroutineScope = rememberCoroutineScope()
        var visibleMonth by remember {
            // We can directly use state.firstVisibleMonth via a derived state
            // but we want the header to change only after scrolling stops.
            mutableStateOf(startMonth)
        }
        LaunchedEffect(state.isScrollInProgress) {
            if (!state.isScrollInProgress) {
                visibleMonth = state.firstVisibleMonth
            }
        }
        CalendarTitle(
            currentMonth = visibleMonth,
            goToPrevious = {
                coroutineScope.launch {
                    state.animateScrollToMonth(state.firstVisibleMonth.minusMonths(1))
                }
            },
            goToNext = {
                coroutineScope.launch {
                    state.animateScrollToMonth(state.firstVisibleMonth.plusMonths(1))
                }
            }
        )
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                Day(day, isSelected = selections.contains(day)) { clicked ->
                    if (selections.contains(clicked)) {
                        selections.remove(clicked)
                    } else {
                        selections.add(clicked)
                    }
                }
            },
            monthHeader = {
                MonthHeader(daysOfWeek = daysOfWeek)
            }
        )
    }
}

@Composable
private fun MonthHeader(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                text = dayOfWeek.displayText(),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun Day(day: CalendarDay, isSelected: Boolean, onClick: (CalendarDay) -> Unit) {
    Box(
        Modifier
            .aspectRatio(1f) // This is important for square-sizing!
            .padding(6.dp)
            .clip(CircleShape)
            .background(color = if (isSelected) colorResource(R.color.example_1_selection_color) else Color.Transparent)
            // Disable clicks on inDates/outDates
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                showRipple = !isSelected,
                onClick = { onClick(day) }
            ),
        contentAlignment = Alignment.Center
    ) {
        val textColor = when (day.position) {
            // Color.Unspecified will use the default text color from the current theme
            DayPosition.MonthDate -> if (isSelected) Color.White else Color.Unspecified
            DayPosition.InDate, DayPosition.OutDate -> colorResource(R.color.inactive_text_color)
        }
        Text(
            text = day.date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun CalendarTitle(
    currentMonth: YearMonth,
    goToPrevious: () -> Unit,
    goToNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Max)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .clip(CircleShape)
                .clickable(onClick = goToPrevious),
            painter = painterResource(id = R.drawable.ic_chevron_left),
            contentDescription = "Previous",
        )
        Text(
            modifier = Modifier.weight(1f),
            text = currentMonth.displayText(),
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
        )
        Icon(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .clip(CircleShape)
                .clickable(onClick = goToNext),
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = "Next",
        )
    }
}

@Preview(heightDp = 700)
@Composable
private fun Example1Preview() {
    Example1Page()
}

