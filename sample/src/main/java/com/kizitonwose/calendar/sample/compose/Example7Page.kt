package com.kizitonwose.calendar.sample.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.sample.R
import com.kizitonwose.calendar.sample.shared.displayText
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun Example7Page() {
    val currentDate = remember { LocalDate.now() }
    val startDate = remember { currentDate.minusDays(500) }
    val endDate = remember { currentDate.plusDays(500) }
    var selection by remember { mutableStateOf<LocalDate?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val state = rememberWeekCalendarState(
            startDate = startDate,
            endDate = endDate,
            firstVisibleWeekDate = currentDate,
        )
        // Draw light content on dark background.
        CompositionLocalProvider(LocalContentColor provides darkColors().onSurface) {
            WeekCalendar(
                modifier = Modifier.padding(vertical = 4.dp),
                state = state,
                calendarScrollPaged = false,
                dayContent = { day ->
                    Day(day.date, selected = selection == day.date) {
                        selection = it
                    }
                },
            )
        }
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("dd")

@Composable
private fun Day(
    date: LocalDate,
    selected: Boolean = false,
    onClick: (LocalDate) -> Unit = {},
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    Box(
        modifier = Modifier
            // If paged scrolling is disabled (calendarScrollPaged = false),
            // you must set the day width on the WeekCalendar!
            .width(screenWidth / 9)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color = colorResource(R.color.example_5_toolbar_color))
            .border(
                shape = RoundedCornerShape(8.dp),
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) colorResource(R.color.colorAccent) else Color.Transparent,
            )
            .wrapContentHeight()
            .clickable { onClick(date) },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = date.month.displayText(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
            )
            Text(
                text = dateFormatter.format(date),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = date.dayOfWeek.displayText(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

@Preview
@Composable
private fun Example7Preview() {
    Example7Page()
}
