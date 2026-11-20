package io.github.ceniorpomidor.workcalendar.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.data.repo.ApplyPreview
import io.github.ceniorpomidor.workcalendar.data.repo.ApplyRequest
import io.github.ceniorpomidor.workcalendar.domain.model.ApplyFromDefault
import io.github.ceniorpomidor.workcalendar.domain.model.ScheduleAssignment
import io.github.ceniorpomidor.workcalendar.domain.model.SchedulePattern
import io.github.ceniorpomidor.workcalendar.domain.model.ScheduleTemplate
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftSpec
import io.github.ceniorpomidor.workcalendar.domain.schedule.ChangeType
import io.github.ceniorpomidor.workcalendar.domain.schedule.ScheduleGenerator
import io.github.ceniorpomidor.workcalendar.domain.schedule.SchedulePresets
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.time.HolidayCalendar
import io.github.ceniorpomidor.workcalendar.domain.time.TimeMath
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.Banner
import io.github.ceniorpomidor.workcalendar.ui.components.BannerKind
import io.github.ceniorpomidor.workcalendar.ui.components.ConfirmDialog
import io.github.ceniorpomidor.workcalendar.ui.components.DateField
import io.github.ceniorpomidor.workcalendar.ui.components.EmptyState
import io.github.ceniorpomidor.workcalendar.ui.components.LocalSnackbar
import io.github.ceniorpomidor.workcalendar.ui.components.NumberField
import io.github.ceniorpomidor.workcalendar.ui.components.ScrollColumn
import io.github.ceniorpomidor.workcalendar.ui.components.SectionCard
import io.github.ceniorpomidor.workcalendar.ui.components.StepperField
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.SwitchRow
import io.github.ceniorpomidor.workcalendar.ui.components.TextInput
import io.github.ceniorpomidor.workcalendar.ui.components.ThinDivider
import io.github.ceniorpomidor.workcalendar.ui.components.TimeField
import io.github.ceniorpomidor.workcalendar.ui.components.ValueRow
import io.github.ceniorpomidor.workcalendar.ui.components.launchSafely
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/** Short human description of a pattern: "Пн–Пт 09:00–18:00" or "2/2, 08:00–20:00". */
fun SchedulePattern.describe(): String = when (this) {
    is SchedulePattern.Weekly -> {
        if (days.isEmpty()) {
            "Нет рабочих дней"
        } else {
            val distinct = days.values.distinctBy { it.startMinute to it.durationMinutes }
            val dayNames = DayOfWeek.entries.filter { it in days }.joinToString(", ") { Formats.weekdayShort(it) }
            if (distinct.size == 1) "$dayNames · ${specText(distinct.first())}" else "$dayNames · разное время"
        }
    }
    is SchedulePattern.Cycle -> {
        val first = days.firstOrNull { it != null }
        "Цикл $workDays/$offDays" + (first?.let { " · ${specText(it)}" } ?: "")
    }
}

fun specText(spec: ShiftSpec): String {
    val start = TimeMath.timeOfMinute(spec.startMinute)
    val end = TimeMath.timeOfMinute(spec.endMinuteOfDay)
    val next = if (spec.crossesMidnight || spec.durationMinutes >= TimeMath.MINUTES_PER_DAY) " (+1)" else ""
    return "${Formats.time(start)}–${Formats.time(end)}$next"
}

