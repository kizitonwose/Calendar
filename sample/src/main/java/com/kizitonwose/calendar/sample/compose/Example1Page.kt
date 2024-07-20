package com.kizitonwose.calendar.sample.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices.PIXEL_TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.VerticalYearCalendar
import com.kizitonwose.calendar.compose.yearcalendar.YearContentHeightMode
import com.kizitonwose.calendar.compose.yearcalendar.rememberYearCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.ExperimentalCalendarApi
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.sample.R
import com.kizitonwose.calendar.sample.shared.displayText
import java.time.Year
import java.time.YearMonth

@OptIn(ExperimentalCalendarApi::class)
@Composable
fun Example1Page(adjacentMonths: Long = 20) {
    val currentMonth = remember { YearMonth.now() }
    val currentYear = remember { Year.of(currentMonth.year) }
    val startYear = remember { currentYear }
    val endYear = remember { currentYear.plusYears(adjacentMonths) }
    val selections = remember { mutableStateListOf<CalendarDay>() }
    val daysOfWeek = remember { daysOfWeek() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val state = rememberYearCalendarState(
            startMonth = currentYear,
            endMonth = endYear,
            firstVisibleMonth = currentYear,
            firstDayOfWeek = daysOfWeek.first(),
        )
        val coroutineScope = rememberCoroutineScope()
//        val visibleMonth = rememberFirstMostVisibleMonth(state, viewportPercent = 90f)
//        SimpleCalendarTitle(
//            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
//            currentMonth = visibleMonth.yearMonth,
//            goToPrevious = {
//                coroutineScope.launch {
//                    state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.previousMonth)
//                }
//            },
//            goToNext = {
//                coroutineScope.launch {
//                    state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.nextMonth)
//                }
//            },
//        )
        VerticalYearCalendar(
            modifier = Modifier
                .fillMaxSize()
                .testTag("Calendar"),
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
            contentHeightMode = YearContentHeightMode.Wrap,
            monthVerticalArrangement = Arrangement.spacedBy(20.dp),
            monthHorizontalArrangement = Arrangement.spacedBy(20.dp),
            yearBodyContentPadding = PaddingValues(horizontal = 20.dp),
            isMonthVisible = {
                it.yearMonth >= currentMonth
            },
            yearHeader = {
                YearHeader(it.year)
            },
            monthHeader = {
                MonthHeader(it)
            },
        )
    }
}

@Composable
private fun MonthHeader(calendarMonth: CalendarMonth) {
    val daysOfWeek = calendarMonth.weekDays.first().map { it.date.dayOfWeek }
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = calendarMonth.yearMonth.displayText(short = true),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            for (dayOfWeek in daysOfWeek) {
                Text(
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    text = dayOfWeek.displayText(uppercase = true, narrow = true),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun YearHeader(year: Year) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .testTag("MonthHeader"),
        textAlign = TextAlign.Center,
        fontSize = 52.sp,
        text = year.toString(),
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun Day(day: CalendarDay, isSelected: Boolean, onClick: (CalendarDay) -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // This is important for square-sizing!
//            .fillMaxSize()
            .testTag("MonthDay")
            .padding(6.dp)
            .clip(CircleShape)
            .background(color = if (isSelected) colorResource(R.color.example_1_selection_color) else Color.Transparent)
            // Disable clicks on inDates/outDates
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                showRipple = !isSelected,
                onClick = { onClick(day) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (day.position == DayPosition.MonthDate) {
            Text(
                text = day.date.dayOfMonth.toString(),
                fontSize = 10.sp,
            )
        }

    }
}

@Preview(showBackground = true, heightDp = 1280, widthDp = 800, device = PIXEL_TABLET)
@Composable
private fun Example1Preview() {
    Example1Page()
}
