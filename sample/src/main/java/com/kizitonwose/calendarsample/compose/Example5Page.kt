package com.kizitonwose.calendarsample.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendarcompose.WeekCalendar
import com.kizitonwose.calendarcompose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendarcore.yearMonth
import com.kizitonwose.calendarsample.R
import com.kizitonwose.calendarsample.displayText
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun Example5Page(close: () -> Unit = {}) {
    val currentDate = remember { LocalDate.now() }
    val startDate = remember { currentDate.minusDays(500) }
    val endDate = remember { currentDate.plusDays(500) }
    var selection by remember { mutableStateOf(currentDate) }
    var toolBarTitle by remember { mutableStateOf("") }
    Column {
        val state = rememberWeekCalendarState(
            startDate = startDate,
            endDate = endDate,
            firstVisibleDate = currentDate,
        )
        val isScrollInProgress = remember {
            derivedStateOf { state.isScrollInProgress }
        }
        val visibleWeek = remember {
            derivedStateOf { state.layoutInfo.visibleWeeksInfo.firstOrNull() }
        }
        if (!isScrollInProgress.value) {
            val datesInWeek = visibleWeek.value?.dates
            if (datesInWeek != null) {
                toolBarTitle = getPageTitle(datesInWeek)
            }
        }
        TopAppBar(
            elevation = 0.dp,
            title = { Text(text = toolBarTitle) },
            navigationIcon = { NavigationIcon(onBackClick = close) },
        )
        WeekCalendar(
            modifier = Modifier.background(color = colorResource(R.color.colorPrimary)),
            state = state,
            dayContent = { date ->
                Day(date, isSelected = selection == date) { clicked ->
                    if (selection != clicked) {
                        selection = clicked
                    }
                }
            },
        )
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("dd")

@Composable
private fun Day(date: LocalDate, isSelected: Boolean, onClick: (LocalDate) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick(date) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = date.dayOfWeek.displayText(),
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Light
            )
            Text(
                text = dateFormatter.format(date),
                fontSize = 14.sp,
                color = if (isSelected) colorResource(R.color.example_7_yellow) else Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        if (isSelected) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(colorResource(R.color.example_7_yellow))
                .align(Alignment.BottomCenter)
            )
        }
    }
}

private fun getPageTitle(datesInWeek: List<LocalDate>): String {
    val firstDate = datesInWeek.first()
    val lastDate = datesInWeek.last()
    return when {
        firstDate.yearMonth == lastDate.yearMonth -> {
            firstDate.yearMonth.displayText()
        }
        firstDate.year == lastDate.year -> {
            "${firstDate.month.displayText(short = false)} - ${lastDate.yearMonth.displayText()}"
        }
        else -> {
            "${firstDate.yearMonth.displayText()} - ${lastDate.yearMonth.displayText()}"
        }
    }
}

@Preview
@Composable
private fun Example5Preview() {
    Example5Page()
}
