package com.kizitonwose.calendarsample.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
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
import com.kizitonwose.calendarsample.ContinuousSelectionHelper.getSelection
import com.kizitonwose.calendarsample.ContinuousSelectionHelper.isInDateBetween
import com.kizitonwose.calendarsample.ContinuousSelectionHelper.isOutDateBetween
import com.kizitonwose.calendarsample.DateSelection
import com.kizitonwose.calendarsample.R
import com.kizitonwose.calendarsample.displayText
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val primaryColor = Color.Black.copy(alpha = 0.9f)

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
                    calendarScrollPaged = false,
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
    Box(
        Modifier
            .aspectRatio(1f) // This is important for square-sizing!
            .backgroundHighlight(day, today = today, selection = selection) { textColor = it }
            .clickable(
                enabled = day.position == DayPosition.MonthDate && day.date >= today,
                showRipple = false,
                onClick = { onClick(day) }
            ),
        contentAlignment = Alignment.Center
    ) {
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
    daysOfWeek: List<DayOfWeek>,
    selection: DateSelection,
    close: () -> Unit,
    clearDates: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        .padding(horizontal = 16.dp),
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
            Row(modifier = Modifier.fillMaxWidth()) {
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

private fun Modifier.backgroundHighlight(
    day: CalendarDay,
    today: LocalDate,
    selection: DateSelection,
    textColor: (Color) -> Unit,
): Modifier = composed {
    val (startDate, endDate) = selection
    val padding = 4.dp
    when (day.position) {
        DayPosition.MonthDate -> {
            when {
                day.date.isBefore(today) -> {
                    textColor(colorResource(R.color.inactive_text_color))
                    this
                }
                startDate == day.date && endDate == null -> {
                    textColor(Color.White)
                    padding(padding)
                        .background(color = primaryColor, shape = CircleShape)
                }
                day.date == startDate -> {
                    textColor(Color.White)
                    padding(vertical = padding)
                        .background(
                            color = primaryColor,
                            shape = RoundedCornerShape(
                                topStartPercent = 50,
                                bottomStartPercent = 50
                            )
                        )
                }
                startDate != null && endDate != null && (day.date > startDate && day.date < endDate) -> {
                    textColor(Color.White)
                    padding(vertical = padding)
                        .background(color = primaryColor)
                }
                day.date == endDate -> {
                    textColor(Color.White)
                    padding(vertical = padding)
                        .background(
                            color = primaryColor,
                            shape = RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
                        )
                }
                day.date == today -> {
                    textColor(colorResource(R.color.example_4_grey))
                    padding(padding)
                        .border(
                            width = 1.dp,
                            shape = CircleShape,
                            color = colorResource(R.color.inactive_text_color))
                }
                else -> {
                    textColor(colorResource(R.color.example_4_grey))
                    this
                }
            }
        }
        DayPosition.InDate -> {
            textColor(Color.Transparent)
            if (startDate != null &&
                endDate != null &&
                isInDateBetween(day.date, startDate, endDate)
            ) {
                padding(vertical = padding)
                    .background(color = primaryColor)
            } else this
        }
        DayPosition.OutDate -> {
            textColor(Color.Transparent)
            if (startDate != null &&
                endDate != null &&
                isOutDateBetween(day.date, startDate, endDate)
            ) {
                padding(vertical = padding)
                    .background(color = primaryColor)
            } else this
        }
    }
}


@Preview(heightDp = 700)
@Composable
private fun Example2Preview() {
    Example2Page()
}

