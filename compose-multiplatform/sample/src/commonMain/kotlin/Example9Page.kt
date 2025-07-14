import Example9PageSharedComponents.CalendarHeader
import Example9PageSharedComponents.Day
import Example9PageSharedComponents.MonthAndWeekCalendarTitle
import Example9PageSharedComponents.WeekModeToggle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.WeekCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.WeekDayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.minusDays
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.now
import com.kizitonwose.calendar.core.plusDays
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Go to [Example9PageAnimatedVisibility] to see how toggling between week and
 * month modes can be done using [AnimatedVisibility] if that interests you.
 */
@Composable
fun Example9Page(adjacentMonths: Int = 500) {
    val currentDate = remember { LocalDate.now() }
    val currentMonth = remember(currentDate) { currentDate.yearMonth }
    val startMonth = remember(currentDate) { currentMonth.minusMonths(adjacentMonths) }
    val endMonth = remember(currentDate) { currentMonth.plusMonths(adjacentMonths) }
    val selections = remember { mutableStateListOf<LocalDate>() }
    val daysOfWeek = remember { daysOfWeek() }

    var isWeekMode by remember { mutableStateOf(false) }
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
        )
        CalendarHeader(daysOfWeek = daysOfWeek)
        val monthCalendarAlpha by animateFloatAsState(if (isWeekMode) 0f else 1f)
        val weekCalendarAlpha by animateFloatAsState(if (isWeekMode) 1f else 0f)
        var weekCalendarSize by remember { mutableStateOf(DpSize.Zero) }
        val visibleMonth = rememberFirstVisibleMonthAfterScroll(monthState)
        val weeksInVisibleMonth = visibleMonth.weekDays.count()
        val monthCalendarHeight by animateDpAsState(
            if (isWeekMode) {
                weekCalendarSize.height
            } else {
                weekCalendarSize.height * weeksInVisibleMonth
            },
            tween(durationMillis = 250),
        )
        val density = LocalDensity.current
        Box {
            HorizontalCalendar(
                modifier = Modifier
                    .height(monthCalendarHeight)
                    .alpha(monthCalendarAlpha)
                    .zIndex(if (isWeekMode) 0f else 1f),
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
            WeekCalendar(
                modifier = Modifier
                    .wrapContentHeight()
                    .onSizeChanged {
                        val size = density.run { DpSize(it.width.toDp(), it.height.toDp()) }
                        if (weekCalendarSize != size) {
                            weekCalendarSize = size
                        }
                    }
                    .alpha(weekCalendarAlpha)
                    .zIndex(if (isWeekMode) 1f else 0f),
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
                isWeekMode = weekMode
            }
        }
    }
}

@Composable
private fun CalendarTitle(
    isWeekMode: Boolean,
    monthState: CalendarState,
    weekState: WeekCalendarState,
) {
    val visibleMonth = rememberFirstVisibleMonthAfterScroll(monthState)
    val visibleWeek = rememberFirstVisibleWeekAfterScroll(weekState)
    MonthAndWeekCalendarTitle(
        isWeekMode = isWeekMode,
        currentMonth = if (isWeekMode) visibleWeek.days.first().date.yearMonth else visibleMonth.yearMonth,
        monthState = monthState,
        weekState = weekState,
    )
}

object Example9PageSharedComponents {
    @Composable
    fun MonthAndWeekCalendarTitle(
        isWeekMode: Boolean,
        currentMonth: YearMonth,
        monthState: CalendarState,
        weekState: WeekCalendarState,
    ) {
        val coroutineScope = rememberCoroutineScope()
        SimpleCalendarTitle(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            currentMonth = currentMonth,
            goToPrevious = {
                coroutineScope.launch {
                    if (isWeekMode) {
                        val targetDate = weekState.firstVisibleWeek.days.first().date.minusDays(1)
                        weekState.animateScrollToWeek(targetDate)
                    } else {
                        val targetMonth = monthState.firstVisibleMonth.yearMonth.previous
                        monthState.animateScrollToMonth(targetMonth)
                    }
                }
            },
            goToNext = {
                coroutineScope.launch {
                    if (isWeekMode) {
                        val targetDate = weekState.firstVisibleWeek.days.last().date.plusDays(1)
                        weekState.animateScrollToWeek(targetDate)
                    } else {
                        val targetMonth = monthState.firstVisibleMonth.yearMonth.next
                        monthState.animateScrollToMonth(targetMonth)
                    }
                }
            },
        )
    }

    @Composable
    fun CalendarHeader(daysOfWeek: List<DayOfWeek>) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            for (dayOfWeek in daysOfWeek) {
                Text(
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    text = dayOfWeek.displayText(),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }

    @Composable
    fun Day(
        day: LocalDate,
        isSelected: Boolean,
        isSelectable: Boolean,
        onClick: (LocalDate) -> Unit,
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f) // This is important for square-sizing!
                .padding(6.dp)
                .clip(CircleShape)
                .background(color = if (isSelected) Colors.example1Selection else Color.Transparent)
                .clickable(
                    enabled = isSelectable,
                    showRipple = !isSelected,
                    onClick = { onClick(day) },
                ),
            contentAlignment = Alignment.Center,
        ) {
            val textColor = when {
                isSelected -> Color.White
                isSelectable -> Color.Unspecified
                else -> Colors.example4GrayPast
            }
            Text(
                text = day.dayOfMonth.toString(),
                color = textColor,
                fontSize = 14.sp,
            )
        }
    }

    @Composable
    fun WeekModeToggle(
        modifier: Modifier,
        isWeekMode: Boolean,
        weekModeToggled: (isWeekMode: Boolean) -> Unit,
    ) {
        // We want the entire content to be clickable, not just the checkbox.
        Row(
            modifier = modifier
                .padding(10.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable { weekModeToggled(!isWeekMode) }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        ) {
            Checkbox(
                checked = isWeekMode,
                onCheckedChange = null, // Check is handled by parent.
                colors = CheckboxDefaults.colors(checkedColor = Colors.example1Selection),
            )
            Text(text = "Week mode")
        }
    }
}

@Preview
@Composable
private fun Example9Preview() {
    Example9Page()
}
