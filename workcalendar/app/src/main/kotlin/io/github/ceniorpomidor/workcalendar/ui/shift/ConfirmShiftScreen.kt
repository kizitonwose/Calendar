package io.github.ceniorpomidor.workcalendar.ui.shift

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.domain.model.CompletionKind
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftPay
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.model.completionKind
import io.github.ceniorpomidor.workcalendar.domain.time.TimeMath
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.Banner
import io.github.ceniorpomidor.workcalendar.ui.components.BannerKind
import io.github.ceniorpomidor.workcalendar.ui.components.CenteredBox
import io.github.ceniorpomidor.workcalendar.ui.components.ConfirmDialog
import io.github.ceniorpomidor.workcalendar.ui.components.LocalSnackbar
import io.github.ceniorpomidor.workcalendar.ui.components.NumberField
import io.github.ceniorpomidor.workcalendar.ui.components.ScrollColumn
import io.github.ceniorpomidor.workcalendar.ui.components.SectionCard
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.SwitchRow
import io.github.ceniorpomidor.workcalendar.ui.components.TimeField
import io.github.ceniorpomidor.workcalendar.ui.components.ValueRow
import io.github.ceniorpomidor.workcalendar.ui.components.launchSafely
import io.github.ceniorpomidor.workcalendar.ui.components.money
import io.github.ceniorpomidor.workcalendar.ui.components.toDecimalOrNull
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import kotlin.math.roundToInt

/**
 * Entering actual worked time: one tap for a fully worked shift, hours or actual start/end
 * for a partial one, or "did not take place". Confirmed shifts are protected from accidental
 * changes and can be corrected after an explicit unlock.
 */
