package com.kizitonwose.calendar.sample.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices.PIXEL_TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalYearCalendar
import com.kizitonwose.calendar.compose.yearcalendar.YearContentHeightMode
import com.kizitonwose.calendar.compose.yearcalendar.rememberYearCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.ExperimentalCalendarApi
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.sample.shared.displayText
import kotlinx.coroutines.launch
import java.time.Year
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.abs

@OptIn(ExperimentalCalendarApi::class)
@Composable
fun Example10Page(adjacentYears: Long = 50) {
    val currentMonth = remember { YearMonth.now() }
    val currentYear = remember { Year.of(currentMonth.year) }
    val startYear = remember { currentYear.minusYears(adjacentYears) }
    val endYear = remember { currentYear.plusYears(adjacentYears) }
    val selections = remember { mutableStateListOf<CalendarDay>() }
    val daysOfWeek = remember { daysOfWeek() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val scope = rememberCoroutineScope()
        val state = rememberYearCalendarState(
            startMonth = startYear,
            endMonth = endYear,
            firstVisibleMonth = currentYear,
            firstDayOfWeek = daysOfWeek.first(),
        )
        val visibleYear = rememberFirstVisibleYearAfterScroll(state)
        val headerState = rememberLazyListState()
        LaunchedEffect(visibleYear) {
            val index = ChronoUnit.YEARS.between(startYear, visibleYear.year).toInt()
            headerState.animateScrollAndCenterItem(index)
        }
        YearHeader(
            startYear = startYear,
            endYear = endYear,
            visibleYear = visibleYear.year,
            headerState = headerState,
        ) click@{ target ->
            val visible = visibleYear.year
            if (target == visible) return@click
            scope.launch {
                if (abs(ChronoUnit.YEARS.between(visible, target)) <= 10) {
                    state.animateScrollToMonth(target)
                } else {
                    val nearbyYear = if (target > visible) {
                        target.minusYears(8)
                    } else {
                        target.plusYears(8)
                    }
                    state.scrollToMonth(nearbyYear)
                    state.animateScrollToMonth(target)
                }
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
        HorizontalYearCalendar(
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
            contentHeightMode = YearContentHeightMode.Fill,
            monthHorizontalArrangement = Arrangement.spacedBy(40.dp),
            monthVerticalArrangement = Arrangement.spacedBy(20.dp),
            yearBodyContentPadding = PaddingValues(start = 40.dp, end = 40.dp, bottom = 40.dp),
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
            text = calendarMonth.yearMonth.month.displayText(short = false),
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
private fun YearHeader(
    startYear: Year,
    endYear: Year,
    visibleYear: Year,
    headerState: LazyListState,
    onClick: (Year) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(headerBackground),
        state = headerState,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = headerState, SnapPosition.Center),
        contentPadding = PaddingValues(horizontal = 40.dp),
    ) {
        items(count = ChronoUnit.YEARS.between(startYear, endYear).toInt()) { index ->
            val year = startYear.plusYears(index.toLong())
            val isSelected = visibleYear == year
            Box(
                modifier = Modifier
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                color = simpleTextBackground(isSelected = true),
                                shape = RoundedCornerShape(4.dp),
                            )
                        } else {
                            Modifier
                        },
                    )
                    .clickable(onClick = { onClick(year) })
                    .padding(horizontal = 60.dp, vertical = 10.dp),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = year.value.toString(),
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    color = simpleTextColor(isSelected),
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Light,
                )
            }
        }
    }
}

@Composable
private fun Day(day: CalendarDay, isSelected: Boolean, onClick: (CalendarDay) -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // This is important for square-sizing!
            .testTag("MonthDay")
            .padding(2.dp)
            .clip(CircleShape)
            .background(simpleTextBackground(isSelected))
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
                color = simpleTextColor(isSelected),
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1280, widthDp = 800, device = PIXEL_TABLET)
@Composable
private fun Example10Preview() {
    Example10Page()
}

private val headerBackground = Color(0xFFF1F1F1)
