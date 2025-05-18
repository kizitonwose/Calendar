package com.kizitonwose.calendar.sample.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices.PIXEL_7
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
import com.kizitonwose.calendar.sample.shared.yearsUntil
import kotlinx.coroutines.launch
import java.time.Year
import kotlin.math.abs

@OptIn(ExperimentalCalendarApi::class)
@Composable
fun Example10Page(adjacentYears: Long = 50) {
    val currentYear = remember { Year.now() }
    val startYear = remember { currentYear.minusYears(adjacentYears) }
    val endYear = remember { currentYear.plusYears(adjacentYears) }
    val selections = remember { mutableStateListOf<CalendarDay>() }
    val daysOfWeek = remember { daysOfWeek() }
    val config = LocalConfiguration.current
    val isTablet = config.smallestScreenWidthDp >= 600
    val isPortrait = config.screenHeightDp > config.screenWidthDp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(LocalScaffoldPaddingValues.current),
    ) {
        val scope = rememberCoroutineScope()
        val state = rememberYearCalendarState(
            startYear = startYear,
            endYear = endYear,
            firstVisibleYear = currentYear,
            firstDayOfWeek = daysOfWeek.first(),
        )
        val visibleYear = rememberFirstVisibleYearAfterScroll(state).year
        val headerState = rememberLazyListState()
        LaunchedEffect(visibleYear) {
            val index = startYear.yearsUntil(visibleYear).toInt()
            headerState.animateScrollAndCenterItem(index)
        }
        YearHeader(
            startYear = startYear,
            endYear = endYear,
            visibleYear = visibleYear,
            headerState = headerState,
            isTablet = isTablet,
        ) click@{ targetYear ->
            if (targetYear == visibleYear) return@click
            scope.launch {
                if (abs(visibleYear.yearsUntil(targetYear)) <= 8) {
                    state.animateScrollToYear(targetYear)
                } else {
                    val nearbyYear = if (targetYear > visibleYear) {
                        targetYear.minusYears(5)
                    } else {
                        targetYear.plusYears(5)
                    }
                    state.scrollToYear(nearbyYear)
                    state.animateScrollToYear(targetYear)
                }
            }
        }
        HorizontalYearCalendar(
            modifier = Modifier
                .fillMaxSize()
                .testTag("Calendar"),
            state = state,
            monthColumns = if (isPortrait) {
                3
            } else {
                if (isTablet) 4 else 6
            },
            dayContent = { day ->
                Day(
                    day = day,
                    isSelected = selections.contains(day),
                    isTablet = isTablet,
                ) { clicked ->
                    if (selections.contains(clicked)) {
                        selections.remove(clicked)
                    } else {
                        selections.add(clicked)
                    }
                }
            },
            contentHeightMode = YearContentHeightMode.Fill,
            monthHorizontalSpacing = if (isTablet) {
                if (isPortrait) 52.dp else 92.dp
            } else {
                10.dp
            },
            monthVerticalSpacing = if (isTablet) 20.dp else 4.dp,
            yearBodyContentPadding = if (isTablet) {
                PaddingValues(horizontal = if (isPortrait) 52.dp else 92.dp, vertical = 20.dp)
            } else {
                PaddingValues(all = 10.dp)
            },
            monthHeader = {
                MonthHeader(
                    calendarMonth = it,
                    isTablet = isTablet,
                )
            },
        )
    }
}

@Composable
private fun YearHeader(
    startYear: Year,
    endYear: Year,
    visibleYear: Year,
    headerState: LazyListState,
    isTablet: Boolean,
    modifier: Modifier = Modifier,
    onClick: (Year) -> Unit,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(headerBackground),
        state = headerState,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = headerState, SnapPosition.Center),
        contentPadding = PaddingValues(horizontal = if (isTablet) 40.dp else 10.dp),
    ) {
        items(count = startYear.yearsUntil(endYear).toInt()) { index ->
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
                    .padding(
                        horizontal = if (isTablet) 60.dp else 28.dp,
                        vertical = if (isTablet) 10.dp else 6.dp,
                    ),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = year.value.toString(),
                    textAlign = TextAlign.Center,
                    fontSize = if (isTablet) 24.sp else 18.sp,
                    color = simpleTextColor(isSelected),
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Light,
                )
            }
        }
    }
}

@Composable
private fun MonthHeader(
    calendarMonth: CalendarMonth,
    isTablet: Boolean,
    modifier: Modifier = Modifier,
) {
    val daysOfWeek = calendarMonth.weekDays.first().map { it.date.dayOfWeek }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(if (isTablet) 12.dp else 8.dp),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = calendarMonth.yearMonth.month.displayText(short = false),
            fontSize = if (isTablet) 16.sp else 12.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            for (dayOfWeek in daysOfWeek) {
                Text(
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = if (isTablet) 11.sp else 9.sp,
                    text = dayOfWeek.displayText(uppercase = true, narrow = true),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun Day(
    day: CalendarDay,
    isSelected: Boolean,
    isTablet: Boolean,
    onClick: (CalendarDay) -> Unit,
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // This is important for square-sizing!
            .testTag("MonthDay")
            .padding(if (isTablet) 2.dp else 0.dp)
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
                fontSize = if (isTablet) 11.sp else 9.sp,
                color = simpleTextColor(isSelected),
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1280, widthDp = 800, device = PIXEL_TABLET)
@Preview(showBackground = true, heightDp = 800, widthDp = 1280, device = PIXEL_TABLET)
@Preview(showBackground = true, heightDp = 891, widthDp = 411, device = PIXEL_7)
@Preview(showBackground = true, heightDp = 411, widthDp = 891, device = PIXEL_7)
@Composable
private fun Example10Preview() {
    Example10Page()
}

private val headerBackground = Color(0xFFF1F1F1)
private fun simpleTextColor(isSelected: Boolean) =
    if (isSelected) Color.White else Color.Black

private fun simpleTextBackground(isSelected: Boolean) =
    if (isSelected) Color.Black else Color.White
