package io.github.ceniorpomidor.workcalendar.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.github.ceniorpomidor.workcalendar.MainActivity
import io.github.ceniorpomidor.workcalendar.appContainer
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceType
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftDisplayState
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.notifications.Notifications
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private data class WidgetDay(
    val date: LocalDate,
    val inMonth: Boolean,
    val color: Color?,
    val isToday: Boolean,
    val isHoliday: Boolean,
)

private data class WidgetData(
    val month: YearMonth,
    val weekDays: List<DayOfWeek>,
    val days: List<WidgetDay>,
    val todayText: String,
    val summaryText: String,
    val unconfirmed: Int,
    val firstUnconfirmedId: Long?,
)

private val PLANNED = Color(0xFF4A7BD0)
private val AWAITING = Color(0xFFE59A1C)
private val CONFIRMED = Color(0xFF3E9B57)
private val MISSED = Color(0xFFD0453C)
private val VACATION = Color(0xFF1E9AA6)
private val SICK = Color(0xFFC2507F)
private val OTHER = Color(0xFF8D7B68)

private suspend fun loadWidgetData(context: Context): WidgetData {
    val container = context.appContainer
    val clock = container.clock
    val today = clock.today()
    val now = clock.now()
    val calc = container.calc.current()
    val month = YearMonth.from(today)
    val firstDay = calc.settings.calendar.firstDayOfWeek
    val start = month.atDay(1).let { d ->
        var x = d
        while (x.dayOfWeek != firstDay) x = x.minusDays(1)
        x
    }
    val gridRange = DateRange(start, start.plusDays(41))
    val shifts = container.shifts.getRange(gridRange).filter { it.isVisible }
    val absences = container.absences.getAll().filter { it.range.overlaps(gridRange) }
    val days = gridRange.map { date ->
        val absence = absences.firstOrNull { date in it.range }
        val dayShifts = shifts.filter { it.date == date && it.isActive }
        val color = when {
            absence != null -> when (absence.type) {
                AbsenceType.VACATION, AbsenceType.UNPAID -> VACATION
                AbsenceType.SICK -> SICK
                AbsenceType.OTHER -> OTHER
            }
            dayShifts.isEmpty() -> null
            else -> when (dayShifts.map { it.displayState(now) }.minByOrNull { it.ordinal }) {
                ShiftDisplayState.CONFIRMED -> CONFIRMED
                ShiftDisplayState.AWAITING_CONFIRMATION -> AWAITING
                ShiftDisplayState.MISSED -> MISSED
                else -> PLANNED
            }
        }
        WidgetDay(date, YearMonth.from(date) == month, color, date == today, calc.holidays.isSpecialDayOff(date))
    }
    val todayShifts = shifts.filter { it.date == today && it.isActive }
    val todayAbsence = absences.firstOrNull { today in it.range }
    val todayText = when {
        todayAbsence != null -> "Сегодня: ${todayAbsence.displayTitle().lowercase()}"
        todayShifts.isNotEmpty() -> "Сегодня: " + todayShifts.joinToString { Formats.timeRange(it.plannedStart, it.plannedEnd) }
        else -> {
            val next = container.shifts.observeNext(now, 1).first().firstOrNull()
            if (next != null) "Сегодня выходной · далее ${Formats.shortDate(next.date)} ${Formats.time(next.plannedStart)}" else "Сегодня выходной"
        }
    }
    val summary = calc.summaries.summarize(DateRange.month(month), shifts, absences, emptyList(), now)
    val summaryText = "${Formats.hours(summary.workedMinutes)} · ${Formats.money(summary.confirmedEarnings, calc.settings.currency)}"
    val unconfirmed = container.shifts.getUnconfirmed(now)
    return WidgetData(
        month = month,
        weekDays = (0..6).map { firstDay.plus(it.toLong()) },
        days = days,
        todayText = todayText,
        summaryText = summaryText,
        unconfirmed = unconfirmed.size,
        firstUnconfirmedId = unconfirmed.lastOrNull()?.id,
    )
}

private fun openApp(context: Context, route: String): Action {
    val intent = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        data = Uri.parse("workcalendar://widget/${Uri.encode(route)}")
        putExtra(Notifications.EXTRA_ROUTE, route)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    return actionStartActivity(intent)
}

class CalendarWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = try {
            loadWidgetData(context)
        } catch (e: Exception) {
            null
        }
        provideContent {
            GlanceTheme {
                if (data == null) {
                    Box(GlanceModifier.fillMaxSize().background(GlanceTheme.colors.widgetBackground), contentAlignment = Alignment.Center) {
                        Text("Откройте приложение", style = TextStyle(color = GlanceTheme.colors.onSurface))
                    }
                } else {
                    WidgetContent(context, data)
                }
            }
        }
    }
}

@Composable
private fun WidgetContent(context: Context, data: WidgetData) {
    val size = LocalSize.current
    val showButtons = size.height >= 200.dp
    val reserved = if (showButtons) 118.dp else 74.dp
    val cellHeight = ((size.height - reserved) / 7).coerceIn(14.dp, 40.dp)
    val text = GlanceTheme.colors.onSurface
    Column(
        modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.widgetBackground).cornerRadius(20.dp).padding(10.dp),
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth().clickable(openApp(context, "calendar")), verticalAlignment = Alignment.CenterVertically) {
            Text(
                Formats.monthTitle(data.month),
                style = TextStyle(color = text, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                modifier = GlanceModifier.defaultWeight(),
            )
            Text(data.summaryText, style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp))
        }
        Spacer(GlanceModifier.height(4.dp))
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                data.weekDays.forEach { day ->
                    Text(
                        Formats.weekdayShort(day),
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 10.sp, textAlign = TextAlign.Center),
                        modifier = GlanceModifier.defaultWeight(),
                    )
                }
            }
            data.days.chunked(7).forEach { week ->
                Row(modifier = GlanceModifier.fillMaxWidth().height(cellHeight)) {
                    week.forEach { day -> DayCell(context, day, cellHeight) }
                }
            }
        }
        Spacer(GlanceModifier.height(4.dp))
        Text(data.todayText, style = TextStyle(color = text, fontSize = 12.sp), maxLines = 1, modifier = GlanceModifier.clickable(openApp(context, "calendar")))
        if (data.unconfirmed > 0) {
            Text(
                "Без отмеченных часов: ${Formats.shifts(data.unconfirmed)}",
                style = TextStyle(color = ColorProvider(AWAITING), fontSize = 12.sp, fontWeight = FontWeight.Medium),
                maxLines = 1,
                modifier = GlanceModifier.clickable(openApp(context, "unconfirmed")),
            )
        }
        if (showButtons) {
            Spacer(GlanceModifier.height(4.dp))
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Button("+ Смена", onClick = openApp(context, "shift/new?date=${LocalDate.now()}"), modifier = GlanceModifier.defaultWeight())
                Spacer(GlanceModifier.width(8.dp))
                val hoursRoute = data.firstUnconfirmedId?.let { "confirm/$it" } ?: "unconfirmed"
                Button("Отметить часы", onClick = openApp(context, hoursRoute), modifier = GlanceModifier.defaultWeight())
            }
        }
    }
}

@Composable
private fun androidx.glance.layout.RowScope.DayCell(context: Context, day: WidgetDay, height: androidx.compose.ui.unit.Dp) {
    val background = when {
        day.color != null -> ColorProvider(if (day.inMonth) day.color else day.color.copy(alpha = 0.35f))
        day.isToday -> GlanceTheme.colors.primaryContainer
        else -> ColorProvider(Color.Transparent)
    }
    val textColor = when {
        day.color != null -> ColorProvider(Color.White)
        day.isHoliday -> ColorProvider(Color(0xFFD0453C))
        !day.inMonth -> GlanceTheme.colors.outline
        else -> GlanceTheme.colors.onSurface
    }
    Box(
        modifier = GlanceModifier.defaultWeight().height(height).padding(1.dp).clickable(openApp(context, "day/${day.date}")),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = GlanceModifier.fillMaxSize().background(background).cornerRadius(6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                day.date.dayOfMonth.toString(),
                style = TextStyle(
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

class CalendarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalendarWidget()
}

object WidgetUpdater {
    suspend fun updateAll(context: Context) {
        try {
            CalendarWidget().updateAll(context)
        } catch (e: Exception) {
            android.util.Log.w("WorkCalendar", "Widget update failed", e)
        }
    }
}
