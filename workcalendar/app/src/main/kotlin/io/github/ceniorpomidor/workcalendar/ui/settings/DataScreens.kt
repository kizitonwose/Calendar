package io.github.ceniorpomidor.workcalendar.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.data.db.HolidayOverrideEntity
import io.github.ceniorpomidor.workcalendar.data.db.toDomain
import io.github.ceniorpomidor.workcalendar.data.repo.CsvKind
import io.github.ceniorpomidor.workcalendar.domain.backup.BackupException
import io.github.ceniorpomidor.workcalendar.domain.backup.BackupFile
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.domain.model.ChangeCategory
import io.github.ceniorpomidor.workcalendar.domain.model.HolidayOverrideType
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.time.HolidayCalendar
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.ConfirmDialog
import io.github.ceniorpomidor.workcalendar.ui.components.DateField
import io.github.ceniorpomidor.workcalendar.ui.components.EmptyState
import io.github.ceniorpomidor.workcalendar.ui.components.LocalSnackbar
import io.github.ceniorpomidor.workcalendar.ui.components.ScrollColumn
import io.github.ceniorpomidor.workcalendar.ui.components.SectionCard
import io.github.ceniorpomidor.workcalendar.ui.components.SettingRow
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.SwitchRow
import io.github.ceniorpomidor.workcalendar.ui.components.TextInput
import io.github.ceniorpomidor.workcalendar.ui.components.ThinDivider
import io.github.ceniorpomidor.workcalendar.ui.components.launchSafely
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import kotlinx.coroutines.flow.map

