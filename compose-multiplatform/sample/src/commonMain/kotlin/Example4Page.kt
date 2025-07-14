
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.now
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.datetime.YearMonth
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun Example4Page() {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth }
    val endMonth = remember { currentMonth.plusMonths(500) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(LocalScaffoldPaddingValues.current),
    ) {
        val state = rememberCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstVisibleMonth = currentMonth,
            firstDayOfWeek = firstDayOfWeekFromLocale(),
        )
        HorizontalCalendar(
            state = state,
            dayContent = { day -> Day(day) },
            monthHeader = { month -> MonthHeader(month) },
            calendarScrollPaged = false,
            contentPadding = PaddingValues(4.dp),
            monthContainer = { _, container ->
                Box(
                    modifier = Modifier
                        .width(maxWidth * 0.73f)
                        .padding(8.dp)
                        .clip(shape = RoundedCornerShape(8.dp))
                        .border(
                            color = Color.Black,
                            width = 1.dp,
                            shape = RoundedCornerShape(8.dp),
                        ),
                ) {
                    container()
                }
            },
            monthBody = { _, content ->
                Box(
                    modifier = Modifier.background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Colors.example6MonthBgColor,
                                Colors.example6MonthBgColor3,
                            ),
                        ),
                    ),
                ) {
                    content()
                }
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
            .background(color = Colors.example6MonthBgColor2)
            .padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = calendarMonth.yearMonth.displayText(short = true),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            for (dayOfWeek in daysOfWeek) {
                Text(
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    text = dayOfWeek.name.first().toString(),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        HorizontalDivider(color = Color.Black)
    }
}

@Composable
private fun Day(day: CalendarDay) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(70.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (day.position == DayPosition.MonthDate) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = day.date.dayOfMonth.toString(),
                color = Color.Black,
                fontSize = 14.sp,
            )
        }
    }
}

@Preview
@Composable
private fun Example4Preview() {
    Example4Page()
}
