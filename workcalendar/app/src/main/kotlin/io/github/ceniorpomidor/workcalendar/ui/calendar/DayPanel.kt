package io.github.ceniorpomidor.workcalendar.ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ceniorpomidor.workcalendar.data.repo.CalcContext
import io.github.ceniorpomidor.workcalendar.domain.alarm.AlarmOfDay
import io.github.ceniorpomidor.workcalendar.domain.model.Absence
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceType
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftDisplayState
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftKind
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.pay.PayoutInstance
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.Tag
import io.github.ceniorpomidor.workcalendar.ui.components.money
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import io.github.ceniorpomidor.workcalendar.ui.theme.AppTheme
import java.time.LocalDate
import java.time.LocalDateTime

/** Actions available for a shift of the selected day. */
class ShiftActions(
    val edit: (Shift) -> Unit,
    val move: (Shift, LocalDate) -> Unit,
    val cancel: (Shift) -> Unit,
    val restore: (Shift) -> Unit,
    val delete: (Shift) -> Unit,
    val missed: (Shift) -> Unit,
)

/** Shift shown in the day panel: the first active one, otherwise a cancelled or moved one. */
fun DayInfo.mainShift(): Shift? {
    val visible = shifts.filter { it.status != ShiftStatus.COVERED || absence == null }
    return visible.firstOrNull { it.isActive } ?: visible.firstOrNull()
}

/** Short state of a shift for the day panel. */
fun ShiftDisplayState.shortTitle(): String = when (this) {
    ShiftDisplayState.UPCOMING -> "Смена"
    ShiftDisplayState.IN_PROGRESS -> "Идёт"
    ShiftDisplayState.AWAITING_CONFIRMATION -> "Не отмечена"
    ShiftDisplayState.CONFIRMED -> "Отмечена"
    ShiftDisplayState.MISSED -> "Не была"
    ShiftDisplayState.CANCELLED -> "Отменена"
    ShiftDisplayState.MOVED -> "Перенесена"
    ShiftDisplayState.COVERED -> "Отсутствие"
    ShiftDisplayState.DELETED -> "Удалена"
}

/**
 * The main facts about the selected day and two buttons: "Изменить" opens the list of changes,
 * "Отметить" enters the hours. Fits the space under the calendar: the facts are cut, the buttons
 * always stay visible.
 */
