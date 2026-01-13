
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.minusDays
import com.kizitonwose.calendar.core.now
import com.kizitonwose.calendar.core.plusDays
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.Padding
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun Example7Page() {
    val currentDate = remember { LocalDate.now() }
    val startDate = remember { currentDate.minusDays(500) }
    val endDate = remember { currentDate.plusDays(500) }
    var selection by remember { mutableStateOf<LocalDate?>(null) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(LocalScaffoldPaddingValues.current),
    ) {
        val state = rememberWeekCalendarState(
            startDate = startDate,
            endDate = endDate,
            firstVisibleWeekDate = currentDate,
        )
        // Draw light content on dark background.
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            WeekCalendar(
                modifier = Modifier.padding(vertical = 4.dp),
                state = state,
                calendarScrollPaged = false,
                dayContent = { day ->
                    Day(
                        // If paged scrolling is disabled (calendarScrollPaged = false),
                        // you must set the day width on the WeekCalendar!
                        modifier = Modifier.width(this@BoxWithConstraints.maxWidth / 9f),
                        date = day.date,
                        selected = selection == day.date,
                    ) {
                        selection = it
                    }
                },
                // Draw a thin border around each week.
                weekContainer = { week, container ->
                    Box(
                        modifier = Modifier
                            .padding(start = 3.dp, top = 3.dp, bottom = 3.dp)
                            .border(
                                color = Colors.primary,
                                width = 2.dp,
                                shape = RoundedCornerShape(8.dp),
                            )
                            .padding(3.dp)
                            .clip(shape = RoundedCornerShape(8.dp)),
                    ) {
                        container()
                    }
                },
            )
        }
    }
}

private val dateFormatter by lazy {
    LocalDate.Format {
        dayOfMonth(Padding.ZERO)
    }
}

@Composable
private fun Day(
    date: LocalDate,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (LocalDate) -> Unit = {},
) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color = Colors.example5ToolbarColor)
            .border(
                shape = RoundedCornerShape(8.dp),
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) Colors.accent else Color.Transparent,
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
