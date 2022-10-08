package com.kizitonwose.calendar.sample.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.sample.*
import com.kizitonwose.calendar.sample.Flight.Airport
import com.kizitonwose.calendar.sample.R
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.YearMonth
import java.util.*

private val flights = generateFlights().groupBy { it.time.toLocalDate() }

private val pageBackGroundColor: Color @Composable get() = colorResource(R.color.example_5_page_bg_color)
private val itemBackGroundColor: Color @Composable get() = colorResource(R.color.example_5_item_view_bg_color)
private val toolbarColor: Color @Composable get() = colorResource(R.color.example_5_toolbar_color)
private val activeTextColor: Color @Composable get() = colorResource(R.color.example_5_text_grey)
private val inActiveTextColor: Color @Composable get() = colorResource(R.color.example_5_text_grey_light)

@Composable
fun Example3Page() {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(500) }
    val endMonth = remember { currentMonth.plusMonths(500) }
    var selection by remember { mutableStateOf<CalendarDay?>(null) }
    val daysOfWeek = remember { daysOfWeek() }
    val flightsInSelectedDate = remember {
        derivedStateOf {
            val date = selection?.date
            if (date == null) emptyList() else flights[date].orEmpty()
        }
    }
    StatusBarColorUpdateEffect(toolbarColor)
    Column(Modifier
        .fillMaxHeight()
        .background(color = pageBackGroundColor)) {
        val state = rememberCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstVisibleMonth = currentMonth,
            firstDayOfWeek = daysOfWeek.first(),
            outDateStyle = OutDateStyle.EndOfGrid,
        )
        val coroutineScope = rememberCoroutineScope()
        val visibleMonth = rememberFirstCompletelyVisibleMonthNonNull(state)
        LaunchedEffect(visibleMonth) {
            // Clear selection if we scroll to a new month.
            selection = null
        }

        // Draw light content on dark background.
        CompositionLocalProvider(LocalContentColor provides darkColors().onSurface) {
            CalendarTitle(
                modifier = Modifier
                    .background(toolbarColor)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                currentMonth = visibleMonth,
                goToPrevious = {
                    coroutineScope.launch {
                        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.previousMonth)
                    }
                },
                goToNext = {
                    coroutineScope.launch {
                        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.nextMonth)
                    }
                }
            )
            HorizontalCalendar(
                modifier = Modifier.wrapContentWidth(),
                state = state,
                dayContent = { day ->
                    CompositionLocalProvider(LocalRippleTheme provides Example3RippleTheme) {
                        val colors = if (day.position == DayPosition.MonthDate) {
                            flights[day.date].orEmpty().map { colorResource(it.color) }
                        } else emptyList()
                        Day(day = day,
                            isSelected = selection == day,
                            colors = colors) { clicked ->
                            selection = clicked
                        }
                    }
                },
                monthHeader = {
                    MonthHeader(
                        modifier = Modifier.padding(vertical = 8.dp),
                        daysOfWeek = daysOfWeek,
                    )
                }
            )
            Divider(color = pageBackGroundColor)
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(items = flightsInSelectedDate.value) { flight ->
                    FlightInformation(flight)
                }
            }
        }
    }
}

@Composable
private fun Day(
    day: CalendarDay,
    isSelected: Boolean = false,
    colors: List<Color> = emptyList(),
    onClick: (CalendarDay) -> Unit = {},
) {
    Box(Modifier
        .aspectRatio(1f) // This is important for square-sizing!
        .border(
            width = if (isSelected) 1.dp else 0.dp,
            color = if (isSelected) activeTextColor else Color.Transparent)
        .padding(1.dp)
        .background(color = itemBackGroundColor)
        // Disable clicks on inDates/outDates
        .clickable(
            enabled = day.position == DayPosition.MonthDate,
            onClick = { onClick(day) }
        )) {
        val textColor = when (day.position) {
            DayPosition.MonthDate -> Color.Unspecified
            DayPosition.InDate, DayPosition.OutDate -> inActiveTextColor
        }
        Text(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 3.dp, end = 4.dp),
            text = day.date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 12.sp,
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            for (color in colors) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(color))
            }
        }
    }
}

@Composable
private fun MonthHeader(
    modifier: Modifier = Modifier,
    daysOfWeek: List<DayOfWeek> = emptyList(),
) {
    Row(modifier = modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = Color.White,
                text = dayOfWeek.displayText(uppercase = true),
                fontWeight = FontWeight.Light,
            )
        }
    }
}

@Composable
private fun LazyItemScope.FlightInformation(flight: Flight) {
    Row(
        modifier = Modifier
            .fillParentMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(modifier = Modifier
            .background(color = colorResource(flight.color))
            .fillParentMaxWidth(1 / 7f)
            .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = flightDateTimeFormatter.format(flight.time).uppercase(Locale.ENGLISH),
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
                fontSize = 12.sp,
            )
        }
        Box(modifier = Modifier
            .background(color = itemBackGroundColor)
            .weight(1f)
            .fillMaxHeight()) {
            AirportInformation(flight.departure, isDeparture = true)
        }
        Box(
            modifier = Modifier
                .background(color = itemBackGroundColor)
                .weight(1f)
                .fillMaxHeight(),
        ) {
            AirportInformation(flight.destination, isDeparture = false)
        }
    }
    Divider(color = pageBackGroundColor, thickness = 2.dp)
}

@Composable
private fun AirportInformation(airport: Airport, isDeparture: Boolean) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()) {
        val resource = if (isDeparture) {
            R.drawable.ic_airplane_takeoff
        } else R.drawable.ic_airplane_landing
        Box(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight()
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Image(painter = painterResource(resource), contentDescription = null)
        }
        Column(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = airport.code,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = airport.city,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

// The default dark them ripple is too bright so we tone it down.
private object Example3RippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = RippleTheme.defaultRippleColor(Color.Gray, lightTheme = false)

    @Composable
    override fun rippleAlpha() = RippleTheme.defaultRippleAlpha(Color.Gray, lightTheme = false)
}

@Preview(heightDp = 600)
@Composable
private fun Example3Preview() {
    Example3Page()
}
