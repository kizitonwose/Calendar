import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.VerticalYearCalendar
import com.kizitonwose.calendar.compose.yearcalendar.YearContentHeightMode
import com.kizitonwose.calendar.compose.yearcalendar.rememberYearCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.ExperimentalCalendarApi
import com.kizitonwose.calendar.core.Year
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.now
import com.kizitonwose.calendar.core.plusYears
import kotlinx.datetime.YearMonth
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalCalendarApi::class)
@Composable
fun Example11Page(adjacentYears: Int = 50) {
    val currentMonth = remember { YearMonth.now() }
    val currentYear = remember { Year(currentMonth.year) }
    val endYear = remember { currentYear.plusYears(adjacentYears) }
    val selections = remember { mutableStateListOf<CalendarDay>() }
    val daysOfWeek = remember { daysOfWeek() }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val isTablet = maxWidth >= 600.dp
        val state = rememberYearCalendarState(
            startYear = currentYear,
            endYear = endYear,
            firstVisibleYear = currentYear,
            firstDayOfWeek = daysOfWeek.first(),
        )
        VerticalYearCalendar(
            modifier = Modifier
                .fillMaxSize()
                .testTag("Calendar"),
            contentPadding = LocalScaffoldPaddingValues.current +
                PaddingValues(horizontal = if (isTablet) 52.dp else 10.dp),
            state = state,
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
            calendarScrollPaged = false,
            contentHeightMode = YearContentHeightMode.Wrap,
            monthVerticalSpacing = 20.dp,
            monthHorizontalSpacing = if (isTablet) 52.dp else 10.dp,
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp),
            text = calendarMonth.yearMonth.month.displayText(short = false),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 20.dp)
                .testTag("MonthHeader"),
            fontSize = 52.sp,
            text = year.toString(),
            fontWeight = FontWeight.Medium,
        )
        HorizontalDivider()
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
            .background(color = if (isSelected) Colors.example1Selection else Color.Transparent)
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
                fontSize = if (isTablet) 10.sp else 9.sp,
                color = if (isSelected) Color.White else Color.Unspecified,
            )
        }
    }
}

@Preview
@Composable
private fun Example11Preview() {
    Example11Page()
}
