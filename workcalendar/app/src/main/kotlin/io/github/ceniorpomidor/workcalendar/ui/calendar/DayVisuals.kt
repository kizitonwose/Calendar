package io.github.ceniorpomidor.workcalendar.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftDisplayState
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.theme.AppTheme
import io.github.ceniorpomidor.workcalendar.ui.theme.StatusPalette
import java.time.DayOfWeek
import java.time.LocalDate

/** Background and content colors of a day state. */
fun DayState.colors(p: StatusPalette, off: Color, onOff: Color): Pair<Color, Color> = when (this) {
    DayState.OFF -> off to onOff
    DayState.PLANNED -> p.planned to p.onPlanned
    DayState.IN_PROGRESS -> p.inProgress to p.onInProgress
    DayState.AWAITING -> p.awaiting to p.onAwaiting
    DayState.CONFIRMED -> p.confirmed to p.onConfirmed
    DayState.MISSED -> p.missed to p.onMissed
    DayState.CANCELLED, DayState.MOVED -> p.cancelled to p.onCancelled
    DayState.EXTRA -> p.extra to p.onExtra
    DayState.VACATION -> p.vacation to p.onVacation
    DayState.SICK -> p.sick to p.onSick
    DayState.OTHER_ABSENCE -> p.otherAbsence to p.onOtherAbsence
}

fun ShiftDisplayState.colors(p: StatusPalette): Pair<Color, Color> = when (this) {
    ShiftDisplayState.UPCOMING -> p.planned to p.onPlanned
    ShiftDisplayState.IN_PROGRESS -> p.inProgress to p.onInProgress
    ShiftDisplayState.AWAITING_CONFIRMATION -> p.awaiting to p.onAwaiting
    ShiftDisplayState.CONFIRMED -> p.confirmed to p.onConfirmed
    ShiftDisplayState.MISSED -> p.missed to p.onMissed
    ShiftDisplayState.CANCELLED, ShiftDisplayState.MOVED, ShiftDisplayState.DELETED -> p.cancelled to p.onCancelled
    ShiftDisplayState.COVERED -> p.vacation to p.onVacation
}

fun ShiftDisplayState.title(): String = when (this) {
    ShiftDisplayState.UPCOMING -> "Запланирована"
    ShiftDisplayState.IN_PROGRESS -> "Идёт сейчас"
    ShiftDisplayState.AWAITING_CONFIRMATION -> "Завершена, часы не отмечены"
    ShiftDisplayState.CONFIRMED -> "Часы подтверждены"
    ShiftDisplayState.MISSED -> "Не состоялась"
    ShiftDisplayState.CANCELLED -> "Отменена"
    ShiftDisplayState.MOVED -> "Перенесена"
    ShiftDisplayState.COVERED -> "Заменена отсутствием"
    ShiftDisplayState.DELETED -> "Удалена"
}

/** Compact amount for calendar cells: 850, 2,4к, 12к. */
fun compactMoney(value: Money): String {
    val rubles = value.kopecks / 100
    return when {
        rubles < 1000 -> rubles.toString()
        rubles < 10_000 -> "${rubles / 1000},${(rubles % 1000) / 100}к"
        else -> "${rubles / 1000}к"
    }
}

/** Height of [WeekHeader]; the calendar layout reserves exactly this space. */
val WEEK_HEADER_HEIGHT = 24.dp

