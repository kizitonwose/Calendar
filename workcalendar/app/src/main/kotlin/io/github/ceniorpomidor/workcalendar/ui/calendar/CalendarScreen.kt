package io.github.ceniorpomidor.workcalendar.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.domain.alarm.AlarmPlanner
import io.github.ceniorpomidor.workcalendar.domain.model.CalendarArt
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceType
import io.github.ceniorpomidor.workcalendar.domain.model.AlarmItem
import io.github.ceniorpomidor.workcalendar.domain.model.AlarmRepeat
import io.github.ceniorpomidor.workcalendar.domain.model.AlarmSettings
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.pay.PeriodSummary
import io.github.ceniorpomidor.workcalendar.domain.time.TimeMath
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.LocalSnackbar
import io.github.ceniorpomidor.workcalendar.ui.components.launchSafely
import io.github.ceniorpomidor.workcalendar.ui.components.money
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs

@Composable
fun CalendarScreen(
    container: AppContainer,
    initialDate: LocalDate?,
    onConfirmShift: (Long) -> Unit,
    onEditShift: (Long) -> Unit,
    onNewShift: (LocalDate, Boolean) -> Unit,
    onNewAbsence: (AbsenceType, LocalDate) -> Unit,
    onOpenAbsence: (Long) -> Unit,
    onOpenUnconfirmed: () -> Unit,
    onOpenFinance: () -> Unit,
    onSearch: () -> Unit,
    onSetupSchedule: () -> Unit,
    onEditAlarm: (Long) -> Unit,
    onAddAlarm: (LocalDate, Int) -> Unit,
    onOpenPayout: (String) -> Unit,
    onNewScheduleFrom: (LocalDate) -> Unit,
) {
    val vm: CalendarViewModel = viewModel { CalendarViewModel(container) }
    val state by vm.state.collectAsStateWithLifecycle()
    val selected by vm.selectedDate.collectAsStateWithLifecycle()
    val selectedDay by vm.selectedDay.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val today = container.clock.today()
    val currentMonth = remember { YearMonth.from(today) }
    var menuOpen by remember { mutableStateOf(false) }
    var legend by rememberSaveable { mutableStateOf(false) }

    // The state is created once: new inputs of rememberCalendarState would recreate it and reset the position.
    val firstDayOfWeek = state.settings.calendar.firstDayOfWeek
    val calendarState = rememberCalendarState(
        startMonth = currentMonth.minusMonths(120),
        endMonth = currentMonth.plusMonths(60),
        firstVisibleMonth = remember { initialDate?.let { YearMonth.from(it) } ?: currentMonth },
        firstDayOfWeek = remember { firstDayOfWeek },
        outDateStyle = OutDateStyle.EndOfGrid,
    )
    LaunchedEffect(firstDayOfWeek) {
        if (calendarState.firstDayOfWeek != firstDayOfWeek) calendarState.firstDayOfWeek = firstDayOfWeek
    }
    LaunchedEffect(initialDate) {
        if (initialDate != null) {
            vm.select(initialDate)
            calendarState.scrollToMonth(YearMonth.from(initialDate))
        }
    }
    // The month that takes most of the width: switches in the middle of a swipe, not when the
    // previous month has completely left the screen.
    val visibleMonth by remember(calendarState) { derivedStateOf { calendarState.mostVisibleMonth() } }
    LaunchedEffect(calendarState) {
        // A swipe must always end on a whole month, even if its animation was interrupted.
        snapshotFlow { calendarState.isScrollInProgress }.collect { scrolling ->
            if (scrolling) return@collect
            // A drag and the fling after it are separate scrolls: never step in between them.
            delay(SETTLE_DELAY_MS)
            if (!calendarState.isScrollInProgress && !calendarState.isAligned()) {
                try {
                    calendarState.animateScrollToMonth(calendarState.mostVisibleMonth())
                } catch (e: CancellationException) {
                    // The user touched the calendar again; keep watching unless the screen is gone.
                    ensureActive()
                }
            }
        }
    }
    LaunchedEffect(calendarState) {
        snapshotFlow { visibleMonth }.distinctUntilChanged().collectLatest { month ->
            // Data of neighbouring months is already loaded: reload only when the swipe has settled.
            calendarState.awaitSettled()
            vm.onMonthVisible(month)
        }
    }

    // The selection stays when months are swiped and may be outside the loaded months.
    val dayInfo = state.days[selected] ?: selectedDay?.takeIf { it.date == selected }
    val dayAlarms = if (selected.isBefore(today)) {
        null
    } else {
        dayAlarmsUi(container, state.settings.alarm, selected, dayInfo, today, scope, snackbar, onEditAlarm, onAddAlarm)
    }

    var choosingPayout by rememberSaveable { mutableStateOf(false) }
    var editing by rememberSaveable { mutableStateOf(false) }
    val actions = ShiftActions(
        edit = { onEditShift(it.id) },
        move = { shift, date -> scope.launchSafely(snackbar, success = "Смена перенесена на ${Formats.shortDate(date)}") { container.shifts.move(shift.id, date) } },
        cancel = { scope.launchSafely(snackbar, success = "Смена отменена") { container.shifts.cancel(it.id) } },
        restore = { scope.launchSafely(snackbar, success = "Смена восстановлена") { container.shifts.restore(it.id) } },
        delete = { scope.launchSafely(snackbar, success = "Смена удалена") { container.shifts.delete(it.id) } },
        missed = { scope.launchSafely(snackbar) { container.shifts.markMissed(it.id) } },
    )
    val editActions = DayEditActions(
        addShift = { extra -> onNewShift(selected, extra) },
        addAbsence = { type -> onNewAbsence(type, selected) },
        openAbsence = { onOpenAbsence(it.id) },
        saveNote = { text -> scope.launchSafely(snackbar) { vm.saveNote(selected, text) } },
        shift = actions,
        newScheduleFrom = onNewScheduleFrom,
        previewStop = { date, manual -> container.schedule.previewStop(date, manual) },
        manualShiftsFrom = { container.schedule.manualShiftsFrom(it) },
        stopFrom = { date, manual ->
            scope.launchSafely(snackbar, success = "Смены с ${Formats.shortDate(date)} удалены") { container.schedule.stopFrom(date, manual) }
        },
    )
    // "Отметить": hours of the shift of the day, otherwise the list of shifts without hours.
    val markShift = dayInfo?.mainShift()?.takeIf { it.status == ShiftStatus.PLANNED || it.isClosed }
    val markAwaiting = markShift?.status == ShiftStatus.PLANNED && !state.now.isBefore(markShift.plannedEnd)
    val markLabel = when {
        markShift?.isClosed == true -> "Исправить"
        markShift != null -> "Отметить"
        state.unconfirmedTotal > 0 -> "Отметить (${state.unconfirmedTotal})"
        else -> "Отметить"
    }
    val otherUnconfirmed = if (markShift != null) state.unconfirmedTotal - (if (markAwaiting) 1 else 0) else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(Formats.monthTitle(visibleMonth), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                actions = {
                    IconButton(onClick = { scope.launch { calendarState.animateScrollToMonth(visibleMonth.minusMonths(1)) } }) {
                        Icon(AppIcons.ChevronLeft, contentDescription = "Предыдущий месяц")
                    }
                    IconButton(onClick = { scope.launch { calendarState.animateScrollToMonth(visibleMonth.plusMonths(1)) } }) {
                        Icon(AppIcons.ChevronRight, contentDescription = "Следующий месяц")
                    }
                    IconButton(onClick = {
                        vm.select(today)
                        scope.launch { calendarState.animateScrollToMonth(currentMonth) }
                    }) { Icon(AppIcons.Today, contentDescription = "Сегодня") }
                    Box {
                        IconButton(onClick = { menuOpen = true }) { Icon(AppIcons.MoreVert, contentDescription = "Ещё") }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Поиск") }, leadingIcon = { Icon(AppIcons.Search, null) },
                                onClick = {
                                    menuOpen = false
                                    onSearch()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Обозначения") }, leadingIcon = { Icon(AppIcons.Help, null) },
                                onClick = {
                                    menuOpen = false
                                    legend = true
                                },
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        val art = state.settings.calendar.art
        val showSummary = state.settings.calendar.showMonthSummary
        val summaryRow: @Composable () -> Unit = {
            MonthSummaryRow(state.summaries[visibleMonth], onOpenFinance)
        }
        val calendar: @Composable (Dp) -> Unit = { cellHeight ->
            Column(Modifier.padding(horizontal = 6.dp).monthSheet(art)) {
                WeekHeader(daysOfWeek(firstDayOfWeek), street = art == CalendarArt.GRAFFITI)
                HorizontalCalendar(
                    state = calendarState,
                    dayContent = { day ->
                        val info = state.days[day.date]
                        val click: () -> Unit = {
                            vm.select(day.date)
                            val payouts = info?.payouts.orEmpty()
                            if (payouts.size == 1) onOpenPayout(payouts.single().key)
                            if (payouts.size > 1) choosingPayout = true
                        }
                        val longClick: () -> Unit = {
                            vm.select(day.date)
                            onNewShift(day.date, state.days[day.date]?.shifts?.any { it.isActive } == true)
                        }
                        if (art == CalendarArt.CLASSIC) {
                            DayCell(
                                day = day,
                                info = info,
                                selected = day.date == selected,
                                today = today,
                                showHolidays = state.settings.calendar.showHolidays,
                                compact = state.settings.calendar.compactDays,
                                height = cellHeight,
                                onClick = click,
                                onLongClick = longClick,
                            )
                        } else {
                            val date = day.date
                            val band = art == CalendarArt.BANDS
                            ArtDay(
                                art, day, info, date == selected, today, state.settings.calendar.showHolidays, cellHeight,
                                state.settings.calendar.compactDays,
                                joinsLeft = band && date.dayOfWeek != firstDayOfWeek && state.days[date.minusDays(1)]?.shifts?.any { it.isActive } == true,
                                joinsRight = band && date.plusDays(1).dayOfWeek != firstDayOfWeek && state.days[date.plusDays(1)]?.shifts?.any { it.isActive } == true,
                                onClick = click, onLongClick = longClick,
                            )
                        }
                    },
                )
            }
        }
        // The day panel takes the space under the calendar and never scrolls: the calendar does not
        // move while months are swiped, and everything about the day is seen at once.
        val details: @Composable (Modifier) -> Unit = { modifier ->
            DayPanel(
                info = dayInfo,
                date = selected,
                calc = state.calc,
                now = state.now,
                alarms = dayAlarms?.alarms.orEmpty(),
                otherUnconfirmed = otherUnconfirmed,
                hasSchedule = state.loading || state.hasSchedule,
                markLabel = markLabel,
                markEnabled = markShift != null || state.unconfirmedTotal > 0,
                onEdit = { editing = true },
                onMark = { if (markShift != null) onConfirmShift(markShift.id) else onOpenUnconfirmed() },
                onOpenAbsence = { onOpenAbsence(it.id) },
                onOpenPayout = { onOpenPayout(it.key) },
                onOpenUnconfirmed = onOpenUnconfirmed,
                onSetupSchedule = onSetupSchedule,
                modifier = if (art == CalendarArt.CLASSIC) modifier else modifier.monthSheet(if (art == CalendarArt.GRAFFITI) CalendarArt.BANDS else art),
            )
        }

        BoxWithConstraints(Modifier.fillMaxSize().padding(padding).calendarBackdrop(art)) {
            if (maxWidth > maxHeight) {
                // Landscape and large screens: the calendar on the left, the day on the right.
                val calendarWidth = maxWidth * 0.55f
                val byWidth = (calendarWidth - 12.dp) / 7 * 1.25f
                val byHeight = (maxHeight - WEEK_HEADER_HEIGHT - 8.dp) / 6
                val cellHeight = minOf(byWidth, byHeight).coerceAtLeast(28.dp)
                Row(Modifier.fillMaxSize()) {
                    Column(Modifier.width(calendarWidth).padding(top = 4.dp)) { calendar(cellHeight) }
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        if (showSummary) summaryRow()
                        details(Modifier.weight(1f))
                    }
                }
            } else {
                val byWidth = (maxWidth - 12.dp) / 7 * 1.25f
                val summaryHeight = if (showSummary) SUMMARY_ROW_HEIGHT else 0.dp
                val byHeight = (maxHeight - summaryHeight - WEEK_HEADER_HEIGHT - MIN_PANEL_HEIGHT) / 6
                val cellHeight = minOf(byWidth, byHeight).coerceIn(40.dp, 96.dp)
                Column(Modifier.fillMaxSize()) {
                    if (showSummary) summaryRow()
                    calendar(cellHeight)
                    details(Modifier.weight(1f))
                }
            }
        }
    }
    if (choosingPayout && !dayInfo?.payouts.isNullOrEmpty()) {
        AlertDialog(
            onDismissRequest = { choosingPayout = false },
            title = { Text("Выплаты за ${Formats.shortDate(selected)}") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    dayInfo?.payouts.orEmpty().forEach { payout ->
                        TextButton(onClick = {
                            choosingPayout = false
                            onOpenPayout(payout.key)
                        }) { Text(payout.title.replaceFirstChar { it.uppercase() }) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { choosingPayout = false }) { Text("Закрыть") } },
        )
    }
    if (legend) LegendDialog(state.settings.calendar.compactDays) { legend = false }
    if (editing) {
        DayEditSheet(dayInfo, selected, state.calc?.zone ?: java.time.ZoneId.systemDefault(), dayAlarms, editActions, onDismiss = { editing = false })
    }
}

/** Alarms of the selected day; switching one off here affects this day only. */
private fun dayAlarmsUi(
    container: AppContainer,
    settings: AlarmSettings,
    date: LocalDate,
    info: DayInfo?,
    today: LocalDate,
    scope: CoroutineScope,
    snackbar: SnackbarHostState,
    onEdit: (Long) -> Unit,
    onAdd: (LocalDate, Int) -> Unit,
): DayAlarmsUi {
    val shifts = info?.shifts.orEmpty()
    val day = Formats.shortDate(date)
    // Before the shift of the day, as a working day alarm would ring; a day without shifts — in the morning.
    val suggested = AlarmPlanner.workShift(date, shifts)?.plannedStart
        ?.minusMinutes(AlarmItem.DEFAULT_MINUTES_BEFORE.toLong())
        ?.takeIf { it.toLocalDate() == date }
    return DayAlarmsUi(
        alarms = AlarmPlanner.forDay(settings, date, shifts),
        suggestedMinute = suggested?.let { TimeMath.minuteOfDay(it.toLocalTime()) } ?: DEFAULT_ALARM_MINUTE,
        onToggle = { alarm, on ->
            val item = alarm.ring.alarm
            scope.launchSafely(snackbar, success = if (on) "Будильник на $day включён" else "Будильник на $day выключен") {
                container.settings.update { s ->
                    val updated = if (item.repeat == AlarmRepeat.ONCE) s.alarm.setEnabled(item.id, on) else s.alarm.skip(item.id, date, !on)
                    s.copy(alarm = updated.cleaned(today))
                }
            }
        },
        onEdit = onEdit,
        onAdd = { minute -> onAdd(date, minute) },
    )
}

/** Alarm time offered for a day without shifts. */
private const val DEFAULT_ALARM_MINUTE = 7 * 60

/** Approximate height of the summary row, used to size the calendar cells. */
private val SUMMARY_ROW_HEIGHT = 64.dp

/** Space kept for the day panel under the calendar in portrait. */
private val MIN_PANEL_HEIGHT = 184.dp

/** Pause after a scroll before the calendar counts as settled (a fling may follow a drag). */
private const val SETTLE_DELAY_MS = 150L

/** Suspends until the calendar has stopped scrolling, including the fling after a drag. */
private suspend fun CalendarState.awaitSettled() {
    do {
        snapshotFlow { isScrollInProgress }.first { !it }
        delay(SETTLE_DELAY_MS)
    } while (isScrollInProgress)
}

/** The month that occupies the largest part of the calendar viewport. */
private fun CalendarState.mostVisibleMonth(): YearMonth {
    val info = layoutInfo
    val viewportEnd = info.viewportEndOffset
    val best = info.visibleMonthsInfo.maxByOrNull { item ->
        minOf(item.offset + item.size, viewportEnd) - maxOf(item.offset, info.viewportStartOffset)
    }
    return best?.month?.yearMonth ?: firstVisibleMonth.yearMonth
}

/** True when the most visible month starts at the edge of the viewport (a whole month is shown). */
private fun CalendarState.isAligned(): Boolean {
    val target = mostVisibleMonth()
    val item = layoutInfo.visibleMonthsInfo.firstOrNull { it.month.yearMonth == target } ?: return true
    return abs(item.offset - layoutInfo.viewportStartOffset) <= 1
}

/** Totals of the month; a month that is not loaded yet keeps the same layout with dashes. */
@Composable
private fun MonthSummaryRow(summary: PeriodSummary?, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceContainerLow,
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            SummaryValue("Часы / план", summary?.let { "${Formats.hoursDecimal(it.workedMinutes)} / ${Formats.hoursDecimal(it.plannedMinutes)}" } ?: "—", Modifier.weight(1f))
            VerticalDivider(Modifier.height(26.dp), color = colors.outlineVariant)
            SummaryValue("Заработано", summary?.let { money(it.confirmedEarnings) } ?: "—", Modifier.weight(1.2f), colors.primary)
            VerticalDivider(Modifier.height(26.dp), color = colors.outlineVariant)
            SummaryValue("Смены", summary?.let { "${it.workedShifts} из ${it.scheduledShifts}" } ?: "—", Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryValue(label: String, value: String, modifier: Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}
