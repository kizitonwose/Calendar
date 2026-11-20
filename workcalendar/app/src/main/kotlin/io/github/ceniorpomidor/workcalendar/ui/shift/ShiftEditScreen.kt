package io.github.ceniorpomidor.workcalendar.ui.shift

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftKind
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftSpec
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.schedule.AssignmentTimeline
import io.github.ceniorpomidor.workcalendar.domain.time.TimeMath
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.Banner
import io.github.ceniorpomidor.workcalendar.ui.components.BannerKind
import io.github.ceniorpomidor.workcalendar.ui.components.ConfirmDialog
import io.github.ceniorpomidor.workcalendar.ui.components.DateField
import io.github.ceniorpomidor.workcalendar.ui.components.LocalSnackbar
import io.github.ceniorpomidor.workcalendar.ui.components.NumberField
import io.github.ceniorpomidor.workcalendar.ui.components.ScrollColumn
import io.github.ceniorpomidor.workcalendar.ui.components.SectionCard
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.SwitchRow
import io.github.ceniorpomidor.workcalendar.ui.components.TextInput
import io.github.ceniorpomidor.workcalendar.ui.components.TimeField
import io.github.ceniorpomidor.workcalendar.ui.components.ValueRow
import io.github.ceniorpomidor.workcalendar.ui.components.launchSafely
import io.github.ceniorpomidor.workcalendar.ui.components.money
import io.github.ceniorpomidor.workcalendar.ui.components.toDecimalOrNull
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import java.time.LocalDate
import kotlin.math.roundToInt

