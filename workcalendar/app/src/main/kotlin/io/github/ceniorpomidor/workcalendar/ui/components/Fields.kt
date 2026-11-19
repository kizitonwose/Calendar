package io.github.ceniorpomidor.workcalendar.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.ceniorpomidor.workcalendar.domain.time.TimeMath
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

fun LocalDate.toPickerMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

fun Long.toPickerDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

/** Read-only text field that opens a picker when tapped. */
@Composable
private fun PickerField(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
    isError: Boolean = false,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    LaunchedEffect(pressed) { if (pressed && enabled) onClick() }
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = enabled,
        label = { Text(label) },
        trailingIcon = { Icon(icon, contentDescription = null) },
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        interactionSource = interaction,
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun DateField(
    label: String,
    date: LocalDate?,
    onChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
    placeholder: String = "Не выбрано",
) {
    var open by rememberSaveable { mutableStateOf(false) }
    PickerField(label, date?.let { Formats.date(it) } ?: placeholder, AppIcons.Event, modifier, enabled, supportingText) { open = true }
    if (open) {
        DatePickerDialogCompat(date ?: LocalDate.now(), onDismiss = { open = false }) {
            onChange(it)
            open = false
        }
    }
}

@Composable
fun DatePickerDialogCompat(initial: LocalDate, onDismiss: () -> Unit, onPicked: (LocalDate) -> Unit) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initial.toPickerMillis())
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { state.selectedDateMillis?.let { onPicked(it.toPickerDate()) } ?: onDismiss() }) { Text("Готово") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    ) {
        DatePicker(state = state)
    }
}

@Composable
fun DateRangePickerDialog(start: LocalDate?, end: LocalDate?, title: String, onDismiss: () -> Unit, onPicked: (LocalDate, LocalDate) -> Unit) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = start?.toPickerMillis(),
        initialSelectedEndDateMillis = end?.toPickerMillis(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val s = state.selectedStartDateMillis
                    if (s != null) {
                        val e = state.selectedEndDateMillis ?: s
                        onPicked(s.toPickerDate(), e.toPickerDate())
                    } else {
                        onDismiss()
                    }
                },
            ) { Text("Готово") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    ) {
        DateRangePicker(state = state, title = { Text(title, modifier = Modifier.fillMaxWidth()) }, showModeToggle = true, modifier = Modifier.fillMaxWidth())
    }
}

/** Time of day as minutes since midnight. */
@Composable
fun TimeField(
    label: String,
    minuteOfDay: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    PickerField(label, Formats.time(TimeMath.timeOfMinute(minuteOfDay)), AppIcons.Schedule, modifier, enabled, supportingText) { open = true }
    if (open) {
        TimePickerDialogCompat(minuteOfDay, onDismiss = { open = false }) {
            onChange(it)
            open = false
        }
    }
}

@Composable
fun TimePickerDialogCompat(minuteOfDay: Int, onDismiss: () -> Unit, onPicked: (Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = minuteOfDay / 60 % 24, initialMinute = minuteOfDay % 60, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onPicked(state.hour * 60 + state.minute) }) { Text("Готово") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        text = { TimePicker(state = state) },
    )
}

/** Decimal text input (money, hours, percents). */
@Composable
fun NumberField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    decimal: Boolean = true,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { text -> onChange(text.filter { it.isDigit() || it == ',' || it == '.' || it == ' ' || (it == '-' && !decimal) }) },
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
    )
}

/** Integer input with − and + buttons. */
@Composable
fun StepperField(label: String, value: Int, onChange: (Int) -> Unit, range: IntRange, modifier: Modifier = Modifier, suffix: String? = null, step: Int = 1) {
    val signed = range.first < 0
    var focused by remember { mutableStateOf(false) }
    var draft by rememberSaveable { mutableStateOf(value.toString()) }
    fun stepBy(delta: Int) {
        val next = ((draft.toIntOrNull()?.takeIf { focused } ?: value) + delta).coerceIn(range)
        draft = next.toString()
        onChange(next)
    }
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = if (focused) draft else value.toString(),
            onValueChange = { input ->
                val text = input.replace('−', '-')
                val digits = if (signed) text.removePrefix("-") else text
                if (digits.all { it.isDigit() }) {
                    draft = text
                    text.toIntOrNull()?.let { number ->
                        val bounded = number.coerceIn(range)
                        if (bounded != number) draft = bounded.toString()
                        onChange(bounded)
                    }
                }
            },
            label = { Text(label) },
            suffix = suffix?.let { { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = if (signed) KeyboardType.Ascii else KeyboardType.Number),
            modifier = Modifier.weight(1f).onFocusChanged {
                if (it.isFocused && !focused) draft = value.toString()
                focused = it.isFocused
            },
        )
        IconButton(onClick = { stepBy(-step) }) { Icon(AppIcons.Remove, contentDescription = "Меньше") }
        IconButton(onClick = { stepBy(step) }) { Icon(AppIcons.Add, contentDescription = "Больше") }
    }
}

/** Text field for free text (titles, notes). */
@Composable
fun TextInput(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    supportingText: String? = null,
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        modifier = modifier.fillMaxWidth(),
    )
}

/** Parses decimal text into a number; accepts comma or dot. */
fun String.toDecimalOrNull(): Double? = filter { !it.isWhitespace() && it != Char(0xA0) }.replace(',', '.').toDoubleOrNull()
