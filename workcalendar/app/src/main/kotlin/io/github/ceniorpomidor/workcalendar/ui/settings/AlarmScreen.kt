package io.github.ceniorpomidor.workcalendar.ui.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.domain.alarm.AlarmTexts
import io.github.ceniorpomidor.workcalendar.domain.alarm.PlannedAlarm
import io.github.ceniorpomidor.workcalendar.domain.model.AlarmItem
import io.github.ceniorpomidor.workcalendar.domain.model.AlarmRepeat
import io.github.ceniorpomidor.workcalendar.domain.model.AlarmSettings
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.notifications.Notifications
import io.github.ceniorpomidor.workcalendar.ui.components.Banner
import io.github.ceniorpomidor.workcalendar.ui.components.BannerKind
import io.github.ceniorpomidor.workcalendar.ui.components.EmptyState
import io.github.ceniorpomidor.workcalendar.ui.components.LocalSnackbar
import io.github.ceniorpomidor.workcalendar.ui.components.ScrollColumn
import io.github.ceniorpomidor.workcalendar.ui.components.SectionCard
import io.github.ceniorpomidor.workcalendar.ui.components.SettingRow
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.launchSafely
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons

/** List of the app's alarms, the nearest rings and the ring settings. */
@Composable
fun AlarmScreen(container: AppContainer, settings: AppSettings, onBack: (() -> Unit)?, onEdit: (Long) -> Unit) {
    val update = rememberSettingsUpdater(container)
    val context = LocalContext.current
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    var refresh by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { refresh++ }
    val canPost = remember(refresh) { Notifications.canPost(context) }
    val fullScreen = remember(refresh) { container.wakeAlarms.canUseFullScreen() }
    val exact = remember(refresh) { container.notifications.canScheduleExact() }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { refresh++ }
    val alarm = settings.alarm
    val today = container.clock.today()
    val upcoming by produceState(emptyList<PlannedAlarm>(), alarm, refresh) { value = container.wakeAlarms.upcoming(UPCOMING_COUNT) }
    val snoozedUntil = remember(refresh, upcoming) { container.wakeAlarms.snoozedUntil() }
    fun change(success: String? = null, transform: (AlarmSettings) -> AlarmSettings) =
        scope.launchSafely(snackbar, success = success) { container.settings.update { it.copy(alarm = transform(it.alarm).cleaned(today)) } }

    SubScreen(
        title = "Будильники",
        onBack = onBack,
        actions = {
            // The clock app keeps its own alarms: open it from here.
            IconButton(onClick = { runCatching { context.startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }) {
                Icon(AppIcons.Schedule, contentDescription = "Будильники «Часов»")
            }
        },
    ) { padding ->
        ScrollColumn(padding) {
            if (!canPost) {
                Banner(
                    "Уведомления запрещены — будильники не смогут зазвонить.",
                    BannerKind.ERROR,
                    action = {
                        TextButton(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
                            }
                        }) { Text("Разрешить") }
                    },
                )
            }
            if (!fullScreen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                Banner(
                    "Нет разрешения на показ во весь экран — при заблокированном телефоне будильник появится только уведомлением.",
                    BannerKind.WARNING,
                    action = {
                        TextButton(onClick = {
                            runCatching {
                                context.startActivity(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:${context.packageName}")))
                            }
                        }) { Text("Разрешить") }
                    },
                )
            }
            if (!exact) {
                Banner(
                    "Точные будильники запрещены — звонок может опоздать на несколько минут.",
                    BannerKind.WARNING,
                    action = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            TextButton(onClick = {
                                runCatching {
                                    context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")))
                                }
                            }) { Text("Настроить") }
                        }
                    },
                )
            }
            snoozedUntil?.let { at ->
                Banner(
                    "Будильник отложен до ${Formats.time(at)}",
                    BannerKind.INFO,
                    action = {
                        TextButton(onClick = {
                            scope.launchSafely(snackbar, success = "Отложенный будильник выключен") {
                                container.wakeAlarms.cancelSnooze()
                                refresh++
                            }
                        }) { Text("Выключить") }
                    },
                )
            }
            Text(
                "Будильники календаря звонят сами и не меняют будильники приложения «Часы». Будильник на один день можно завести и в панели дня на календаре.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val items = alarm.items.filter { it.repeat != AlarmRepeat.ONCE || it.date?.isBefore(today) != true }
            if (items.isEmpty()) {
                EmptyState(AppIcons.Alarm, "Будильников нет", "Например, будильник перед каждой сменой по графику") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            change("Будильник заведён") {
                                it.save(AlarmItem(repeat = AlarmRepeat.WORK_DAYS, minutesBefore = AlarmItem.DEFAULT_MINUTES_BEFORE))
                            }
                        }) { Text("Перед каждой сменой") }
                        OutlinedButton(onClick = { onEdit(0) }) { Text("Другой будильник") }
                    }
                }
            } else {
                SectionCard(
                    title = "Будильники",
                    icon = AppIcons.Alarm,
                    action = {
                        TextButton(onClick = { onEdit(0) }) {
                            Icon(AppIcons.AlarmAdd, contentDescription = null)
                            Spacer(Modifier.padding(start = 6.dp))
                            Text("Добавить")
                        }
                    },
                ) {
                    for (item in items.sortedWith(compareBy({ it.repeat }, { it.minutesBefore == null }, { it.minutesBefore }, { it.date }, { it.minute }))) {
                        AlarmItemRow(item, onClick = { onEdit(item.id) }, onEnabled = { on -> change { it.setEnabled(item.id, on) } })
                    }
                }
            }
            if (upcoming.isNotEmpty()) {
                SectionCard(title = "Ближайшие звонки", icon = AppIcons.Event) {
                    for (ring in upcoming) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${Formats.time(ring.at)} · ${Formats.dayTitle(ring.at.toLocalDate())}", style = MaterialTheme.typography.bodyLarge)
                                Text(ring.description(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                change("Будильник на ${Formats.shortDate(ring.date)} выключен") { s ->
                                    // A one-time alarm is removed, a repeating one skips this day.
                                    if (ring.alarm.repeat == AlarmRepeat.ONCE) s.remove(ring.alarm.id) else s.skip(ring.alarm.id, ring.date, true)
                                }
                            }) { Icon(AppIcons.AlarmOff, contentDescription = "Не будить в этот день") }
                        }
                    }
                }
            }
            SectionCard(title = "Звонок", icon = AppIcons.NotificationsActive) {
                Text("Отложить на", style = MaterialTheme.typography.bodyMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 15, 20, 30).forEach { m ->
                        FilterChip(selected = alarm.snoozeMinutes == m, onClick = { update { it.copy(alarm = it.alarm.copy(snoozeMinutes = m)) } }, label = { Text("$m мин") })
                    }
                }
                Text("Звонит не дольше", style = MaterialTheme.typography.bodyMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 5, 10, 15, 30).forEach { m ->
                        FilterChip(selected = alarm.ringMinutes == m, onClick = { update { it.copy(alarm = it.alarm.copy(ringMinutes = m)) } }, label = { Text("$m мин") })
                    }
                }
                SettingRow(
                    "Мелодия и вибрация",
                    "Системные настройки звука будильника. Громкость — как у будильника телефона",
                    AppIcons.Tune,
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    .putExtra(Settings.EXTRA_CHANNEL_ID, Notifications.CHANNEL_ALARM),
                            )
                        }
                    },
                )
                OutlinedButton(onClick = { scope.launchSafely(snackbar) { container.wakeAlarms.test() } }, modifier = Modifier.fillMaxWidth()) {
                    Text("Проверить будильник")
                }
            }
            Spacer(Modifier.padding(bottom = 16.dp))
        }
    }
}

@Composable
private fun AlarmItemRow(item: AlarmItem, onClick: () -> Unit, onEnabled: (Boolean) -> Unit) {
    val color = if (item.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                AlarmTexts.time(item),
                style = if (item.beforeShift) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = color,
            )
            val details = listOfNotNull(AlarmTexts.repeat(item), item.label.trim().ifBlank { null }).joinToString(" · ")
            Text(details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = item.enabled, onCheckedChange = onEnabled)
    }
}

private const val UPCOMING_COUNT = 10
