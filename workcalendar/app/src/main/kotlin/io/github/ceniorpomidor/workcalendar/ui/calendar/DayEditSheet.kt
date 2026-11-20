package io.github.ceniorpomidor.workcalendar.ui.calendar

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.ceniorpomidor.workcalendar.domain.model.Absence
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceType
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftKind
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.schedule.ScheduleCut
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.ConfirmDialog
import io.github.ceniorpomidor.workcalendar.ui.components.DatePickerDialogCompat
import io.github.ceniorpomidor.workcalendar.ui.components.TextInput
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import java.time.LocalDate
import java.time.ZoneId

/** What can be changed for a day from the "Изменить" list. */
class DayEditActions(
    val addShift: (extra: Boolean) -> Unit,
    val addAbsence: (AbsenceType) -> Unit,
    val openAbsence: (Absence) -> Unit,
    val saveNote: (String) -> Unit,
    val shift: ShiftActions,
    /** Opens the schedule templates to apply one from this day. */
    val newScheduleFrom: (LocalDate) -> Unit,
    val previewStop: suspend (LocalDate, Boolean) -> ScheduleCut,
    val manualShiftsFrom: suspend (LocalDate) -> Int,
    val stopFrom: (LocalDate, Boolean) -> Unit,
)

private enum class DayDialog { ALARM, NOTE, MOVE, MISSED, CANCEL, DELETE, STOP }

/** The list of changes of a day, then the dialog of the chosen change; [onDismiss] closes both. */
@Composable
fun DayEditSheet(info: DayInfo?, date: LocalDate, zone: ZoneId, alarms: DayAlarmsUi?, actions: DayEditActions, onDismiss: () -> Unit) {
    var dialog by rememberSaveable { mutableStateOf<DayDialog?>(null) }
    val shift = info?.mainShift()
    when (dialog) {
        null -> EditSheet(info, date, zone, shift, alarms, actions, onDismiss, onDialog = { dialog = it })
        DayDialog.ALARM -> if (alarms != null) AlarmsDialog(date, alarms, onDismiss)
        DayDialog.NOTE -> NoteDialog(date, info?.note.orEmpty(), actions.saveNote, onDismiss)
        DayDialog.MOVE -> if (shift != null) {
            DatePickerDialogCompat(shift.date.plusDays(1), onDismiss = onDismiss) { newDate ->
                actions.shift.move(shift, newDate)
                onDismiss()
            }
        }
        DayDialog.MISSED -> if (shift != null) {
            ConfirmDialog(
                title = "Смена не состоялась?",
                text = "Смена будет отмечена как несостоявшаяся: 0 часов, без начислений.",
                confirmText = "Отметить",
                onConfirm = { actions.shift.missed(shift) },
                onDismiss = onDismiss,
            )
        }
        DayDialog.CANCEL -> if (shift != null) {
            ConfirmDialog(
                title = "Отменить смену?",
                text = "Смена ${Formats.shortDate(shift.date)} останется в календаре как отменённая и не войдёт в учёт. Остальные даты и график не изменятся.",
                confirmText = "Отменить смену",
                dismissText = "Назад",
                onConfirm = { actions.shift.cancel(shift) },
                onDismiss = onDismiss,
            )
        }
        DayDialog.DELETE -> if (shift != null) {
            ConfirmDialog(
                title = "Удалить смену?",
                text = "Смена ${Formats.shortDate(shift.date)} будет удалена. Если она создана по графику, повторно она не появится.",
                confirmText = "Удалить",
                destructive = true,
                onConfirm = { actions.shift.delete(shift) },
                onDismiss = onDismiss,
            )
        }
        DayDialog.STOP -> StopDialog(date, actions, onDismiss)
    }
}

