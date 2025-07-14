
import Example9PageSharedComponents.CalendarHeader
import Example9PageSharedComponents.Day
import Example9PageSharedComponents.MonthAndWeekCalendarTitle
import Example9PageSharedComponents.WeekModeToggle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.WeekCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.WeekDayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.now
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.yearMonth
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Go to [Example9Page] to see how toggling between week and month
 * modes can be done using the [Modifier] if that interests you.
 */
@Composable
fun Example9PageAnimatedVisibility(adjacentMonths: Int = 500) {
    val currentDate = remember { LocalDate.now() }
    val currentMonth = remember(currentDate) { currentDate.yearMonth }
    val startMonth = remember(currentDate) { currentMonth.minusMonths(adjacentMonths) }
    val endMonth = remember(currentDate) { currentMonth.plusMonths(adjacentMonths) }
    val selections = remember { mutableStateListOf<LocalDate>() }
    val daysOfWeek = remember { daysOfWeek() }

    var isWeekMode by remember { mutableStateOf(false) }
    var isAnimating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(LocalScaffoldPaddingValues.current),
    ) {
        val monthState = rememberCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstVisibleMonth = currentMonth,
            firstDayOfWeek = daysOfWeek.first(),
        )
        val weekState = rememberWeekCalendarState(
            startDate = startMonth.firstDay,
            endDate = endMonth.lastDay,
            firstVisibleWeekDate = currentDate,
            firstDayOfWeek = daysOfWeek.first(),
        )
        CalendarTitle(
            isWeekMode = isWeekMode,
            monthState = monthState,
            weekState = weekState,
            isAnimating = isAnimating,
        )
        CalendarHeader(daysOfWeek = daysOfWeek)
        AnimatedVisibility(visible = !isWeekMode) {
            HorizontalCalendar(
                state = monthState,
                dayContent = { day ->
                    val isSelectable = day.position == DayPosition.MonthDate
                    Day(
                        day.date,
                        isSelected = isSelectable && selections.contains(day.date),
                        isSelectable = isSelectable,
                    ) { clicked ->
                        if (selections.contains(clicked)) {
                            selections.remove(clicked)
                        } else {
                            selections.add(clicked)
                        }
                    }
                },
            )
        }
        AnimatedVisibility(visible = isWeekMode) {
            WeekCalendar(
                state = weekState,
                dayContent = { day ->
                    val isSelectable = day.position == WeekDayPosition.RangeDate
                    Day(
                        day.date,
                        isSelected = isSelectable && selections.contains(day.date),
                        isSelectable = isSelectable,
                    ) { clicked ->
                        if (selections.contains(clicked)) {
                            selections.remove(clicked)
                        } else {
                            selections.add(clicked)
                        }
                    }
                },
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        WeekModeToggle(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            isWeekMode = isWeekMode,
        ) { weekMode ->
            isAnimating = true
            isWeekMode = weekMode
            coroutineScope.launch {
                if (weekMode) {
                    val targetDate = monthState.firstVisibleMonth.weekDays.last().last().date
                    weekState.scrollToWeek(targetDate)
                    weekState.animateScrollToWeek(targetDate) // Trigger a layout pass for title update
                } else {
                    val targetMonth = weekState.firstVisibleWeek.days.first().date.yearMonth
                    monthState.scrollToMonth(targetMonth)
                    monthState.animateScrollToMonth(targetMonth) // Trigger a layout pass for title update
                }
                isAnimating = false
            }
        }
    }
}

@Composable
private fun CalendarTitle(
    isWeekMode: Boolean,
    monthState: CalendarState,
    weekState: WeekCalendarState,
    isAnimating: Boolean,
) {
    val visibleMonth = rememberFirstVisibleMonthAfterScroll(monthState)
    val visibleWeek = rememberFirstVisibleWeekAfterScroll(weekState)
    val visibleWeekMonth = visibleWeek.days.first().date.yearMonth
    // Track animation state to prevent updating the title too early before
    // the correct value is available (after the animation).
    val currentMonth = if (isWeekMode) {
        if (isAnimating) visibleMonth.yearMonth else visibleWeekMonth
    } else {
        if (isAnimating) visibleWeekMonth else visibleMonth.yearMonth
    }
    MonthAndWeekCalendarTitle(
        isWeekMode = isWeekMode,
        currentMonth = currentMonth,
        monthState = monthState,
        weekState = weekState,
    )
}

@Preview
@Composable
private fun Example9PageAnimatedVisibilityPreview() {
    Example9PageAnimatedVisibility()
}
