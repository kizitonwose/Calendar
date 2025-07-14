import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.ContentHeightMode
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.now
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.yearMonth
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun Example8Page(close: () -> Unit = {}) {
    val today = remember { LocalDate.now() }
    val currentMonth = remember(today) { today.yearMonth }
    val startMonth = remember { currentMonth.minusMonths(500) }
    val endMonth = remember { currentMonth.plusMonths(500) }
    val selections = remember { mutableStateListOf<CalendarDay>() }
    val daysOfWeek = remember { daysOfWeek() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colors.example1BgLight)
            .padding(top = 20.dp)
            .applyScaffoldHorizontalPaddings()
            .applyScaffoldTopPadding(),
    ) {
        // Draw light content on dark background.
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            var selectedIndex by remember { mutableIntStateOf(0) }
            PageOptions(selectedIndex, close = close) { selectedIndex = it }
            val state = rememberCalendarState(
                startMonth = startMonth,
                endMonth = endMonth,
                firstVisibleMonth = currentMonth,
                firstDayOfWeek = daysOfWeek.first(),
                outDateStyle = OutDateStyle.EndOfGrid,
            )
            val coroutineScope = rememberCoroutineScope()
            val visibleMonth = rememberFirstVisibleMonthAfterScroll(state)
            SimpleCalendarTitle(
                modifier = Modifier.padding(bottom = 14.dp, top = 4.dp, start = 8.dp, end = 8.dp),
                isHorizontal = selectedIndex == 0,
                currentMonth = visibleMonth.yearMonth,
                goToPrevious = {
                    coroutineScope.launch {
                        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.previous)
                    }
                },
                goToNext = {
                    coroutineScope.launch {
                        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.next)
                    }
                },
            )
            FullScreenCalendar(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Colors.example1Bg)
                    .testTag("Calendar"),
                state = state,
                horizontal = selectedIndex == 0,
                dayContent = { day ->
                    Day(
                        day = day,
                        isSelected = selections.contains(day),
                        isToday = day.position == DayPosition.MonthDate && day.date == today,
                    ) { clicked ->
                        if (selections.contains(clicked)) {
                            selections.remove(clicked)
                        } else {
                            selections.add(clicked)
                        }
                    }
                },
                // The month body is only needed for ui test tag.
                monthBody = { _, content ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("MonthBody"),
                    ) {
                        content()
                    }
                },
                monthHeader = {
                    MonthHeader(daysOfWeek = daysOfWeek)
                },
                monthFooter = { month ->
                    val count = month.weekDays.flatten()
                        .count { selections.contains(it) }
                    MonthFooter(
                        modifier = Modifier.applyScaffoldBottomPadding(),
                        selectionCount = count,
                    )
                },
            )
        }
    }
}

@Composable
private fun FullScreenCalendar(
    modifier: Modifier,
    state: CalendarState,
    horizontal: Boolean,
    dayContent: @Composable BoxScope.(CalendarDay) -> Unit,
    monthHeader: @Composable ColumnScope.(CalendarMonth) -> Unit,
    monthBody: @Composable ColumnScope.(CalendarMonth, content: @Composable () -> Unit) -> Unit,
    monthFooter: @Composable ColumnScope.(CalendarMonth) -> Unit,
) {
    if (horizontal) {
        HorizontalCalendar(
            modifier = modifier,
            state = state,
            calendarScrollPaged = true,
            contentHeightMode = ContentHeightMode.Fill,
            dayContent = dayContent,
            monthBody = monthBody,
            monthHeader = monthHeader,
            monthFooter = monthFooter,
        )
    } else {
        VerticalCalendar(
            modifier = modifier,
            state = state,
            calendarScrollPaged = true,
            contentHeightMode = ContentHeightMode.Fill,
            dayContent = dayContent,
            monthBody = monthBody,
            monthHeader = monthHeader,
            monthFooter = monthFooter,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PageOptions(selectedIndex: Int, close: () -> Unit = {}, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!isMobile()) {
            Button(
                onClick = close,
                colors = ButtonDefaults.buttonColors().copy(containerColor = Colors.example1Bg),
            ) {
                Text("Close")
            }
        }
        val options = listOf("Horizontal", "Vertical")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    colors = SegmentedButtonDefaults.colors().copy(
                        activeContainerColor = Colors.example1Bg,
                        activeContentColor = Color.White,
                        inactiveContainerColor = Color.Transparent,
                        inactiveContentColor = Color.White,
                    ),
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    onClick = { onSelect(index) },
                    selected = index == selectedIndex,
                ) {
                    Text(label)
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(daysOfWeek: List<DayOfWeek>) {
    Row(
        Modifier
            .fillMaxWidth()
            .testTag("MonthHeader")
            .background(Colors.example1BgSecondary)
            .padding(vertical = 8.dp),
    ) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                text = dayOfWeek.displayText(),
            )
        }
    }
}

@Composable
private fun MonthFooter(selectionCount: Int, modifier: Modifier = Modifier) {
    Box(
        Modifier
            .fillMaxWidth()
            .testTag("MonthFooter")
            .background(Colors.example1BgSecondary)
            .padding(vertical = 10.dp)
            .then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        val text = when (selectionCount) {
            0 -> "No selections in this month"
            1 -> "$selectionCount selection in this month"
            else -> "$selectionCount selections in this month"
        }
        Text(text = text)
    }
}

@Composable
private fun Day(
    day: CalendarDay,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: (CalendarDay) -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RectangleShape)
            .background(
                color = when {
                    isSelected -> Colors.example1Selection
                    isToday -> Colors.example1WhiteLight
                    else -> Color.Transparent
                },
            )
            // Disable clicks on inDates/outDates
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                showRipple = !isSelected,
                onClick = { onClick(day) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        val textColor = when (day.position) {
            // Color.Unspecified will use the default text color from the current theme
            DayPosition.MonthDate -> if (isSelected) Colors.example1Bg else Color.Unspecified
            DayPosition.InDate, DayPosition.OutDate -> Colors.example1WhiteLight
        }
        Text(
            text = day.date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 15.sp,
        )
    }
}

@Preview
@Composable
private fun Example8Preview() {
    Example8Page()
}