@Composable
fun DayPanel(
    info: DayInfo?,
    date: LocalDate,
    calc: CalcContext?,
    now: LocalDateTime,
    alarms: List<AlarmOfDay>,
    otherUnconfirmed: Int,
    hasSchedule: Boolean,
    markLabel: String,
    markEnabled: Boolean,
    onEdit: () -> Unit,
    onMark: () -> Unit,
    onOpenAbsence: (Absence) -> Unit,
    onOpenPayout: (PayoutInstance) -> Unit,
    onOpenUnconfirmed: () -> Unit,
    onSetupSchedule: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        Column(Modifier.weight(1f, fill = true), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    Formats.dayTitle(date).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                info?.holidayName?.let {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.labelMedium,
                        color = AppTheme.status.holiday,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 180.dp),
                    )
                }
            }
            MainLine(info, calc, now, hasSchedule, onOpenAbsence, onSetupSchedule)
            info?.payouts?.forEach { payout -> PayoutLine(payout, onClick = { onOpenPayout(payout) }) }
            ExtrasLine(alarms, info?.note)
            if (otherUnconfirmed > 0) {
                InfoLine(
                    AppIcons.Pending,
                    "Не отмечены часы: ${Formats.shifts(otherUnconfirmed)}",
                    AppTheme.status.onAwaiting,
                    onClick = onOpenUnconfirmed,
                )
            }
        }
        Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                Icon(AppIcons.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Изменить", maxLines = 1)
            }
            Button(onClick = onMark, enabled = markEnabled, modifier = Modifier.weight(1f)) {
                Icon(AppIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(markLabel, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun MainLine(
    info: DayInfo?,
    calc: CalcContext?,
    now: LocalDateTime,
    hasSchedule: Boolean,
    onOpenAbsence: (Absence) -> Unit,
    onSetupSchedule: () -> Unit,
) {
    val absence = info?.absence
    val shift = info?.mainShift()
    val zone = calc?.zone ?: java.time.ZoneId.systemDefault()
    when {
        absence != null -> {
            val p = AppTheme.status
            val (bg, fg) = when (absence.type) {
                AbsenceType.VACATION, AbsenceType.UNPAID -> p.vacation to p.onVacation
                AbsenceType.SICK -> p.sick to p.onSick
                AbsenceType.OTHER -> p.otherAbsence to p.onOtherAbsence
            }
            Row(Modifier.fillMaxWidth().clickable { onOpenAbsence(absence) }, verticalAlignment = Alignment.CenterVertically) {
                Tag(absence.displayTitle(), bg, fg)
                Spacer(Modifier.width(8.dp))
                val amount = absence.amount?.let { " · ${money(it)}" } ?: ""
                Text(
                    "${Formats.period(absence.startDate, absence.endDate)}$amount",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        shift != null -> {
            val display = shift.displayState(now)
            val (bg, fg) = display.colors(AppTheme.status)
            val text = when (shift.status) {
                ShiftStatus.CONFIRMED -> {
                    val pay = shift.pay ?: calc?.pay?.calculate(shift, shift.workedMinutes ?: 0)
                    "${Formats.timeRange(shift.plannedStart, shift.plannedEnd)} · ${Formats.hours((shift.workedMinutes ?: 0).toLong())}" +
                        (pay?.let { " · ${money(it.total)}" } ?: "")
                }
                ShiftStatus.MOVED -> shift.movedToDate?.let { "на ${Formats.shortDate(it)}" } ?: Formats.timeRange(shift.plannedStart, shift.plannedEnd)
                else -> "${Formats.timeRange(shift.plannedStart, shift.plannedEnd)} · ${Formats.hours(shift.plannedPaidMinutes(zone))}"
            }
            val more = (info.shifts.count { it.isActive } - if (shift.isActive) 1 else 0).takeIf { it > 0 }?.let { " +$it" } ?: ""
            Row(verticalAlignment = Alignment.CenterVertically) {
                Tag(if (shift.kind == ShiftKind.EXTRA) "Доп. смена" else display.shortTitle(), bg, fg)
                Spacer(Modifier.width(8.dp))
                Text(
                    text + more,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        !hasSchedule -> InfoLine(AppIcons.EventRepeat, "График не настроен — настроить", MaterialTheme.colorScheme.primary, onClick = onSetupSchedule)
        else -> Text(
            if (info?.specialDayOff == true) "Праздничный выходной" else "Выходной",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PayoutLine(payout: PayoutInstance, onClick: () -> Unit) {
    val received = payout.payment?.amount
    val expected = payout.expectedNet
    val text = buildString {
        append(payout.title.replaceFirstChar { it.uppercase() })
        when {
            received != null -> append(": пришло ${money(received)}")
            expected != null -> append(": ≈ ${money(expected)}")
        }
    }
    InfoLine(AppIcons.Wallet, text, AppTheme.status.onPayday, onClick = onClick)
}

@Composable
private fun ExtrasLine(alarms: List<AlarmOfDay>, note: String?) {
    if (alarms.isEmpty() && note.isNullOrBlank()) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (alarms.isNotEmpty()) {
            val on = alarms.filter { it.on }
            Icon(
                if (on.isEmpty()) AppIcons.AlarmOff else AppIcons.Alarm,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                if (on.isEmpty()) "будильник выключен" else on.joinToString(", ") { Formats.time(it.ring.at) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            Spacer(Modifier.width(12.dp))
        }
        if (!note.isNullOrBlank()) {
            Icon(AppIcons.Note, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(note, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun InfoLine(icon: ImageVector, text: String, color: Color, onClick: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