@Composable
fun ConfirmShiftScreen(container: AppContainer, shiftId: Long, onEdit: (Long) -> Unit, onDone: () -> Unit) {
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val shiftState = remember(shiftId) { container.shifts.observeShift(shiftId) }.collectAsStateWithLifecycle(initialValue = null)
    val calc by container.calc.context.collectAsStateWithLifecycle(initialValue = null)
    val shift = shiftState.value
    var loadedOnce by remember { mutableStateOf(false) }
    LaunchedEffect(shiftId) {
        kotlinx.coroutines.delay(1500)
        loadedOnce = true
    }
    if (shift == null) {
        SubScreen(title = "Смена", onBack = onDone) { padding ->
            CenteredBox(Modifier.padding(padding).fillMaxWidth().height(200.dp)) {
                Text(if (loadedOnce) "Смена не найдена — возможно, она удалена" else "Загрузка…")
            }
        }
        return
    }
    val zone = calc?.zone ?: java.time.ZoneId.systemDefault()
    val plannedMinutes = shift.plannedPaidMinutes(zone).toInt()
    var editing by rememberSaveable(shift.id) { mutableStateOf(!shift.isClosed) }
    var askUnlock by remember { mutableStateOf(false) }
    var askReset by remember { mutableStateOf(false) }
    var askMissed by remember { mutableStateOf(false) }
    var hoursText by rememberSaveable(shift.id) { mutableStateOf(Formats.hoursDecimal((shift.workedMinutes ?: plannedMinutes).toLong())) }
    var useTimes by rememberSaveable(shift.id) { mutableStateOf(shift.actualStart != null) }
    var startMinute by rememberSaveable(shift.id) { mutableStateOf(TimeMath.minuteOfDay((shift.actualStart ?: shift.plannedStart).toLocalTime())) }
    var endMinute by rememberSaveable(shift.id) { mutableStateOf(TimeMath.minuteOfDay((shift.actualEnd ?: shift.plannedEnd).toLocalTime())) }
    var breakText by rememberSaveable(shift.id) { mutableStateOf((shift.actualBreakMinutes ?: shift.plannedBreakMinutes).toString()) }

    // Actual interval when times are given: the start is on the shift date (or the next day if
    // it is much earlier than planned), the end is after the start.
    val actualStart = run {
        var s = TimeMath.atMinute(shift.date, startMinute)
        if (s.isBefore(shift.plannedStart.minusHours(12))) s = s.plusDays(1)
        s
    }
    val actualEnd = run {
        var e = TimeMath.atMinute(actualStart.toLocalDate(), endMinute)
        while (!e.isAfter(actualStart)) e = e.plusDays(1)
        e
    }
    val breakMinutes = breakText.toIntOrNull() ?: 0
    val minutesFromTimes = (TimeMath.elapsedMinutes(actualStart, actualEnd, zone) - breakMinutes).toInt()
    val minutesFromHours = hoursText.toDecimalOrNull()?.let { (it * 60).roundToInt() }
    val minutes = if (useTimes) minutesFromTimes else minutesFromHours
    val valid = minutes != null && minutes in 1..(48 * 60)
    val preview: ShiftPay? = if (valid) {
        calc?.pay?.calculate(
            shift.copy(
                actualStart = if (useTimes) actualStart else null,
                actualEnd = if (useTimes) actualEnd else null,
                actualBreakMinutes = if (useTimes) breakMinutes else null,
            ),
            minutes,
        )
    } else {
        null
    }

    fun save(worked: Int, withTimes: Boolean) {
        scope.launchSafely(snackbar, success = "Часы сохранены: ${Formats.hours(worked.toLong())}", onSuccess = onDone) {
            container.shifts.confirm(
                shift.id,
                worked,
                actualStart = if (withTimes) actualStart else null,
                actualEnd = if (withTimes) actualEnd else null,
                actualBreakMinutes = if (withTimes) breakMinutes else null,
            )
        }
    }

    SubScreen(title = "Отработанное время", onBack = onDone) { padding ->
        ScrollColumn(padding) {
            SectionCard {
                Text(Formats.dayTitle(shift.date, withYear = true).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium)
                Text("План: ${Formats.timeRange(shift.plannedStart, shift.plannedEnd)} · ${Formats.hoursMinutes(plannedMinutes.toLong())}", style = MaterialTheme.typography.bodyLarge)
                if (shift.plannedBreakMinutes > 0) Text("Перерыв ${shift.plannedBreakMinutes} мин не оплачивается", style = MaterialTheme.typography.bodySmall)
                if (shift.title.isNotBlank()) Text(shift.title, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { onEdit(shift.id) }) {
                    Icon(AppIcons.Edit, contentDescription = null)
                    Text("  Изменить план смены")
                }
            }

            when (shift.status) {
                ShiftStatus.CANCELLED, ShiftStatus.MOVED, ShiftStatus.DELETED -> {
                    Banner("Смена отменена или перенесена — часы по ней не вносятся.", BannerKind.INFO)
                    return@ScrollColumn
                }
                ShiftStatus.COVERED -> Banner("На эту дату оформлено отсутствие. Внесите часы, только если вы всё-таки работали.", BannerKind.WARNING)
                else -> Unit
            }

            if (shift.isClosed && !editing) {
                ConfirmedSummary(shift, plannedMinutes)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { askReset = true }, modifier = Modifier.weight(1f)) { Text("Снять отметку") }
                    Button(onClick = { askUnlock = true }, modifier = Modifier.weight(1f)) { Text("Исправить часы") }
                }
                return@ScrollColumn
            }

            if (!shift.isClosed) {
                Button(onClick = { save(plannedMinutes, withTimes = false) }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Icon(AppIcons.TaskAlt, contentDescription = null)
                    Text("  Отработано полностью — ${Formats.hours(plannedMinutes.toLong())}")
                }
            }
            SectionCard(title = if (shift.isClosed) "Исправление" else "Частично или сверх плана", icon = AppIcons.Timer) {
                SwitchRow("Указать время начала и окончания", "Иначе введите количество часов", useTimes) { useTimes = it }
                if (useTimes) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TimeField("Начало", startMinute, { startMinute = it }, modifier = Modifier.weight(1f))
                        TimeField(
                            "Окончание",
                            endMinute,
                            { endMinute = it },
                            modifier = Modifier.weight(1f),
                            supportingText = if (actualEnd.toLocalDate() != actualStart.toLocalDate()) "следующего дня" else null,
                        )
                    }
                    NumberField("Перерыв", breakText, { breakText = it.filter { c -> c.isDigit() } }, suffix = "мин", decimal = false)
                    ValueRow("Получается", if (minutesFromTimes > 0) Formats.hoursMinutes(minutesFromTimes.toLong()) else "—", emphasize = true)
                } else {
                    NumberField(
                        "Отработано часов",
                        hoursText,
                        { hoursText = it },
                        suffix = "ч",
                        isError = hoursText.isNotBlank() && !valid,
                        supportingText = "Например 7,5. Сейчас: ${minutesFromHours?.let { Formats.hoursMinutes(it.toLong()) } ?: "—"}",
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(-60, -30, 30, 60).forEach { delta ->
                            AssistChip(
                                onClick = {
                                    val current = minutesFromHours ?: plannedMinutes
                                    hoursText = Formats.hoursDecimal((current + delta).coerceIn(0, 48 * 60).toLong())
                                },
                                label = { Text((if (delta > 0) "+" else "−") + Formats.hoursDecimal(kotlin.math.abs(delta).toLong()) + " ч") },
                            )
                        }
                    }
                }
                if (preview != null) {
                    PayBreakdown(preview)
                } else if (valid && calc?.pay?.hourlyRateFor(shift) == null) {
                    Text("Ставка не задана — часы сохранятся, сумма будет рассчитана после указания ставки.", style = MaterialTheme.typography.bodySmall)
                }
                FilledTonalButton(onClick = { minutes?.let { save(it, useTimes) } }, enabled = valid, modifier = Modifier.fillMaxWidth()) {
                    Text(if (valid) "Сохранить ${Formats.hours(minutes.toLong())}" else "Сохранить")
                }
            }
            if (shift.status != ShiftStatus.MISSED) {
                OutlinedButton(onClick = { askMissed = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(AppIcons.DoNotDisturb, contentDescription = null)
                    Text("  Смена не состоялась")
                }
            }
        }
    }

    if (askUnlock) {
        ConfirmDialog(
            title = "Исправить подтверждённые часы?",
            text = "Сумма по смене будет пересчитана, изменение попадёт в журнал изменений.",
            confirmText = "Исправить",
            onConfirm = { editing = true },
            onDismiss = { askUnlock = false },
        )
    }
    if (askReset) {
        ConfirmDialog(
            title = "Снять отметку о часах?",
            text = "Смена снова станет неподтверждённой и не будет учитываться в заработке.",
            confirmText = "Снять",
            destructive = true,
            onConfirm = { scope.launchSafely(snackbar, success = "Отметка снята") { container.shifts.resetConfirmation(shift.id) } },
            onDismiss = { askReset = false },
        )
    }
    if (askMissed) {
        ConfirmDialog(
            title = "Смена не состоялась?",
            text = "Будет записано 0 часов без начислений. Это можно исправить позже.",
            confirmText = "Отметить",
            onConfirm = { scope.launchSafely(snackbar, success = "Отмечено", onSuccess = onDone) { container.shifts.markMissed(shift.id) } },
            onDismiss = { askMissed = false },
        )
    }
}