@Composable
fun TemplatesScreen(container: AppContainer, onBack: (() -> Unit)?, onEdit: (Long) -> Unit, onApply: (Long) -> Unit, from: LocalDate? = null) {
    val templates by container.schedule.templates.collectAsStateWithLifecycle(initialValue = emptyList())
    val assignments by container.schedule.assignments.collectAsStateWithLifecycle(initialValue = emptyList())
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    var removing by remember { mutableStateOf<ScheduleAssignment?>(null) }
    var deleting by remember { mutableStateOf<ScheduleTemplate?>(null) }
    SubScreen(
        title = "График работы",
        onBack = onBack,
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { onEdit(0) }, icon = { Icon(AppIcons.Add, contentDescription = null) }, text = { Text("Шаблон") })
        },
    ) { padding ->
        ScrollColumn(padding) {
            if (from != null) {
                Banner(
                    "Новый график с ${Formats.date(from)}: нажмите «Применить» у нужного шаблона или создайте новый. " +
                        "Смены до этой даты не изменятся, с неё — заменятся новым графиком.",
                    BannerKind.INFO,
                )
            }
            Text(
                "Шаблон описывает типовой график. После применения календарь заполняется сменами автоматически. Изменение шаблона не меняет календарь, пока вы снова не примените его.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SectionCard(title = "Шаблоны", icon = AppIcons.EventRepeat) {
                if (templates.isEmpty()) {
                    EmptyState(AppIcons.EventRepeat, "Нет шаблонов", "Создайте шаблон: 5/2, 2/2, сутки через трое или свой цикл")
                }
                templates.forEach { t ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(t.name, style = MaterialTheme.typography.titleSmall)
                        Text(t.pattern.describe(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row {
                            TextButton(onClick = { onApply(t.id) }) { Text("Применить") }
                            TextButton(onClick = { onEdit(t.id) }) { Text("Изменить") }
                            TextButton(onClick = { deleting = t }) { Text("Удалить") }
                        }
                    }
                    ThinDivider()
                }
            }
            val settings by container.settings.settings.collectAsStateWithLifecycle(initialValue = null)
            settings?.let { s ->
                SectionCard(title = "Правила применения", icon = AppIcons.Tune) {
                    StepperField(
                        "Создавать смены на месяцев вперёд",
                        s.schedule.horizonMonths,
                        { v -> scope.launchSafely(snackbar) { container.settings.update { it.copy(schedule = it.schedule.copy(horizonMonths = v)) } } },
                        1..24,
                    )
                    Text("При пролистывании календаря дальше смены создаются автоматически.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Изменения графика по умолчанию применять:", style = MaterialTheme.typography.bodyMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(ApplyFromDefault.TODAY to "с сегодня", ApplyFromDefault.TOMORROW to "с завтра", ApplyFromDefault.NEXT_MONTH to "со след. месяца").forEach { (v, label) ->
                            FilterChip(
                                selected = s.schedule.applyFromDefault == v,
                                onClick = { scope.launchSafely(snackbar) { container.settings.update { it.copy(schedule = it.schedule.copy(applyFromDefault = v)) } } },
                                label = { Text(label) },
                            )
                        }
                    }
                    SwitchRow("Сохранять дни, изменённые вручную", "Иначе при применении графика они будут перезаписаны (кроме подтверждённых)", s.schedule.keepUserChanges) { v ->
                        scope.launchSafely(snackbar) { container.settings.update { it.copy(schedule = it.schedule.copy(keepUserChanges = v)) } }
                    }
                }
            }
            SectionCard(title = "Применённые графики", icon = AppIcons.DateRange) {
                if (assignments.isEmpty()) {
                    Text("График ещё не применён к календарю", style = MaterialTheme.typography.bodyMedium)
                }
                assignments.sortedByDescending { it.startDate }.forEach { a ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(a.templateName, style = MaterialTheme.typography.titleSmall)
                            Text(
                                (if (a.endDate != null) Formats.period(a.startDate, a.endDate!!) else "с ${Formats.date(a.startDate)}, бессрочно") + " · " + a.pattern.describe(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { removing = a }) { Icon(AppIcons.Close, contentDescription = "Отменить график") }
                    }
                }
            }
            Spacer(Modifier.padding(bottom = 72.dp))
        }
    }
    removing?.let { a ->
        AlertDialog(
            onDismissRequest = { removing = null },
            title = { Text("Отменить график «${a.templateName}»?") },
            text = { Text("Будущие смены этого графика, которые вы не меняли, можно удалить. Прошедшие, подтверждённые и изменённые вручную смены сохранятся.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launchSafely(snackbar, success = "График отменён") { container.schedule.removeAssignment(a, removeFutureShifts = true) }
                    removing = null
                }) { Text("Удалить будущие смены") }
            },
            dismissButton = {
                TextButton(onClick = {
                    scope.launchSafely(snackbar, success = "График отменён, смены сохранены") { container.schedule.removeAssignment(a, removeFutureShifts = false) }
                    removing = null
                }) { Text("Оставить смены") }
            },
        )
    }
    deleting?.let { t ->
        ConfirmDialog(
            title = "Удалить шаблон «${t.name}»?",
            text = "Уже созданные по нему смены и применённые графики не изменятся.",
            confirmText = "Удалить",
            destructive = true,
            onConfirm = { scope.launchSafely(snackbar, success = "Шаблон удалён") { container.schedule.deleteTemplate(t) } },
            onDismiss = { deleting = null },
        )
    }
}

