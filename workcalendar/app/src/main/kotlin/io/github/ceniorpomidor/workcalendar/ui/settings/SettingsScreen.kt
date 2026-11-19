package io.github.ceniorpomidor.workcalendar.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.BuildConfig
import io.github.ceniorpomidor.workcalendar.domain.absence.InsuranceExperience
import io.github.ceniorpomidor.workcalendar.domain.alarm.AlarmTexts
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.ui.components.ConfirmDialog
import io.github.ceniorpomidor.workcalendar.ui.components.LocalSnackbar
import io.github.ceniorpomidor.workcalendar.ui.components.ScrollColumn
import io.github.ceniorpomidor.workcalendar.ui.components.SectionCard
import io.github.ceniorpomidor.workcalendar.ui.components.SettingRow
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.launchSafely
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons

private data class SettingLink(val title: String, val route: String, val icon: ImageVector, val keywords: String, val detail: String? = null)

@Composable
fun SettingsScreen(container: AppContainer, settings: AppSettings, onNavigate: (String) -> Unit) {
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    var askReset by rememberSaveable { mutableStateOf(false) }
    var search by rememberSaveable { mutableStateOf("") }
    val experience = InsuranceExperience.months(settings.employmentStartDate, settings.priorExperienceMonths, container.clock.today())
    val groups = listOf(
        "Календарь и напоминания" to listOf(
            SettingLink("Оформление", "settings/display", AppIcons.Palette, "тема цвет палитра вид меню компактный", "Цвета, вид месяца и нижнее меню"),
            SettingLink("Уведомления", "settings/notifications", AppIcons.NotificationsActive, "напоминание часы окончание смены выплаты аванс зарплата отложить"),
            SettingLink("Будильники", "alarm", AppIcons.Alarm, "звонок время", AlarmTexts.summary(settings.alarm)),
        ),
        "Работа и график" to listOf(
            SettingLink("График работы", "settings/templates", AppIcons.EventRepeat, "шаблоны смен расписание"),
            SettingLink("Профиль и стаж", "settings/profile", AppIcons.Person, "трудоустройство валюта", settings.employmentStartDate?.let { "Стаж ${InsuranceExperience.describe(experience)}" }),
            SettingLink("Отпуск и больничный", "settings/absence", AppIcons.Vacation, "пособие отпускные средний заработок"),
            SettingLink("Праздники и переносы", "settings/holidays", AppIcons.Celebration, "производственный календарь выходные"),
        ),
        "Деньги" to listOf(
            SettingLink("Аванс и зарплата", "settings/payouts", AppIcons.Wallet, "правила выплат даты"),
            SettingLink("Ставки и надбавки", "settings/rates", AppIcons.Ruble, "почасовая ставка сверхурочные"),
            SettingLink("Защита финансов", "settings/security", AppIcons.Lock, "pin пин код отпечаток биометрия", if (settings.security.financeLock) "Включена" else null),
        ),
        "Приложение и данные" to listOf(
            SettingLink("Резервная копия и экспорт", "settings/backup", AppIcons.Backup, "восстановление CSV ics файл"),
            SettingLink("Журнал изменений", "settings/log", AppIcons.History, "история"),
            SettingLink("Обновление приложения", "settings/update", AppIcons.Download, "скачать версия", BuildConfig.VERSION_NAME),
            SettingLink("Сбросить настройки", "reset", AppIcons.Restore, "вернуть по умолчанию"),
        ),
    )
    val words = search.trim().split(' ').filter { it.isNotEmpty() }
    val matches = groups.map { (group, links) ->
        group to links.filter { link ->
            val terms = "$group ${link.title} ${link.keywords} ${link.detail.orEmpty()}"
            words.all { terms.contains(it, ignoreCase = true) }
        }
    }.filter { it.second.isNotEmpty() }

    SubScreen(title = "Настройки", onBack = null) { padding ->
        ScrollColumn(padding) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text("Найти настройку") },
                leadingIcon = { Icon(AppIcons.Search, contentDescription = null) },
                trailingIcon = if (search.isEmpty()) null else ({
                    IconButton(onClick = { search = "" }) { Icon(AppIcons.Close, contentDescription = "Очистить поиск") }
                }),
            )
            for ((group, links) in matches) {
                SectionCard(title = group) {
                    for (link in links) {
                        SettingRow(
                            link.title, link.detail, link.icon,
                            onClick = { if (link.route == "reset") askReset = true else onNavigate(link.route) },
                            trailing = { Icon(AppIcons.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline) },
                        )
                    }
                }
            }
            if (matches.isEmpty()) Text("Настройки по запросу «${search.trim()}» не найдены", style = MaterialTheme.typography.bodyMedium)
        }
    }
    if (askReset) {
        ConfirmDialog(
            title = "Сбросить настройки?",
            text = "Оформление, уведомления, будильники и правила расчёта вернутся к значениям по умолчанию. Смены и выплаты сохранятся.",
            confirmText = "Сбросить",
            destructive = true,
            onConfirm = { scope.launchSafely(snackbar, success = "Настройки сброшены") { container.backup.resetSettings() } },
            onDismiss = { askReset = false },
        )
    }
}