@Composable
fun WeekHeader(days: List<DayOfWeek>) {
    Row(Modifier.fillMaxWidth().height(WEEK_HEADER_HEIGHT), verticalAlignment = Alignment.CenterVertically) {
        for (day in days) {
            val weekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
            Text(
                Formats.weekdayShort(day),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = if (weekend) AppTheme.status.holiday.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun DayCell(
    day: CalendarDay,
    info: DayInfo?,
    selected: Boolean,
    today: LocalDate,
    showHolidays: Boolean,
    compact: Boolean,
    height: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val inMonth = day.position == DayPosition.MonthDate
    val palette = AppTheme.status
    val state = info?.state ?: DayState.OFF
    val (bg, fg) = state.colors(palette, Color.Transparent, MaterialTheme.colorScheme.onSurface)
    val alpha = if (inMonth) 1f else 0.38f
    val isToday = day.date == today
    val holiday = showHolidays && info?.specialDayOff == true
    val payday = info?.payouts?.isNotEmpty() == true
    val fill = when {
        compact && selected -> MaterialTheme.colorScheme.primaryContainer
        compact -> Color.Transparent
        state == DayState.OFF && payday -> palette.payday
        else -> bg
    }
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(2.dp)
            .clip(shape)
            .background(fill.copy(alpha = fill.alpha * alpha))
            .then(if (!compact && selected) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, shape) else Modifier)
            .semantics {
                this.selected = selected
                if (info != null) {
                    stateDescription = when (state) {
                        DayState.OFF -> "Выходной"
                        DayState.PLANNED -> "Запланирована смена"
                        DayState.IN_PROGRESS -> "Идёт смена"
                        DayState.AWAITING -> "Часы не отмечены"
                        DayState.CONFIRMED -> "Часы подтверждены"
                        DayState.MISSED -> "Смена не состоялась"
                        DayState.CANCELLED -> "Смена отменена"
                        DayState.MOVED -> "Смена перенесена"
                        DayState.EXTRA -> "Дополнительная смена"
                        else -> info.absence?.displayTitle().orEmpty()
                    }
                }
                if (payday) contentDescription = "День выплаты, ${Formats.date(day.date)}"
            }
            .combinedClickable(onClickLabel = if (payday) "Внести полученную сумму" else null, onClick = onClick, onLongClick = onLongClick),
    ) {
        if (compact) {
            val numberColor = when {
                isToday -> MaterialTheme.colorScheme.onPrimary
                holiday -> palette.holiday
                selected -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onSurface
            }
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                    Box(
                        Modifier.size(if (height < 40.dp) 20.dp else 28.dp).clip(CircleShape)
                            .background(if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            day.date.dayOfMonth.toString(), style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selected || isToday) FontWeight.Bold else FontWeight.Medium,
                            color = numberColor.copy(alpha = alpha), maxLines = 1,
                        )
                    }
                    if (payday) Text("₽", style = MaterialTheme.typography.labelSmall, color = palette.onPayday.copy(alpha = alpha))
                }
                Row(
                    Modifier.height(7.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    if (state != DayState.OFF) {
                        Spacer(Modifier.size(width = 13.dp, height = 3.dp).clip(CircleShape).background(fg.copy(alpha = alpha)))
                    }
                    if (info?.note != null) {
                        Spacer(Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)))
                    }
                    if (info?.hasExtra == true && state != DayState.EXTRA) {
                        Spacer(Modifier.size(4.dp).clip(CircleShape).background(palette.onExtra.copy(alpha = alpha)))
                    }
                }
                info?.earned?.takeIf { height >= 52.dp }?.let {
                    Text(compactMoney(it), style = MaterialTheme.typography.labelSmall, color = palette.positive.copy(alpha = alpha), maxLines = 1)
                }
            }
        } else {
            Column(Modifier.fillMaxSize().padding(horizontal = 3.dp, vertical = 3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val numberColor = when {
                        isToday -> MaterialTheme.colorScheme.onPrimary
                        holiday -> palette.holiday
                        state == DayState.OFF && payday -> palette.onPayday
                        state == DayState.OFF -> MaterialTheme.colorScheme.onSurface
                        else -> fg
                    }
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            day.date.dayOfMonth.toString(),
                            fontSize = 12.sp,
                            fontWeight = if (isToday || holiday) FontWeight.Bold else FontWeight.Medium,
                            color = numberColor.copy(alpha = alpha),
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    if (payday) {
                        Text("₽", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = (if (state == DayState.OFF) palette.onPayday else fg).copy(alpha = alpha))
                    }
                    if (info?.hasExtra == true) {
                        Text("+", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.onExtra.copy(alpha = alpha))
                    }
                    if (info?.note != null) {
                        Box(Modifier.padding(start = 1.dp).size(5.dp).clip(CircleShape).background(fg.copy(alpha = 0.8f * alpha)))
                    }
                }
                Spacer(Modifier.weight(1f))
                val lines = info?.lines.orEmpty()
                // Low cells (small screens, landscape) show fewer lines instead of cutting them.
                val maxLines = when {
                    height >= 64.dp -> 3
                    height >= 52.dp -> 2
                    else -> 1
                }
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    for (line in lines.take(maxLines)) {
                        Text(
                            line,
                            fontSize = 10.sp,
                            lineHeight = 11.sp,
                            maxLines = 1,
                            color = fg.copy(alpha = alpha),
                            textDecoration = if (state == DayState.CANCELLED) TextDecoration.LineThrough else null,
                            fontWeight = if (state == DayState.CONFIRMED) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                    info?.earned?.takeIf { height >= 60.dp }?.let {
                        Text(
                            compactMoney(it),
                            fontSize = 9.sp,
                            lineHeight = 10.sp,
                            maxLines = 1,
                            color = fg.copy(alpha = 0.85f * alpha),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

/** Explains colors and marks of the calendar. */
@Composable
fun LegendDialog(compact: Boolean, onDismiss: () -> Unit) {
    val p = AppTheme.status
    val items = listOf(
        Triple(p.planned, p.onPlanned, "Смена запланирована"),
        Triple(p.inProgress, p.onInProgress, "Смена идёт сейчас"),
        Triple(p.awaiting, p.onAwaiting, "Смена завершена — отметьте часы"),
        Triple(p.confirmed, p.onConfirmed, "Часы подтверждены"),
        Triple(p.extra, p.onExtra, "Дополнительная смена"),
        Triple(p.missed, p.onMissed, "Смена не состоялась"),
        Triple(p.cancelled, p.onCancelled, "Смена отменена или перенесена"),
        Triple(p.vacation, p.onVacation, "Отпуск"),
        Triple(p.sick, p.onSick, "Больничный"),
        Triple(p.otherAbsence, p.onOtherAbsence, "Другое отсутствие"),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Обозначения") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for ((bg, fg, text) in items) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(if (compact) Color.Transparent else bg),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (compact) {
                                Spacer(Modifier.size(13.dp, 3.dp).clip(CircleShape).background(fg))
                            } else {
                                Text("12", fontSize = 10.sp, color = fg)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(p.payday),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("₽", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.onPayday)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("День выплаты. Нажмите на дату, чтобы внести сумму.", style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                        Text("23", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = p.holiday)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Праздник или перенесённый выходной", style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("К дню есть заметка", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Понятно") } },
    )
}