/** Editable state of one day of a pattern. */
private data class DayDraft(val working: Boolean, val start: Int, val duration: Int, val breakMinutes: Int, val title: String = "") {
    fun toSpec(): ShiftSpec? = if (working) ShiftSpec(start, duration, breakMinutes.coerceIn(0, duration - 1), title) else null

    companion object {
        fun from(spec: ShiftSpec?, fallback: ShiftSpec): DayDraft {
            val s = spec ?: fallback
            return DayDraft(spec != null, s.startMinute, s.durationMinutes, s.breakMinutes, s.title)
        }
    }
}

@Composable
fun TemplateEditScreen(container: AppContainer, templateId: Long?, onBack: () -> Unit, onSavedApply: (Long) -> Unit) {
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val default = ShiftSpec(9 * 60, 9 * 60, 60)
    var loaded by rememberSaveable { mutableStateOf(templateId == null) }
    var existing by remember { mutableStateOf<ScheduleTemplate?>(null) }
    var name by rememberSaveable { mutableStateOf("Мой график") }
    var weekly by rememberSaveable { mutableStateOf(true) }
    var skipHolidays by rememberSaveable { mutableStateOf(true) }
    var anchor by rememberSaveable { mutableStateOf(container.clock.today()) }
    val weekDays = remember { mutableStateListOf<DayDraft>().apply { DayOfWeek.entries.forEach { add(DayDraft(it.value <= 5, 9 * 60, 9 * 60, 60)) } } }
    val cycleDays =
        remember { mutableStateListOf(DayDraft(true, 8 * 60, 12 * 60, 60), DayDraft(true, 8 * 60, 12 * 60, 60), DayDraft(false, 8 * 60, 12 * 60, 60), DayDraft(false, 8 * 60, 12 * 60, 60)) }
    var askApply by remember { mutableStateOf<Long?>(null) }

    fun load(pattern: SchedulePattern) {
        when (pattern) {
            is SchedulePattern.Weekly -> {
                weekly = true
                skipHolidays = pattern.skipHolidays
                DayOfWeek.entries.forEachIndexed { i, d -> weekDays[i] = DayDraft.from(pattern.days[d], pattern.days.values.firstOrNull() ?: default) }
            }
            is SchedulePattern.Cycle -> {
                weekly = false
                skipHolidays = pattern.skipHolidays
                anchor = pattern.anchorDate
                cycleDays.clear()
                val fallback = pattern.days.firstOrNull { it != null } ?: default
                pattern.days.forEach { cycleDays.add(DayDraft.from(it, fallback)) }
            }
        }
    }

    LaunchedEffect(templateId) {
        if (templateId != null && !loaded) {
            container.schedule.getTemplate(templateId)?.let { t ->
                existing = t
                name = t.name
                load(t.pattern)
            }
            loaded = true
        } else if (templateId != null) {
            existing = container.schedule.getTemplate(templateId)
        }
    }

    val pattern: SchedulePattern? = runCatching {
        if (weekly) {
            SchedulePattern.Weekly(DayOfWeek.entries.mapIndexedNotNull { i, d -> weekDays[i].toSpec()?.let { d to it } }.toMap(), skipHolidays)
        } else {
            SchedulePattern.Cycle(cycleDays.map { it.toSpec() }, anchor, skipHolidays)
        }
    }.getOrNull()

    SubScreen(title = if (templateId == null) "Новый шаблон" else "Шаблон", onBack = onBack) { padding ->
        ScrollColumn(padding) {
            SectionCard(title = "Быстрый выбор") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SchedulePresets.all.forEach { preset ->
                        AssistChip(
                            onClick = {
                                load(SchedulePresets.create(preset.id, anchor))
                                if (name.isBlank() || name == "Мой график" || SchedulePresets.all.any { it.name == name }) name = preset.name
                            },
                            label = { Text(preset.name) },
                        )
                    }
                }
            }
            SectionCard {
                TextInput("Название шаблона", name, { name = it })
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(selected = weekly, onClick = { weekly = true }, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text("По дням недели") }
                    SegmentedButton(selected = !weekly, onClick = { weekly = false }, shape = SegmentedButtonDefaults.itemShape(1, 2)) { Text("Цикл (2/2, 3/3…)") }
                }
                SwitchRow("Не ставить смены в праздники", "Праздничные и перенесённые выходные дни", skipHolidays) { skipHolidays = it }
            }
            if (weekly) {
                SectionCard(title = "Рабочие дни", icon = AppIcons.Today) {
                    DayOfWeek.entries.forEachIndexed { i, day ->
                        DayEditor(Formats.weekdayFull(day).replaceFirstChar { it.uppercase() }, weekDays[i]) { weekDays[i] = it }
                        if (i < 6) ThinDivider()
                    }
                    TextButton(onClick = {
                        val first = weekDays.firstOrNull { it.working } ?: return@TextButton
                        for (i in weekDays.indices) if (weekDays[i].working) weekDays[i] = first.copy(working = true)
                    }) { Text("Одинаковое время для всех рабочих дней") }
                }
            } else {
                SectionCard(title = "Цикл", icon = AppIcons.EventRepeat) {
                    DateField("Первый день цикла", anchor, { anchor = it }, supportingText = "Дата, с которой начинается день 1 цикла")
                    cycleDays.forEachIndexed { i, d ->
                        DayEditor("День ${i + 1}", d) { cycleDays[i] = it }
                        ThinDivider()
                    }
                    Row {
                        TextButton(onClick = { cycleDays.add(DayDraft(false, cycleDays.last().start, cycleDays.last().duration, cycleDays.last().breakMinutes)) }) { Text("+ день") }
                        TextButton(onClick = { if (cycleDays.size > 1) cycleDays.removeAt(cycleDays.lastIndex) }, enabled = cycleDays.size > 1) { Text("− день") }
                    }
                }
            }
            if (pattern != null) {
                SectionCard(title = "Предпросмотр на 2 недели", icon = AppIcons.Visibility) {
                    val holidays = HolidayCalendar()
                    val from = if (weekly) container.clock.today() else maxOf(anchor, container.clock.today())
                    val shifts = ScheduleGenerator.generate(pattern, DateRange(from, from.plusDays(13)), holidays).associateBy { it.date }
                    for (offset in 0 until 14) {
                        val date = from.plusDays(offset.toLong())
                        val s = shifts[date]
                        Row(Modifier.fillMaxWidth()) {
                            Text("${Formats.weekdayShort(date.dayOfWeek)} ${Formats.shortDate(date)}", modifier = Modifier.width(96.dp), style = MaterialTheme.typography.bodyMedium)
                            Text(
                                s?.let { Formats.timeRange(it.start, it.end) } ?: "выходной",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (s ==
                                    null
                                ) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        }
                    }
                }
            } else {
                Banner("Проверьте время смен: перерыв должен быть короче смены", BannerKind.ERROR)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Отмена") }
                Button(
                    enabled = pattern != null && name.isNotBlank() && loaded,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val p = pattern ?: return@Button
                        scope.launchSafely(snackbar) {
                            val base = existing ?: ScheduleTemplate(name = name.trim(), pattern = p)
                            val id = container.schedule.saveTemplate(base.copy(name = name.trim(), pattern = p))
                            askApply = id
                        }
                    },
                ) { Text("Сохранить") }
            }
        }
    }
    askApply?.let { id ->
        AlertDialog(
            onDismissRequest = onBack,
            title = { Text("Шаблон сохранён") },
            text = { Text("Применить его к календарю? Перед применением будет показано, какие даты изменятся.") },
            confirmButton = { TextButton(onClick = { onSavedApply(id) }) { Text("Применить") } },
            dismissButton = { TextButton(onClick = onBack) { Text("Позже") } },
        )
    }
}