@Composable
private fun ConfirmedSummary(shift: Shift, plannedMinutes: Int) {
    SectionCard(title = if (shift.status == ShiftStatus.MISSED) "Смена не состоялась" else "Часы подтверждены", icon = AppIcons.Lock) {
        if (shift.status == ShiftStatus.CONFIRMED) {
            val worked = (shift.workedMinutes ?: 0).toLong()
            ValueRow("Отработано", Formats.hoursMinutes(worked), emphasize = true)
            val kind = when (shift.completionKind(java.time.ZoneId.systemDefault())) {
                CompletionKind.FULL -> "полностью"
                CompletionKind.PARTIAL -> "частично (план ${Formats.hours(plannedMinutes.toLong())})"
                CompletionKind.OVERTIME -> "сверх плана на ${Formats.hoursMinutes(worked - plannedMinutes)}"
                else -> "—"
            }
            ValueRow("Выполнение", kind)
            shift.actualStart?.let { s -> shift.actualEnd?.let { e -> ValueRow("Фактическое время", Formats.timeRange(s, e)) } }
            shift.pay?.let { PayBreakdown(it) } ?: Text("Ставка не была задана — сумма не рассчитана", style = MaterialTheme.typography.bodySmall)
            shift.confirmedAt?.let { Text("Подтверждено ${Formats.dateTime(it)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            Text("0 часов, без начислений", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Explains how the amount for a shift was calculated. */
@Composable
fun PayBreakdown(pay: ShiftPay) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        ValueRow("${Formats.hoursDecimal(pay.paidMinutes.toLong())} ч × ${money(pay.hourlyRate)}", money(pay.base))
        if (!pay.holidayBonus.isZero) ValueRow("Праздничные ${Formats.hoursDecimal(pay.holidayMinutes.toLong())} ч, +${pay.holidayPercent}%", "+" + money(pay.holidayBonus))
        if (!pay.overtimeBonus.isZero) ValueRow("Сверхурочные ${Formats.hoursDecimal(pay.overtimeMinutes.toLong())} ч, +${pay.overtimePercent}%", "+" + money(pay.overtimeBonus))
        if (!pay.extraShiftBonus.isZero) ValueRow("Доплата за доп. смену +${pay.extraShiftPercent}%", "+" + money(pay.extraShiftBonus))
        Row {
            Text("Начисление по смене", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(money(pay.total), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}