@Composable
private fun EditSheet(
    info: DayInfo?,
    date: LocalDate,
    zone: ZoneId,
    shift: Shift?,
    alarms: DayAlarmsUi?,
    actions: DayEditActions,
    onDismiss: () -> Unit,
    onDialog: (DayDialog) -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Every choice closes the list; a dialog of the choice may follow.
    fun pick(action: () -> Unit) {
        onDismiss()
        action()
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = 12.dp)) {
            Text(
                "Изменить · ${Formats.dayTitle(date)}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            Group("Добавить на этот день")
            if (alarms != null) {
                val on = alarms.alarms.filter { it.on }
                SheetItem(
                    AppIcons.Alarm,
                    "Будильник",
                    if (on.isEmpty()) "Не заведён" else on.joinToString(", ") { Formats.time(it.ring.at) },
                ) { onDialog(DayDialog.ALARM) }
            }
            val hasRegular = info?.shifts.orEmpty().any { it.isActive && it.kind == ShiftKind.REGULAR }
            SheetItem(AppIcons.Add, if (hasRegular) "Доп. смена" else "Смена", "Время, название, своя ставка") {
                pick { actions.addShift(hasRegular || info?.absence != null) }
            }
            val absence = info?.absence
            if (absence == null) {
                SheetItem(AppIcons.Vacation, "Отпуск") { pick { actions.addAbsence(AbsenceType.VACATION) } }
                SheetItem(AppIcons.Medical, "Больничный") { pick { actions.addAbsence(AbsenceType.SICK) } }
                SheetItem(AppIcons.EventBusy, "Другое", "Отгул, командировка, без сохранения…") { pick { actions.addAbsence(AbsenceType.OTHER) } }
            } else {
                SheetItem(AppIcons.Vacation, absence.displayTitle(), "${Formats.period(absence.startDate, absence.endDate)} — открыть") {
                    pick { actions.openAbsence(absence) }
                }
            }
            SheetItem(AppIcons.EditNote, if (info?.note.isNullOrBlank()) "Заметка" else "Изменить заметку") { onDialog(DayDialog.NOTE) }

            if (shift != null) {
                Group("Смена ${Formats.timeRange(shift.plannedStart, shift.plannedEnd)}")
                if (shift.status == ShiftStatus.PLANNED || shift.isClosed) {
                    SheetItem(AppIcons.Edit, "Изменить смену", "Время, перерыв, ставка") { pick { actions.shift.edit(shift) } }
                }
                if (shift.status == ShiftStatus.PLANNED) {
                    SheetItem(AppIcons.Swap, "Перенести на другую дату") { onDialog(DayDialog.MOVE) }
                    SheetItem(AppIcons.DoNotDisturb, "Не состоялась") { onDialog(DayDialog.MISSED) }
                    SheetItem(AppIcons.EventBusy, "Отменить смену") { onDialog(DayDialog.CANCEL) }
                }
                if (shift.status == ShiftStatus.CANCELLED) {
                    SheetItem(AppIcons.Restore, "Восстановить смену") { pick { actions.shift.restore(shift) } }
                }
                if (shift.status == ShiftStatus.PLANNED || shift.status == ShiftStatus.CONFIRMED) {
                    SheetItem(AppIcons.EventAvailable, "В системный календарь") { pick { addToSystemCalendar(context, shift, zone) } }
                }
                if (!shift.isClosed && shift.status != ShiftStatus.MOVED) {
                    SheetItem(AppIcons.Delete, "Удалить смену", destructive = true) { onDialog(DayDialog.DELETE) }
                }
            }

            Group("График")
            SheetItem(AppIcons.EventRepeat, "Новый график с этого дня", "Выбрать шаблон — он заменит график с ${Formats.date(date)}") {
                pick { actions.newScheduleFrom(date) }
            }
            SheetItem(AppIcons.Delete, "Удалить смены с этого дня", "Этот день и все следующие: график перестанет действовать", destructive = true) {
                onDialog(DayDialog.STOP)
            }
        }
    }
}

@Composable
private fun Group(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun SheetItem(icon: ImageVector, title: String, supporting: String? = null, destructive: Boolean = false, onClick: () -> Unit) {
    val color = if (destructive) MaterialTheme.colorScheme.error else Color.Unspecified
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = color)
            if (supporting != null) {
                Text(supporting, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AlarmsDialog(date: LocalDate, alarms: DayAlarmsUi, onDismiss: () -> Unit) {
    // Opening an alarm leaves the calendar: close the dialog first.
    val ui = DayAlarmsUi(
        alarms = alarms.alarms,
        suggestedMinute = alarms.suggestedMinute,
        onToggle = alarms.onToggle,
        onEdit = {
            onDismiss()
            alarms.onEdit(it)
        },
        onAdd = {
            onDismiss()
            alarms.onAdd(it)
        },
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Будильники · ${Formats.shortDate(date)}") },
        text = {
            Column {
                if (alarms.alarms.isEmpty()) {
                    Text("На этот день будильников нет.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.padding(top = 8.dp))
                }
                DayAlarms(date, ui)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Готово") } },
    )
}

@Composable
private fun NoteDialog(date: LocalDate, current: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var text by rememberSaveable { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Заметка · ${Formats.shortDate(date)}") },
        text = { TextInput("Текст заметки", text, { text = it }, singleLine = false, minLines = 3) },
        confirmButton = {
            TextButton(onClick = {
                onSave(text)
                onDismiss()
            }) { Text("Сохранить") }
        },
        dismissButton = {
            if (current.isNotEmpty()) {
                TextButton(onClick = {
                    onSave("")
                    onDismiss()
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            } else {
                TextButton(onClick = onDismiss) { Text("Отмена") }
            }
        },
    )
}

@Composable
private fun StopDialog(date: LocalDate, actions: DayEditActions, onDismiss: () -> Unit) {
    var includeManual by remember { mutableStateOf(false) }
    val cut by produceState<ScheduleCut?>(null, includeManual) { value = actions.previewStop(date, includeManual) }
    val manual by produceState(0) { value = actions.manualShiftsFrom(date) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить смены с ${Formats.date(date)}?") },
        text = {
            Column {
                Text(
                    "Этот день и все следующие смены по графику будут удалены " +
                        "(${cut?.plannedCount?.let { Formats.shifts(it) } ?: "…"}), график перестанет действовать с этой даты. " +
                        "Смены с отмеченными часами, отпуска и больничные останутся. Новый график можно применить с этого же дня: " +
                        "«Изменить» → «Новый график с этого дня».",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (manual > 0) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .toggleable(value = includeManual, role = Role.Checkbox, onValueChange = { includeManual = it }),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = includeManual, onCheckedChange = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Удалить и доп. и ручные смены ($manual)", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                actions.stopFrom(date, includeManual)
                onDismiss()
            }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

private fun addToSystemCalendar(context: Context, shift: Shift, zone: ZoneId) {
    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, shift.plannedStart.atZone(zone).toInstant().toEpochMilli())
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, shift.plannedEnd.atZone(zone).toInstant().toEpochMilli())
        putExtra(CalendarContract.Events.TITLE, if (shift.kind == ShiftKind.EXTRA) "Доп. смена" else "Смена")
        putExtra(CalendarContract.Events.DESCRIPTION, shift.note)
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Не найдено приложение календаря", Toast.LENGTH_SHORT).show()
    }
}