@Composable
private fun DayEditor(label: String, day: DayDraft, onChange: (DayDraft) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(if (day.working) "работа" else "выходной", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Switch(checked = day.working, onCheckedChange = { onChange(day.copy(working = it)) })
        }
        if (day.working) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeField("Начало", day.start, { onChange(day.copy(start = it)) }, modifier = Modifier.weight(1f))
                TimeField(
                    "Конец",
                    (day.start + day.duration) % TimeMath.MINUTES_PER_DAY,
                    { end ->
                        var d = end - day.start
                        if (d <= 0) d += TimeMath.MINUTES_PER_DAY
                        onChange(day.copy(duration = d))
                    },
                    modifier = Modifier.weight(1f),
                    supportingText = if (day.start + day.duration > TimeMath.MINUTES_PER_DAY) "след. дня" else null,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(
                    "Перерыв, мин",
                    day.breakMinutes.toString(),
                    { t -> onChange(day.copy(breakMinutes = t.filter { it.isDigit() }.toIntOrNull()?.coerceAtMost(day.duration - 1) ?: 0)) },
                    decimal = false,
                    modifier = Modifier.weight(1f),
                )
                NumberField(
                    "Длительность, ч",
                    Formats.hoursDecimal(day.duration.toLong()),
                    { t ->
                        t.replace(',', '.').toDoubleOrNull()?.let { h -> if (h > 0 && h <= 48) onChange(day.copy(duration = (h * 60).toInt())) }
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** Preview and application of a template to the calendar. */
@Composable
fun ApplyScheduleScreen(container: AppContainer, templateId: Long, onDone: () -> Unit, from: LocalDate? = null) {
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val settings by container.settings.settings.collectAsStateWithLifecycle(initialValue = null)
    var template by remember { mutableStateOf<ScheduleTemplate?>(null) }
    val today = container.clock.today()
    var start by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var limited by rememberSaveable { mutableStateOf(false) }
    var end by rememberSaveable { mutableStateOf(today.plusMonths(1)) }
    var overwrite by rememberSaveable { mutableStateOf(false) }
    var preview by remember { mutableStateOf<ApplyPreview?>(null) }
    var askConfirm by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(templateId) { template = container.schedule.getTemplate(templateId) }
    LaunchedEffect(settings) {
        val s = settings ?: return@LaunchedEffect
        if (start == null) {
            start = from ?: when (s.schedule.applyFromDefault) {
                ApplyFromDefault.TODAY -> today
                ApplyFromDefault.TOMORROW -> today.plusDays(1)
                ApplyFromDefault.NEXT_MONTH -> YearMonth.from(today).plusMonths(1).atDay(1)
            }
            overwrite = !s.schedule.keepUserChanges
        }
    }
    val t = template
    val startDate = start
    LaunchedEffect(t, startDate, limited, end, overwrite) {
        if (t == null || startDate == null) return@LaunchedEffect
        preview = null
        try {
            preview = container.schedule.preview(ApplyRequest(t.id, t.name, t.pattern, startDate, if (limited) end else null, overwrite))
        } catch (e: Exception) {
            snackbar.showSnackbar(e.message ?: "Ошибка предпросмотра")
        }
    }

    SubScreen(title = "Применить график", onBack = onDone) { padding ->
        ScrollColumn(padding) {
            if (t == null || startDate == null) {
                Text("Загрузка…")
                return@ScrollColumn
            }
            SectionCard(title = t.name, icon = AppIcons.EventRepeat) {
                Text(t.pattern.describe(), style = MaterialTheme.typography.bodyMedium)
            }
            SectionCard(title = "Период применения", icon = AppIcons.DateRange) {
                DateField("Применять с", startDate, { start = it }, supportingText = "Даты до этой не изменятся")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = startDate == today, onClick = { start = today }, label = { Text("Сегодня") })
                    FilterChip(selected = startDate == today.plusDays(1), onClick = { start = today.plusDays(1) }, label = { Text("Завтра") })
                    val nextMonth = YearMonth.from(today).plusMonths(1).atDay(1)
                    FilterChip(selected = startDate == nextMonth, onClick = { start = nextMonth }, label = { Text("С 1-го числа след. месяца") })
                }
                SwitchRow("Ограничить период", "Например, только на время подработки", limited) { limited = it }
                if (limited) DateField("По (включительно)", end, { end = if (it < startDate) startDate else it })
                SwitchRow(
                    "Перезаписать изменённые вручную дни",
                    "Подтверждённые и перенесённые смены не меняются никогда",
                    overwrite,
                ) { overwrite = it }
            }
            val p = preview
            if (p == null) {
                Text("Расчёт изменений…")
            } else {
                SectionCard(title = "Что изменится", icon = AppIcons.Visibility) {
                    ValueRow("Проверено дат", "${Formats.period(p.range.start, p.range.endInclusive)}")
                    ValueRow("Будет добавлено смен", p.addCount.toString(), emphasize = true)
                    ValueRow("Изменится время смен", p.updateCount.toString())
                    ValueRow("Будет удалено смен", p.removeCount.toString())
                    ValueRow("Сохранится ручных изменений", p.keptCount.toString())
                    if (p.timeline.updated.isNotEmpty() || p.timeline.removed.isNotEmpty()) {
                        Text(
                            "Текущие графики: " +
                                (
                                    p.timeline.updated.map {
                                        "«${it.templateName}» до ${it.endDate?.let { e -> Formats.date(e) } ?: "—"}"
                                    } + p.timeline.removed.map { "«${it.templateName}» отменяется" }
                                ).joinToString("; "),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                val interesting = p.plan.changes.filter { it.type != ChangeType.ADD }
                if (interesting.isNotEmpty()) {
                    SectionCard(title = "Затронутые даты") {
                        interesting.take(40).forEach { ch ->
                            val label = when (ch.type) {
                                ChangeType.UPDATE -> "новое время ${ch.after?.let { Formats.timeRange(it.plannedStart, it.plannedEnd) }}"
                                ChangeType.REMOVE -> "смена удалится (выходной)"
                                ChangeType.KEEP_USER_CHANGE -> "изменено вручную — сохранится"
                                ChangeType.KEEP_CLOSED -> "подтверждено — не изменится"
                                ChangeType.RESET_USER_CHANGE -> "ручное изменение будет заменено"
                                ChangeType.REMOVE_USER_CHANGE -> "ручное изменение будет удалено"
                                ChangeType.ADD -> "новая смена"
                            }
                            Text("${Formats.weekdayShort(ch.date.dayOfWeek)} ${Formats.date(ch.date)} — $label", style = MaterialTheme.typography.bodySmall)
                        }
                        if (interesting.size > 40) Text("…и ещё ${interesting.size - 40}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Button(
                    onClick = { if (p.plan.touchesExistingShifts) askConfirm = true else applySchedule(scope, container, p, snackbar, onDone) { busy = it } },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (p.plan.isEmpty) "Сохранить график (смены уже соответствуют)" else "Применить") }
            }
        }
    }
    val p = preview
    if (askConfirm && p != null) {
        ConfirmDialog(
            title = "Изменить существующие смены?",
            text = "Изменится время у ${p.updateCount} и удалится ${p.removeCount} смен, созданных ранее по графику. Подтверждённые часы и финансовые данные не затрагиваются.",
            confirmText = "Применить",
            onConfirm = { applySchedule(scope, container, p, snackbar, onDone) { busy = it } },
            onDismiss = { askConfirm = false },
        )
    }
}

private fun applySchedule(
    scope: kotlinx.coroutines.CoroutineScope,
    container: AppContainer,
    preview: ApplyPreview,
    snackbar: androidx.compose.material3.SnackbarHostState,
    onDone: () -> Unit,
    setBusy: (Boolean) -> Unit,
) {
    setBusy(true)
    scope.launchSafely(snackbar, success = "График применён: добавлено ${Formats.shifts(preview.addCount)}", onSuccess = onDone) {
        try {
            container.schedule.apply(preview)
        } finally {
            setBusy(false)
        }
    }
}