/** Create a manual shift or edit any shift (times, break, kind, individual rate, note). */
@Composable
fun ShiftEditScreen(container: AppContainer, shiftId: Long?, date: LocalDate, extra: Boolean, onDone: () -> Unit) {
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val calc by container.calc.context.collectAsStateWithLifecycle(initialValue = null)
    var existing by remember { mutableStateOf<Shift?>(null) }
    var loaded by rememberSaveable { mutableStateOf(shiftId == null) }

    var shiftDate by rememberSaveable { mutableStateOf(date) }
    var startMinute by rememberSaveable { mutableStateOf(9 * 60) }
    var durationMinutes by rememberSaveable { mutableStateOf(9 * 60) }
    var breakText by rememberSaveable { mutableStateOf("60") }
    var isExtra by rememberSaveable { mutableStateOf(extra) }
    var rateText by rememberSaveable { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var byDuration by rememberSaveable { mutableStateOf(false) }
    var durationText by rememberSaveable { mutableStateOf("") }
    var unlocked by rememberSaveable { mutableStateOf(false) }
    var askUnlock by remember { mutableStateOf(false) }
    var askDelete by remember { mutableStateOf(false) }

    LaunchedEffect(shiftId) {
        if (shiftId != null) {
            val shift = container.shifts.get(shiftId)
            existing = shift
            if (shift != null && !loaded) {
                shiftDate = shift.date
                startMinute = TimeMath.minuteOfDay(shift.plannedStart.toLocalTime())
                durationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(shift.plannedStart, shift.plannedEnd).toInt()
                breakText = shift.plannedBreakMinutes.toString()
                isExtra = shift.kind == ShiftKind.EXTRA
                rateText = shift.hourlyRateOverride?.toString()?.replace('.', ',') ?: ""
                title = shift.title
                note = shift.note
                loaded = true
            }
        } else {
            // Suggest the schedule's working time for this weekday.
            val assignments = container.schedule.getAssignments()
            val pattern = (AssignmentTimeline.find(assignments, date) ?: assignments.lastOrNull())?.pattern
            val spec: ShiftSpec? = pattern?.specFor(date) ?: when (pattern) {
                is io.github.ceniorpomidor.workcalendar.domain.model.SchedulePattern.Weekly -> pattern.days.values.firstOrNull()
                is io.github.ceniorpomidor.workcalendar.domain.model.SchedulePattern.Cycle -> pattern.days.firstOrNull { it != null }
                null -> null
            }
            if (spec != null) {
                startMinute = spec.startMinute
                durationMinutes = spec.durationMinutes
                breakText = spec.breakMinutes.toString()
            }
        }
    }

    val shift = existing
    val locked = shift != null && shift.isClosed && !unlocked
    val endMinute = (startMinute + durationMinutes) % TimeMath.MINUTES_PER_DAY
    val breakMinutes = breakText.toIntOrNull() ?: 0
    val paidMinutes = (durationMinutes - breakMinutes).coerceAtLeast(0)
    val rate = rateText.takeIf { it.isNotBlank() }?.let { Money.parse(it) }
    val rateError = rateText.isNotBlank() && rate == null
    val breakError = breakMinutes >= durationMinutes
    val defaultRate = calc?.rates?.on(shiftDate)?.hourlyRate

    fun build(): Shift {
        val start = TimeMath.atMinute(shiftDate, startMinute)
        val base = shift ?: Shift(date = shiftDate, plannedStart = start, plannedEnd = start)
        return base.copy(
            date = shiftDate,
            plannedStart = start,
            plannedEnd = start.plusMinutes(durationMinutes.toLong()),
            plannedBreakMinutes = breakMinutes,
            kind = if (isExtra) ShiftKind.EXTRA else ShiftKind.REGULAR,
            hourlyRateOverride = rate,
            title = title.trim(),
            note = note.trim(),
        )
    }

    SubScreen(
        title = if (shift == null) (if (isExtra) "Дополнительная смена" else "Новая смена") else "Смена ${Formats.shortDate(shift.date)}",
        onBack = onDone,
        actions = {
            if (shift != null && !shift.isClosed) {
                IconButton(onClick = { askDelete = true }) { Icon(AppIcons.Delete, contentDescription = "Удалить") }
            }
        },
    ) { padding ->
        ScrollColumn(padding) {
            if (shift != null && shift.isClosed) {
                val text = if (locked) {
                    "Часы по этой смене подтверждены. Данные защищены от случайных изменений."
                } else {
                    "Редактирование подтверждённой смены. Фактические часы меняются на экране подтверждения."
                }
                Banner(
                    text,
                    if (locked) BannerKind.INFO else BannerKind.WARNING,
                    action = if (locked) ({ TextButton(onClick = { askUnlock = true }) { Text("Изменить") } }) else null,
                )
            }
            if (shift?.status == ShiftStatus.COVERED) {
                Banner("Смена приходится на период отсутствия и не учитывается в часах и заработке.", BannerKind.INFO)
            }
            SectionCard(title = "Время", icon = AppIcons.Schedule) {
                DateField("Дата", shiftDate, { shiftDate = it }, enabled = !locked && shift?.status != ShiftStatus.MOVED)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeField("Начало", startMinute, { startMinute = it }, modifier = Modifier.weight(1f), enabled = !locked)
                    if (byDuration) {
                        NumberField(
                            label = "Длительность",
                            value = durationText.ifEmpty { Formats.hoursDecimal(durationMinutes.toLong()) },
                            onChange = { text ->
                                durationText = text
                                text.toDecimalOrNull()?.let { h -> if (h > 0 && h <= 48) durationMinutes = (h * 60).roundToInt() }
                            },
                            suffix = "ч",
                            modifier = Modifier.weight(1f),
                            enabled = !locked,
                        )
                    } else {
                        TimeField(
                            "Окончание",
                            endMinute,
                            { end ->
                                var d = end - startMinute
                                if (d <= 0) d += TimeMath.MINUTES_PER_DAY
                                durationMinutes = d
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !locked,
                            supportingText = if (startMinute + durationMinutes > TimeMath.MINUTES_PER_DAY) "следующего дня" else null,
                        )
                    }
                }
                SwitchRow("Задать продолжительность", "Вместо времени окончания (например, сутки — 24 ч)", byDuration, enabled = !locked) {
                    byDuration = it
                    durationText = ""
                }
                NumberField(
                    label = "Неоплачиваемый перерыв",
                    value = breakText,
                    onChange = { breakText = it.filter { c -> c.isDigit() } },
                    suffix = "мин",
                    decimal = false,
                    isError = breakError,
                    supportingText = if (breakError) "Перерыв должен быть короче смены" else null,
                    enabled = !locked,
                )
                ValueRow("Оплачиваемое время", Formats.hoursMinutes(paidMinutes.toLong()), emphasize = true)
            }
            SectionCard(title = "Тип и оплата", icon = AppIcons.Payments) {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(selected = !isExtra, onClick = { isExtra = false }, shape = SegmentedButtonDefaults.itemShape(0, 2), enabled = !locked) {
                        Text("По графику")
                    }
                    SegmentedButton(selected = isExtra, onClick = { isExtra = true }, shape = SegmentedButtonDefaults.itemShape(1, 2), enabled = !locked) {
                        Text("Дополнительная")
                    }
                }
                NumberField(
                    label = "Своя ставка для смены",
                    value = rateText,
                    onChange = { rateText = it },
                    suffix = "₽/ч",
                    isError = rateError,
                    supportingText = when {
                        rateError -> "Неверная сумма"
                        defaultRate != null -> "Необязательно. По умолчанию: ${money(defaultRate)}/ч"
                        else -> "Ставка по умолчанию не задана"
                    },
                    enabled = !locked,
                )
                val estimate = calc?.pay?.estimate(build())
                ValueRow("Заработок по плану", estimate?.let { "≈ " + money(it.total) } ?: "нет ставки")
            }
            SectionCard(title = "Описание", icon = AppIcons.EditNote) {
                TextInput("Название (например, «Ночная»)", title, { title = it })
                TextInput("Заметка", note, { note = it }, singleLine = false, minLines = 2)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onDone, modifier = Modifier.weight(1f)) { Text("Отмена") }
                Button(
                    onClick = {
                        scope.launchSafely(snackbar, success = "Смена сохранена", onSuccess = onDone) {
                            if (shift == null) container.shifts.create(build()) else container.shifts.updatePlan(build())
                        }
                    },
                    enabled = !rateError && !breakError && (shift == null || loaded),
                    modifier = Modifier.weight(1f),
                ) { Text("Сохранить") }
            }
            if (shift?.origin == io.github.ceniorpomidor.workcalendar.domain.model.ShiftOrigin.TEMPLATE) {
                Text(
                    "Смена создана по графику. После изменения она сохранит свои настройки даже при смене графика.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (askUnlock) {
        ConfirmDialog(
            title = "Изменить подтверждённую смену?",
            text = "Изменения будут записаны в журнал. Начисление по смене будет пересчитано, если изменится ставка или тип смены.",
            confirmText = "Разблокировать",
            onConfirm = { unlocked = true },
            onDismiss = { askUnlock = false },
        )
    }
    if (askDelete && shift != null) {
        ConfirmDialog(
            title = "Удалить смену?",
            text = "Смена ${Formats.shortDate(shift.date)} будет удалена. Остальные даты и график не изменятся.",
            confirmText = "Удалить",
            destructive = true,
            onConfirm = { scope.launchSafely(snackbar, success = "Смена удалена", onSuccess = onDone) { container.shifts.delete(shift.id) } },
            onDismiss = { askDelete = false },
        )
    }
}
