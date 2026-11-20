package io.github.ceniorpomidor.workcalendar.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceType
import io.github.ceniorpomidor.workcalendar.domain.model.CalendarArt
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.PayoutKind
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftDisplayState
import io.github.ceniorpomidor.workcalendar.domain.pay.PayoutInstance
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import io.github.ceniorpomidor.workcalendar.ui.theme.StatusPalette
import java.time.DayOfWeek
import java.time.LocalDate

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

internal enum class CalendarMark(
    val label: String,
    val description: String,
    private val dayInk: Long,
    private val nightInk: Long,
    val icon: () -> ImageVector,
    val isWork: Boolean = false,
) {
    SHIFT("Смена", "Смена запланирована", 0xFF355775, 0xFFB7CDE2, { AppIcons.Work }, isWork = true),
    STARTED("Идёт", "Смена идёт сейчас", 0xFF23635C, 0xFFA0D8CE, { AppIcons.Schedule }, isWork = true),
    AWAITING("Часы?", "Смена окончена, часы ещё не отмечены", 0xFF7A511E, 0xFFE5C18C, { AppIcons.Pending }, isWork = true),
    CONFIRMED("Учтено", "Часы подтверждены", 0xFF3D6144, 0xFFBCD5AC, { AppIcons.Check }, isWork = true),
    EXTRA("Доп.", "Дополнительная смена", 0xFF685182, 0xFFCCBAE2, { AppIcons.Add }, isWork = true),
    MISSED("Пропуск", "Смена не состоялась", 0xFF873F37, 0xFFE2A8A0, { AppIcons.Close }),
    CANCELLED("Отмена", "Смена отменена", 0xFF5D575F, 0xFFCEC5CF, { AppIcons.Remove }),
    MOVED("Перенос", "Смена перенесена", 0xFF545682, 0xFFC4C4E2, { AppIcons.ArrowForward }),
    VACATION("Отпуск", "Отпуск", 0xFF506133, 0xFFD0D8A7, { AppIcons.Vacation }),
    UNPAID("Б/с", "Отпуск за свой счёт", 0xFF735741, 0xFFE1C6AB, { AppIcons.Vacation }),
    SICK("Больн.", "Больничный", 0xFF834963, 0xFFE6B7C8, { AppIcons.Sick }),
    ABSENCE("Отсут.", "Другое отсутствие", 0xFF575C66, 0xFFD2D3DF, { AppIcons.EventBusy }),
    ;

    fun ink(style: CalendarStyle) = Color(if (style.dark) nightInk else dayInk)

    fun fill(style: CalendarStyle) = lerp(style.paper, ink(style), if (style.dark) 0.22f else 0.14f)
}

internal fun DayInfo?.calendarMark(): CalendarMark? = when (this?.state) {
    DayState.OFF, null -> null
    DayState.PLANNED -> CalendarMark.SHIFT
    DayState.IN_PROGRESS -> CalendarMark.STARTED
    DayState.AWAITING -> CalendarMark.AWAITING
    DayState.CONFIRMED -> CalendarMark.CONFIRMED
    DayState.EXTRA -> CalendarMark.EXTRA
    DayState.MISSED -> CalendarMark.MISSED
    DayState.CANCELLED -> CalendarMark.CANCELLED
    DayState.MOVED -> CalendarMark.MOVED
    DayState.VACATION -> if (absence?.type == AbsenceType.UNPAID) CalendarMark.UNPAID else CalendarMark.VACATION
    DayState.SICK -> CalendarMark.SICK
    DayState.OTHER_ABSENCE -> CalendarMark.ABSENCE
}

private val markerText = TextStyle(
    fontSize = 9.sp,
    lineHeight = 11.sp,
    fontWeight = FontWeight.SemiBold,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

private val tagShape = RoundedCornerShape(4.dp)

/** Height of [WeekHeader]; the calendar layout reserves exactly this space. */
internal val WEEK_HEADER_HEIGHT = 26.dp

@Composable
internal fun WeekHeader(days: List<DayOfWeek>, style: CalendarStyle) {
    Row(
        Modifier.fillMaxWidth().height(WEEK_HEADER_HEIGHT).calendarRule(style.rule),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (day in days) {
            val weekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    Formats.weekdayShort(day),
                    style = style.weekdayStyle,
                    color = if (weekend) style.holiday else style.secondaryInk,
                    maxLines = 1,
                )
            }
        }
    }
}

