package io.github.ceniorpomidor.workcalendar.ui.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.BuildConfig
import io.github.ceniorpomidor.workcalendar.domain.absence.InsuranceExperience
import io.github.ceniorpomidor.workcalendar.domain.alarm.AlarmTexts
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.components.ConfirmDialog
import io.github.ceniorpomidor.workcalendar.ui.components.LocalSnackbar
import io.github.ceniorpomidor.workcalendar.ui.components.ScrollColumn
import io.github.ceniorpomidor.workcalendar.ui.components.SectionCard
import io.github.ceniorpomidor.workcalendar.ui.components.SettingRow
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.launchSafely
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons

@Composable
fun SettingsScreen(container: AppContainer, settings: AppSettings, onNavigate: (String) -> Unit) {
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    var askReset by remember { mutableStateOf(false) }
    val experience = InsuranceExperience.months(settings.employmentStartDate, settings.priorExperienceMonths, container.clock.today())
    SubScreen(title = "Настройки", onBack = null) { padding ->
        ScrollColumn(padding) {
            SectionCard(title = "Работа") {
                SettingRow(
                    "Профиль и стаж",
                    settings.employmentStartDate?.let { "Трудоустройство ${Formats.date(it)} · стаж ${InsuranceExperience.describe(experience)}" } ?: "Укажите дату трудоустройства",
                    AppIcons.Person,
                    onClick = { onNavigate("settings/profile") },
                )
                SettingRow("График работы", "Шаблоны смен и их применение к календарю", AppIcons.EventRepeat, onClick = { onNavigate("settings/templates") })
                SettingRow("Ставки и надбавки", "Почасовая ставка с датами действия, праздничные, сверхурочные", AppIcons.Ruble, onClick = { onNavigate("settings/rates") })
                SettingRow("Аванс и зарплата", "Дни выплат, суммы и напоминания", AppIcons.Wallet, onClick = { onNavigate("settings/payouts") })
                SettingRow("Отпуск и больничный", "Правила расчёта и заработок прошлых лет", AppIcons.Vacation, onClick = { onNavigate("settings/absence") })
                SettingRow("Праздники и переносы", "Производственный календарь", AppIcons.Celebration, onClick = { onNavigate("settings/holidays") })
            }
            SectionCard(title = "Приложение") {
                SettingRow("Будильники", AlarmTexts.summary(settings.alarm), AppIcons.Alarm, onClick = { onNavigate("alarm") })
                SettingRow("Уведомления", "Окончание смены, выплаты, напоминания", AppIcons.NotificationsActive, onClick = { onNavigate("settings/notifications") })
                SettingRow("Оформление", "Тема, цветовая палитра, нижнее меню, вид календаря", AppIcons.Palette, onClick = { onNavigate("settings/display") })
                SettingRow(
                    "Защита финансов",
                    if (settings.security.financeLock) "Включена (PIN${if (settings.security.biometric) " и биометрия" else ""})" else "PIN-код или отпечаток для раздела «Финансы»",
                    AppIcons.Lock,
                    onClick = { onNavigate("settings/security") },
                )
            }
            SectionCard(title = "Данные") {
                SettingRow("Резервная копия и экспорт", "Копия в файл, восстановление, CSV, календарь .ics", AppIcons.Backup, onClick = { onNavigate("settings/backup") })
                SettingRow("Журнал изменений", "История подтверждений, ставок, графиков и выплат", AppIcons.History, onClick = { onNavigate("settings/log") })
                SettingRow(
                    "Обновление приложения",
                    "Версия ${BuildConfig.VERSION_NAME} · проверить новую",
                    AppIcons.Download,
                    onClick = { onNavigate("settings/update") },
                )
                SettingRow("Сбросить настройки", "Данные календаря и финансов сохранятся", AppIcons.Restore, onClick = { askReset = true })
            }
            Text(
                "Рабочий календарь ${BuildConfig.VERSION_NAME}. Все данные хранятся только на этом устройстве.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    if (askReset) {
        ConfirmDialog(
            title = "Сбросить настройки?",
            text = "Оформление, уведомления, будильник и правила расчёта вернутся к значениям по умолчанию. Смены, ставки, выплаты и графики не изменятся.",
            confirmText = "Сбросить",
            destructive = true,
            onConfirm = { scope.launchSafely(snackbar, success = "Настройки сброшены") { container.backup.resetSettings() } },
            onDismiss = { askReset = false },
        )
    }
}
