package com.kizitonwose.calendarsample.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendarcompose.*
import com.kizitonwose.calendarsample.ContinuousSelectionHelper.getSelection
import com.kizitonwose.calendarsample.DateSelection
import com.kizitonwose.calendarsample.R
import com.kizitonwose.calendarsample.displayText
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val primaryColor = Color.Black.copy(alpha = 0.9f)
private val selectionColor = primaryColor
private val continuousSelectionColor = Color.LightGray.copy(alpha = 0.3f)

@Composable
fun Example2Page(
    close: () -> Unit = {},
    dateSelected: (startDate: LocalDate, endDate: LocalDate) -> Unit = { _, _ -> },
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth }
    val endMonth = remember { currentMonth.plusMonths(10) }
    val today = remember { LocalDate.now() }
    var selection by remember { mutableStateOf(DateSelection()) }
    val daysOfWeek = remember { daysOfWeek() }
    ToggleStatusBarColor(Color.White, isLight = true)
    MaterialTheme(colors = MaterialTheme.colors.copy(primary = primaryColor)) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                val state = rememberCalendarState(
                    startMonth = startMonth,
                    endMonth = endMonth,
                    firstVisibleMonth = currentMonth,
                    firstDayOfWeek = daysOfWeek.first(),
                )
                CalendarTop(
                    daysOfWeek = daysOfWeek,
                    selection = selection,
                    close = close,
                    clearDates = { selection = DateSelection() }
                )
                VerticalCalendar(
                    state = state,
                    contentPadding = PaddingValues(bottom = 100.dp),
                    dayContent = { value ->
                        Day(value,
                            today = today,
                            selection = selection) { day ->
                            if (day.position == DayPosition.MonthDate &&
                                (day.date == today || day.date.isAfter(today))
                            ) {
                                selection = getSelection(
                                    clickedDate = day.date,
                                    selectionStartDate = selection.startDate,
                                    selectionEndDate = selection.endDate,
                                )
                            }
                        }
                    },
                    monthHeader = { month -> MonthHeader(month) }
                )
            }
            CalendarBottom(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .background(Color.White)
                    .align(Alignment.BottomCenter),
                selection = selection,
                save = {
                    val (startDate, endDate) = selection
                    if (startDate != null && endDate != null) {
                        dateSelected(startDate, endDate)
                    }
                }
            )
        }
    }
}

@Composable
private fun Day(
    day: CalendarDay,
    today: LocalDate,
    selection: DateSelection,
    onClick: (CalendarDay) -> Unit,
) {
    var textColor = Color.Transparent
    Box(modifier = Modifier
        .aspectRatio(1f) // This is important for square-sizing!
        .clickable(
            enabled = day.position == DayPosition.MonthDate && day.date >= today,
            showRipple = false,
            onClick = { onClick(day) }
        )
        .backgroundHighlight(
            day = day,
            today = today,
            selection = selection,
            selectionColor = selectionColor,
            continuousSelectionColor = continuousSelectionColor) { textColor = it },
        contentAlignment = Alignment.Center) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun MonthHeader(calendarMonth: CalendarMonth) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 12.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)) {
        Text(
            textAlign = TextAlign.Center,
            text = calendarMonth.yearMonth.displayText(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CalendarTop(
    modifier: Modifier = Modifier,
    daysOfWeek: List<DayOfWeek>,
    selection: DateSelection,
    close: () -> Unit,
    clearDates: () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.height(IntrinsicSize.Max),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .clickable(onClick = close)
                        .padding(10.dp),
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Close",
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .clickable(onClick = clearDates)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    text = "Clear",
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End,
                )
            }
            val daysBetween = selection.daysBetween
            val text = if (daysBetween == null) "Select dates" else {
                // Ideally you'd do this using the strings.xml file
                "$daysBetween ${if (daysBetween == 1L) "night" else "nights"} in Munich"
            }
            Text(
                modifier = Modifier.padding(horizontal = 14.dp),
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            )
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)) {
                for (dayOfWeek in daysOfWeek) {
                    Text(
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        text = dayOfWeek.displayText(),
                        fontSize = 15.sp,
                    )
                }
            }
        }
        Divider()
    }
}


@Composable
private fun CalendarBottom(
    modifier: Modifier = Modifier,
    selection: DateSelection,
    save: () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Divider()
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "€75 night",
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                modifier = Modifier
                    .height(40.dp)
                    .width(100.dp),
                onClick = save,
                enabled = selection.daysBetween != null
            ) {
                Text(text = "Save")
            }
        }
    }
}


@Preview(heightDp = 800)
@Composable
private fun Example2Preview() {
    Example2Page()
}