private fun Modifier.calendarRule(color: Color) = drawBehind {
    drawLine(color, Offset(0f, size.height - 0.5.dp.toPx()), Offset(size.width, size.height - 0.5.dp.toPx()), 0.7.dp.toPx())
}

@Composable
internal fun DayCell(
    day: CalendarDay,
    info: DayInfo?,
    selected: Boolean,
    today: LocalDate,
    showHolidays: Boolean,
    compact: Boolean,
    height: Dp,
    style: CalendarStyle,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    joinsLeft: Boolean = false,
    joinsRight: Boolean = false,
) {
    val inMonth = day.position == DayPosition.MonthDate
    val isToday = day.date == today
    val holiday = showHolidays && info?.specialDayOff == true
    val mark = info.calendarMark()
    val hasNote = !info?.note.isNullOrBlank()
    val hasExtra = info?.hasExtra == true && mark != CalendarMark.EXTRA
    val payouts = info?.payouts.orEmpty()
    val payout = payouts.takeIf { it.isNotEmpty() }?.let { payments ->
        val label = payments.singleOrNull()?.rule?.kind?.let(::payoutMark) ?: "₽"
        label to payments.all { it.payment != null }
    }
    val hasStatus = mark != null || payout != null
    val small = height < 38.dp
    val density = LocalDensity.current
    val symbolOnly = small || density.fontScale > 1.25f
    val numberHeight = if (small) 16.dp else 23.dp
    val markerHeight = if (small) 11.dp else 14.dp
    val shape = RoundedCornerShape(8.dp)
    val cellBackground = if (style.art == CalendarArt.CLASSIC) mark?.fill(style) ?: Color.Transparent else Color.Transparent
    val decoration = when {
        style.art == CalendarArt.CLASSIC -> Modifier.padding(1.dp).clip(shape).background(cellBackground).border(0.7.dp, style.rule, shape)
        else -> Modifier.calendarRule(style.rule)
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(height)
            .alpha(if (inMonth) 1f else 0.58f)
            .then(decoration)
            .then(if (selected) Modifier.border(1.5.dp, style.accent, shape) else Modifier)
            .semantics {
                this.selected = selected
                contentDescription = if (payout != null) "День выплаты, ${Formats.date(day.date)}" else Formats.dayTitle(day.date)
                val details = buildList {
                    if (isToday) add("Сегодня")
                    if (mark != null) add(mark.description)
                    if (holiday) add(info?.holidayName ?: "Праздничный или перенесённый выходной")
                    if (hasExtra) add("Есть дополнительная смена")
                    for (payment in payouts) add(payment.accessibilityLabel())
                    if (hasNote) add("Есть заметка")
                }
                if (details.isNotEmpty()) stateDescription = details.joinToString(". ")
            }
            .combinedClickable(
                onClickLabel = if (payout != null) "Внести полученную сумму" else "Выбрать день",
                onLongClickLabel = "Добавить смену",
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(top = if (small) 0.dp else 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CalendarDate(
                number = day.date.dayOfMonth,
                style = style,
                mark = mark,
                variant = Math.floorMod(day.date.toEpochDay(), 4L).toInt(),
                today = isToday,
                holiday = holiday,
                small = small,
                modifier = Modifier.fillMaxWidth().height(numberHeight),
                joinsLeft = joinsLeft,
                joinsRight = joinsRight,
            )
            if (hasStatus) {
                Row(
                    Modifier.fillMaxWidth().height(markerHeight).padding(horizontal = 1.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (mark != null) {
                        CalendarMarkTag(mark, style, symbolOnly = symbolOnly, modifier = Modifier.weight(1f, fill = false))
                    }
                    if (payout != null) PayoutTag(payout.first, payout.second, style, symbolOnly)
                }
            }
            if (info != null && !small) {
                val lineHeight = with(density) { markerText.lineHeight.toDp() }
                val available = height - numberHeight - (if (hasStatus) markerHeight else 0.dp) - 5.dp
                val lineBudget = (available.value / lineHeight.value).toInt().coerceIn(0, 3)
                val details = when (mark) {
                    CalendarMark.VACATION, CalendarMark.UNPAID, CalendarMark.SICK, CalendarMark.CANCELLED, CalendarMark.MISSED -> emptyList()
                    else -> info.lines
                }
                val showEarnings = height >= 60.dp && info.earned != null && lineBudget > 0
                if (!compact) {
                    val reserved = if (showEarnings) 1 else 0
                    details.take(lineBudget - reserved).forEach { DayCaption(it, style) }
                }
                if (showEarnings) {
                    info.earned?.let { DayCaption(compactMoney(it), style) }
                }
            }
        }
        if (hasExtra) Icon(
            AppIcons.Add,
            contentDescription = null,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 2.dp, top = 2.dp).size(10.dp),
            tint = CalendarMark.EXTRA.ink(style),
        )
        if (hasNote) Icon(
            AppIcons.EditNote,
            contentDescription = null,
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 2.dp, top = 2.dp).size(10.dp),
            tint = style.secondaryInk,
        )
    }
}