@Composable
fun HolidaysScreen(container: AppContainer, settings: AppSettings, onBack: () -> Unit) {
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val overrides by remember { container.db.miscDao().observeHolidays().map { list -> list.map { it.toDomain() } } }.collectAsStateWithLifecycle(initialValue = emptyList())
    var year by remember { mutableIntStateOf(container.clock.today().year) }
    var adding by remember { mutableStateOf(false) }
    val calendar = HolidayCalendar(overrides, settings.russianHolidays)
    SubScreen(title = "Праздники и переносы", onBack = onBack) { padding ->
        ScrollColumn(padding) {
            SwitchRow(
                "Производственный календарь РФ",
                "Праздники по ст. 112 ТК РФ и переносы выходных по постановлениям Правительства на 2025–${HolidayCalendar.LAST_DECREE_YEAR} гг.",
                settings.russianHolidays,
            ) { v -> scope.launchSafely(snackbar) { container.settings.update { it.copy(russianHolidays = v) } } }
            SectionCard(
                title = "Свои дни",
                icon = AppIcons.EditCalendar,
                action = { TextButton(onClick = { adding = true }) { Text("Добавить") } },
            ) {
                Text("Добавьте переносы выходных, рабочие субботы или дополнительные праздники, если они отличаются.", style = MaterialTheme.typography.bodySmall)
                if (overrides.isEmpty()) Text("Нет", style = MaterialTheme.typography.bodyMedium)
                overrides.forEach { o ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(Formats.date(o.date), style = MaterialTheme.typography.bodyLarge)
                            Text(
                                when (o.type) {
                                    HolidayOverrideType.DAY_OFF -> "выходной"
                                    HolidayOverrideType.WORKDAY -> "рабочий день"
                                    HolidayOverrideType.HOLIDAY -> "праздник (оплата как праздничный)"
                                } + if (o.title.isNotBlank()) " · ${o.title}" else "",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        IconButton(onClick = { scope.launchSafely(snackbar) { container.db.miscDao().deleteHoliday(o.date) } }) {
                            Icon(AppIcons.Delete, contentDescription = "Удалить")
                        }
                    }
                }
            }
            SectionCard(title = "Нерабочие дни $year", icon = AppIcons.Celebration) {
                Row {
                    TextButton(onClick = { year-- }) { Text("← ${year - 1}") }
                    TextButton(onClick = { year++ }) { Text("${year + 1} →") }
                }
                val days = DateRange.year(year)
                Text(
                    when {
                        !settings.russianHolidays -> "Производственный календарь выключен: учитываются только свои дни."
                        year in 2025..HolidayCalendar.LAST_DECREE_YEAR -> "Переносы по постановлению Правительства учтены."
                        else ->
                            "Постановление о переносах на $year г. ещё не учтено: автоматически переносятся только праздники, " +
                                "выпавшие на выходные (кроме январских). Переносы можно добавить в «Свои дни»."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Рабочих дней при пятидневке: ${days.count { calendar.isBusinessDay(it) }}, выходных и праздничных: ${days.count { calendar.isNonWorkingDay(it) }}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                days.filter { calendar.isSpecialDayOff(it) || calendar.isWorkingWeekend(it) }.forEach { d ->
                    val what = if (calendar.isWorkingWeekend(d)) "рабочий день (перенос)" else calendar.holidayName(d) ?: "выходной"
                    Text("${Formats.weekdayShort(d.dayOfWeek)} ${Formats.date(d)} — $what", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
    if (adding) {
        var date by remember { mutableStateOf(container.clock.today()) }
        var type by remember { mutableStateOf(HolidayOverrideType.DAY_OFF) }
        var title by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { adding = false },
            title = { Text("Особый день") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateField("Дата", date, { date = it })
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(HolidayOverrideType.DAY_OFF to "Выходной", HolidayOverrideType.WORKDAY to "Рабочий", HolidayOverrideType.HOLIDAY to "Праздник").forEach { (t, label) ->
                            FilterChip(selected = type == t, onClick = { type = t }, label = { Text(label) })
                        }
                    }
                    TextInput("Название (необязательно)", title, { title = it })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launchSafely(snackbar, success = "Сохранено") { container.db.miscDao().upsertHoliday(HolidayOverrideEntity(date, type, title.trim())) }
                    adding = false
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { adding = false }) { Text("Отмена") } },
        )
    }
}

@Composable
fun BackupScreen(container: AppContainer, onBack: () -> Unit) {
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val today = container.clock.today()
    var pendingRestore by remember { mutableStateOf<BackupFile?>(null) }
    var csvKind by remember { mutableStateOf<CsvKind?>(null) }
    var exportYear by remember { mutableIntStateOf(today.year) }
    var askDeleteAll by remember { mutableStateOf(false) }
    var confirmText by remember { mutableStateOf("") }
    var localsVersion by remember { mutableIntStateOf(0) }
    val locals = remember(localsVersion) { container.backup.localCopies() }
    val stamp = today.toString()
    val range = DateRange.year(exportYear)

    val createBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launchSafely(snackbar, success = "Резервная копия сохранена") { container.backup.exportBackup(uri) }
    }
    val openBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launchSafely(snackbar) {
                try {
                    pendingRestore = container.backup.readBackup(uri)
                } catch (e: BackupException) {
                    snackbar.showSnackbar(e.message ?: "Не удалось прочитать файл")
                }
            }
        }
    }
    val exportCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        val kind = csvKind
        if (uri != null && kind != null) scope.launchSafely(snackbar, success = "Файл сохранён") { container.backup.exportCsv(uri, kind, range) }
    }
    val exportZip = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) scope.launchSafely(snackbar, success = "Архив сохранён") { container.backup.exportCsvZip(uri, range) }
    }
    val exportIcs = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/calendar")) { uri ->
        if (uri != null) scope.launchSafely(snackbar, success = "Календарь сохранён") { container.backup.exportIcs(uri, range) }
    }

    SubScreen(title = "Резервная копия и экспорт", onBack = onBack) { padding ->
        ScrollColumn(padding) {
            SectionCard(title = "Резервная копия", icon = AppIcons.Backup) {
                Text(
                    "Полная копия всех данных и настроек в один файл. Сохраните его, например, в Google Диск — после переустановки приложения данные можно восстановить.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = { createBackup.launch("workcalendar-backup-$stamp.json") }, modifier = Modifier.fillMaxWidth()) { Text("Создать резервную копию") }
                OutlinedButton(onClick = { openBackup.launch(arrayOf("application/json", "application/octet-stream", "text/plain", "*/*")) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Восстановить из файла")
                }
            }
            SectionCard(title = "Экспорт в таблицы", icon = AppIcons.Download) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Год:", modifier = Modifier.weight(1f))
                    TextButton(onClick = { exportYear-- }) { Text("←") }
                    Text(exportYear.toString(), style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { exportYear++ }) { Text("→") }
                }
                Text("CSV открывается в Excel и Google Таблицах (разделитель «;»).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                CsvKind.entries.forEach { kind ->
                    SettingRow(kind.title, "${kind.fileName.removeSuffix(".csv")}-$exportYear.csv", AppIcons.Description, onClick = {
                        csvKind = kind
                        exportCsv.launch("${kind.fileName.removeSuffix(".csv")}-$exportYear.csv")
                    })
                }
                SettingRow("Всё одним архивом", "workcalendar-$exportYear.zip", AppIcons.Download, onClick = { exportZip.launch("workcalendar-$exportYear.zip") })
                SettingRow("Смены в календарь (.ics)", "Импорт в Google Календарь и другие", AppIcons.EventAvailable, onClick = { exportIcs.launch("workcalendar-shifts-$exportYear.ics") })
            }
            SectionCard(title = "Автоматические копии", icon = AppIcons.History) {
                Text(
                    "Еженедельно и перед восстановлением или удалением данных приложение сохраняет копию во внутреннюю память (удаляется вместе с приложением).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (locals.isEmpty()) Text("Пока нет", style = MaterialTheme.typography.bodyMedium)
                locals.take(8).forEach { local ->
                    SettingRow(local.title, null, AppIcons.Restore, onClick = {
                        scope.launchSafely(snackbar) {
                            try {
                                pendingRestore = container.backup.readLocal(local.file)
                            } catch (e: BackupException) {
                                snackbar.showSnackbar(e.message ?: "Копия повреждена")
                            }
                        }
                    })
                }
                TextButton(onClick = {
                    scope.launchSafely(snackbar, success = "Копия сохранена") {
                        container.backup.saveLocalCopy("manual")
                        localsVersion++
                    }
                }) { Text("Сохранить копию сейчас") }
            }
            SectionCard(title = "Удаление данных", icon = AppIcons.Warning) {
                OutlinedButton(onClick = { askDeleteAll = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Удалить все данные", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    pendingRestore?.let { backup ->
        ConfirmDialog(
            title = "Восстановить данные?",
            text = "Копия от ${backup.createdAt.take(16).replace('T', ' ')} (версия ${backup.appVersion.ifBlank { "?" }}): " +
                "${Formats.shifts(backup.shifts.size)}, ${backup.absences.size} отсутствий, ${backup.payments.size} выплат. " +
                "Текущие данные будут заменены (перед этим будет сохранена автоматическая копия).",
            confirmText = "Восстановить",
            destructive = true,
            onConfirm = {
                scope.launchSafely(snackbar, success = "Данные восстановлены") {
                    container.backup.restore(backup)
                    localsVersion++
                }
            },
            onDismiss = { pendingRestore = null },
        )
    }
    if (askDeleteAll) {
        AlertDialog(
            onDismissRequest = { askDeleteAll = false },
            title = { Text("Удалить все данные?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Будут удалены смены, графики, ставки, отсутствия, выплаты и настройки. Перед удалением будет сохранена автоматическая копия. Чтобы подтвердить, введите слово УДАЛИТЬ.")
                    TextInput("Подтверждение", confirmText, { confirmText = it })
                }
            },
            confirmButton = {
                TextButton(
                    enabled = confirmText.trim().equals("УДАЛИТЬ", ignoreCase = true),
                    onClick = {
                        scope.launchSafely(snackbar, success = "Все данные удалены") { container.backup.deleteAllData() }
                        askDeleteAll = false
                        confirmText = ""
                    },
                ) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { askDeleteAll = false }) { Text("Отмена") } },
        )
    }
}

@Composable
fun ChangeLogScreen(container: AppContainer, onBack: () -> Unit) {
    val entries by remember { container.db.miscDao().observeChangeLog(500).map { list -> list.map { it.toDomain() } } }.collectAsStateWithLifecycle(initialValue = emptyList())
    var filter by remember { mutableStateOf<ChangeCategory?>(null) }
    SubScreen(title = "Журнал изменений", onBack = onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp)) {
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("Все") })
                    listOf(
                        ChangeCategory.SHIFT to "Смены",
                        ChangeCategory.SCHEDULE to "Графики",
                        ChangeCategory.RATE to "Ставки",
                        ChangeCategory.ABSENCE to "Отсутствия",
                        ChangeCategory.PAYMENT to "Выплаты",
                        ChangeCategory.ACCRUAL to "Начисления",
                    ).forEach { (c, label) -> FilterChip(selected = filter == c, onClick = { filter = c }, label = { Text(label) }) }
                }
            }
            val visible = entries.filter { filter == null || it.category == filter }
            if (visible.isEmpty()) item { EmptyState(AppIcons.History, "Записей нет") }
            items(visible, key = { it.id }) { e ->
                Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(e.action, style = MaterialTheme.typography.titleSmall)
                    Text(e.description, style = MaterialTheme.typography.bodyMedium)
                    Text(Formats.dateTime(e.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                ThinDivider()
            }
        }
    }
}
