package io.github.ceniorpomidor.workcalendar.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.domain.alarm.AlarmTexts
import io.github.ceniorpomidor.workcalendar.domain.model.AlarmItem
import io.github.ceniorpomidor.workcalendar.domain.model.AlarmRepeat
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.ConfirmDialog
import io.github.ceniorpomidor.workcalendar.ui.components.DateField
import io.github.ceniorpomidor.workcalendar.ui.components.LocalSnackbar
import io.github.ceniorpomidor.workcalendar.ui.components.ScrollColumn
import io.github.ceniorpomidor.workcalendar.ui.components.SectionCard
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.SwitchRow
import io.github.ceniorpomidor.workcalendar.ui.components.TextInput
import io.github.ceniorpomidor.workcalendar.ui.components.TimeField
import io.github.ceniorpomidor.workcalendar.ui.components.launchSafely
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * New or existing alarm. A new alarm for [date] (from the day panel) rings once that day at
 * [minute]; a new alarm from the list rings on working days.
 */
@Composable
fun AlarmEditScreen(container: AppContainer, settings: AppSettings, alarmId: Long?, date: LocalDate?, minute: Int?, onDone: () -> Unit) {
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val today = container.clock.today()
    val initial = remember {
        alarmId?.let { settings.alarm.item(it) } ?: if (date != null) {
            AlarmItem(repeat = AlarmRepeat.ONCE, date = date, minute = minute ?: AlarmItem.DEFAULT_MINUTE)
        } else {
            AlarmItem(repeat = AlarmRepeat.WORK_DAYS, minute = minute ?: AlarmItem.DEFAULT_MINUTE)
        }
    }
    var repeat by rememberSaveable { mutableStateOf(initial.repeat) }
    var beforeShift by rememberSaveable { mutableStateOf(initial.minutesBefore != null) }
    var minutesBefore by rememberSaveable { mutableStateOf(initial.minutesBefore ?: AlarmItem.DEFAULT_MINUTES_BEFORE) }
    var time by rememberSaveable { mutableStateOf(initial.minute) }
    var weekdays by rememberSaveable { mutableStateOf(initial.weekdays.ifEmpty { DayOfWeek.entries.filter { it.value <= 5 }.toSet() }.toList()) }
    var onceDate by rememberSaveable { mutableStateOf(initial.date ?: date ?: today.plusDays(1)) }
    var includeExtra by rememberSaveable { mutableStateOf(initial.includeExtraShifts) }
    var label by rememberSaveable { mutableStateOf(initial.label) }
    var skipDates by rememberSaveable { mutableStateOf(initial.skipDates.filter { !it.isBefore(today) }.sorted()) }
    var askDelete by remember { mutableStateOf(false) }
    val existing = initial.id != 0L

    fun current(): AlarmItem = initial.copy(
        enabled = if (existing) initial.enabled else true,
        label = label.trim(),
        repeat = repeat,
        minutesBefore = if (repeat == AlarmRepeat.WORK_DAYS && beforeShift) minutesBefore else null,
        minute = time,
        weekdays = if (repeat == AlarmRepeat.WEEKDAYS) weekdays.toSet() else emptySet(),
        date = if (repeat == AlarmRepeat.ONCE) onceDate else null,
        includeExtraShifts = includeExtra,
        skipDates = skipDates.toSet(),
    )
    val valid = repeat != AlarmRepeat.WEEKDAYS || weekdays.isNotEmpty()

    SubScreen(
        title = if (existing) "Будильник" else "Новый будильник",
        onBack = onDone,
        actions = {
            if (existing) IconButton(onClick = { askDelete = true }) { Icon(AppIcons.Delete, contentDescription = "Удалить") }
        },
    ) { padding ->
        ScrollColumn(padding) {
            SectionCard(title = "Когда звонит", icon = AppIcons.Alarm) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        AlarmRepeat.WORK_DAYS to "Рабочие дни",
                        AlarmRepeat.DAYS_OFF to "Выходные",
                        AlarmRepeat.WEEKDAYS to "Дни недели",
                        AlarmRepeat.ONCE to "Один раз",
                    ).forEach { (value, text) ->
                        FilterChip(selected = repeat == value, onClick = { repeat = value }, label = { Text(text) })
                    }
                }
                when (repeat) {
                    AlarmRepeat.WORK_DAYS -> {
                        Text("Дни со сменой по графику; в отпуск, на больничном и при отменённой смене не звонит.", style = hint(), color = hintColor())
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = beforeShift, onClick = { beforeShift = true }, label = { Text("До начала смены") })
                            FilterChip(selected = !beforeShift, onClick = { beforeShift = false }, label = { Text("Точное время") })
                        }
                        if (beforeShift) {
                            TimeField(
                                "За сколько до начала смены (ч:мин)",
                                minutesBefore,
                                { minutesBefore = it.coerceIn(0, AlarmItem.MAX_MINUTES_BEFORE) },
                                supportingText = "За ${Formats.hoursMinutes(minutesBefore.toLong())}. Перед ранней сменой может зазвонить накануне вечером",
                            )
                        } else {
                            TimeField("Время", time, { time = it })
                        }
                        SwitchRow("И в дни доп. смен", "Смены, добавленные вручную", includeExtra) { includeExtra = it }
                    }
                    AlarmRepeat.DAYS_OFF -> {
                        Text("Дни без смен: выходные по графику, отпуск, больничный.", style = hint(), color = hintColor())
                        TimeField("Время", time, { time = it })
                    }
                    AlarmRepeat.WEEKDAYS -> {
                        Text("Выбранные дни недели, независимо от графика.", style = hint(), color = hintColor())
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            DayOfWeek.entries.forEach { day ->
                                FilterChip(
                                    selected = day in weekdays,
                                    onClick = { weekdays = if (day in weekdays) weekdays - day else (weekdays + day).sorted() },
                                    label = { Text(Formats.weekdayShort(day)) },
                                )
                            }
                        }
                        TimeField("Время", time, { time = it })
                    }
                    AlarmRepeat.ONCE -> {
                        DateField("Дата", onceDate, { onceDate = it })
                        TimeField("Время", time, { time = it })
                    }
                }
            }
            SectionCard(title = "Название", icon = AppIcons.EditNote) {
                TextInput("Название (необязательно)", label, { label = it })
            }
            if (skipDates.isNotEmpty() && repeat != AlarmRepeat.ONCE) {
                SectionCard(title = "Не звонит в дни", icon = AppIcons.AlarmOff) {
                    for (day in skipDates) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(Formats.dayTitle(day).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            TextButton(onClick = { skipDates = skipDates - day }) { Text("Вернуть") }
                        }
                    }
                }
            }
            Text(AlarmTexts.repeat(current()), style = hint(), color = hintColor())
            Button(
                enabled = valid,
                onClick = {
                    val item = current()
                    scope.launchSafely(snackbar, success = "Будильник сохранён", onSuccess = onDone) {
                        container.settings.update { it.copy(alarm = it.alarm.save(item).cleaned(today)) }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Сохранить") }
            Spacer(Modifier.padding(bottom = 24.dp))
        }
    }
    if (askDelete) {
        ConfirmDialog(
            title = "Удалить будильник?",
            text = "${AlarmTexts.time(initial)} · ${AlarmTexts.repeat(initial)}",
            confirmText = "Удалить",
            destructive = true,
            onConfirm = {
                scope.launchSafely(snackbar, success = "Будильник удалён", onSuccess = onDone) {
                    container.settings.update { it.copy(alarm = it.alarm.remove(initial.id)) }
                }
            },
            onDismiss = { askDelete = false },
        )
    }
}

@Composable
private fun hint() = MaterialTheme.typography.bodySmall

@Composable
private fun hintColor() = MaterialTheme.colorScheme.onSurfaceVariant