private fun PayoutInstance.accessibilityLabel(): String =
    if (payment != null) "${rule.name}, сумма внесена" else "${rule.name}, ожидается"

private fun payoutMark(kind: PayoutKind): String = when (kind) {
    PayoutKind.ADVANCE -> "А"
    PayoutKind.SALARY -> "З"
    PayoutKind.OTHER -> "₽"
}

@Composable
private fun DayCaption(text: String, style: CalendarStyle) {
    Text(
        text,
        style = markerText.copy(fontSize = 10.sp, fontWeight = FontWeight.Normal),
        color = style.secondaryInk,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
internal fun CalendarMarkTag(mark: CalendarMark, style: CalendarStyle, symbolOnly: Boolean, modifier: Modifier = Modifier) {
    val ink = mark.ink(style)
    Box(
        modifier.background(mark.fill(style), tagShape).padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (symbolOnly || mark == CalendarMark.CONFIRMED) {
            Icon(mark.icon(), contentDescription = null, modifier = Modifier.size(11.dp), tint = ink)
        } else {
            Text(mark.label, style = markerText, color = ink, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
internal fun PayoutTag(label: String, received: Boolean, style: CalendarStyle, symbolOnly: Boolean = false) {
    val ink = if (style.dark) Color(0xFFE7C897) else Color(0xFF775123)
    val fill = if (received) lerp(style.paper, ink, 0.3f) else style.paper
    Box(
        Modifier.widthIn(min = 13.dp).background(fill, tagShape).border(0.75.dp, ink.copy(alpha = 0.72f), tagShape).padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (symbolOnly) {
            Icon(AppIcons.Wallet, contentDescription = null, modifier = Modifier.size(10.dp), tint = ink)
        } else {
            Text(label, style = markerText, color = ink, maxLines = 1)
        }
    }
}

/** Each item uses the actual marker shown in this theme. */
@Composable
internal fun LegendDialog(style: CalendarStyle, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Обозначения") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                CalendarMark.entries.forEach { mark ->
                    LegendRow(mark.description) { CalendarMarkTag(mark, style, symbolOnly = false) }
                }
                LegendRow("День аванса") { PayoutTag("А", false, style) }
                LegendRow("День зарплаты") { PayoutTag("З", false, style) }
                LegendRow("Другая выплата или несколько выплат в один день") { PayoutTag("₽", false, style) }
                LegendRow("Сумма выплаты внесена") { PayoutTag("А", true, style) }
                LegendRow("К дню есть заметка") {
                    Icon(AppIcons.EditNote, contentDescription = null, tint = style.secondaryInk, modifier = Modifier.size(18.dp))
                }
                LegendRow("Праздник или перенесённый выходной") {
                    Text("23", color = style.holiday, style = style.dateStyle(small = false))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Понятно") } },
    )
}

@Composable
private fun LegendRow(label: String, marker: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        Box(Modifier.widthIn(min = 50.dp), contentAlignment = Alignment.Center) { marker() }
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
