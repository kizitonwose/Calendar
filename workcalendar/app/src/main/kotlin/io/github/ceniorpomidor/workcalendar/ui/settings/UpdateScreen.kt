package io.github.ceniorpomidor.workcalendar.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.ceniorpomidor.workcalendar.AppContainer
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.domain.update.AvailableUpdate
import io.github.ceniorpomidor.workcalendar.ui.components.Banner
import io.github.ceniorpomidor.workcalendar.ui.components.BannerKind
import io.github.ceniorpomidor.workcalendar.ui.components.ScrollColumn
import io.github.ceniorpomidor.workcalendar.ui.components.SectionCard
import io.github.ceniorpomidor.workcalendar.ui.components.SubScreen
import io.github.ceniorpomidor.workcalendar.ui.components.SwitchRow
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import io.github.ceniorpomidor.workcalendar.update.AppUpdater
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

private sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class Available(val update: AvailableUpdate) : UpdateState
    data class Downloading(val update: AvailableUpdate, val progress: Float) : UpdateState
    data class Ready(val update: AvailableUpdate, val file: File) : UpdateState
    data class Failed(val message: String, val update: AvailableUpdate?) : UpdateState
}

/** Checks GitHub for a new version, downloads it and opens the system installer. */
@Composable
fun UpdateScreen(container: AppContainer, settings: AppSettings, onBack: () -> Unit, checkAtOnce: Boolean = false) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val updater = container.updater
    var state by remember { mutableStateOf<UpdateState>(UpdateState.Idle) }
    var refresh by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { refresh++ }
    val canInstall = remember(refresh) { updater.canInstall() }
    val updateSettings = rememberSettingsUpdater(container)

    fun check() {
        state = UpdateState.Checking
        scope.launch {
            state = try {
                updater.check()?.let { UpdateState.Available(it) } ?: UpdateState.UpToDate
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                UpdateState.Failed(e.message ?: "Нет связи с GitHub", null)
            }
        }
    }

    fun download(available: AvailableUpdate) {
        state = UpdateState.Downloading(available, 0f)
        scope.launch {
            state = try {
                var shown = -1
                val file = updater.download(available) { progress ->
                    // Redraw only when the whole percent changes.
                    val percent = (progress * 100).toInt()
                    if (percent != shown) {
                        shown = percent
                        state = UpdateState.Downloading(available, progress)
                    }
                }
                if (updater.canInstall()) updater.install(context, file)
                UpdateState.Ready(available, file)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                UpdateState.Failed(e.message ?: "Не удалось скачать обновление", available)
            }
        }
    }

    // Opened from the message about a new version: show it at once.
    LaunchedEffect(Unit) { if (checkAtOnce && state == UpdateState.Idle) check() }

    SubScreen(title = "Обновление приложения", onBack = onBack) { padding ->
        ScrollColumn(padding) {
            SectionCard(title = "Версия ${updater.currentVersion}", icon = AppIcons.Info) {
                Text(
                    "Новые версии публикуются на GitHub. Обновление ставится поверх установленной версии, все данные сохраняются.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                when (val s = state) {
                    UpdateState.Idle -> Unit
                    UpdateState.Checking -> {
                        Text("Проверка…", style = MaterialTheme.typography.bodyMedium)
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                    UpdateState.UpToDate -> Text("Установлена последняя версия", style = MaterialTheme.typography.titleSmall)
                    is UpdateState.Failed -> Banner(s.message, BannerKind.ERROR)
                    else -> Unit
                }
                if (state.update == null) {
                    OutlinedButton(onClick = { check() }, enabled = state != UpdateState.Checking, modifier = Modifier.fillMaxWidth()) {
                        Text("Проверить обновления")
                    }
                }
            }
            val s = state
            val available = s.update
            if (available != null) {
                SectionCard(title = "Доступна версия ${available.version}", icon = AppIcons.Download) {
                    if (available.notes.isEmpty()) {
                        Text("Описание изменений — на странице релиза.", style = MaterialTheme.typography.bodyMedium)
                    }
                    available.notes.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    if (!canInstall) {
                        Banner(
                            "Разрешите Рабочему календарю устанавливать приложения — это нужно, чтобы поставить обновление.",
                            BannerKind.WARNING,
                            action = { TextButton(onClick = { updater.openInstallPermission(context) }) { Text("Разрешить") } },
                        )
                    }
                    when (s) {
                        is UpdateState.Downloading -> {
                            Text("Загрузка… ${(s.progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                            LinearProgressIndicator(progress = { s.progress }, modifier = Modifier.fillMaxWidth())
                        }
                        is UpdateState.Ready -> Button(
                            onClick = {
                                try {
                                    updater.install(context, s.file)
                                } catch (e: Exception) {
                                    state = UpdateState.Failed(e.message ?: "Не удалось открыть установщик", available)
                                }
                            },
                            enabled = canInstall,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Установить", fontWeight = FontWeight.SemiBold) }
                        else -> Button(onClick = { download(available) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Скачать и установить (${sizeText(available.apk.size)})", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(available.release.htmlUrl.ifBlank { RELEASES })))
                        }) { Text("Страница релиза") }
                    }
                }
            }
            SectionCard(title = "При запуске", icon = AppIcons.Refresh) {
                SwitchRow(
                    "Проверять при запуске",
                    "При открытии приложения; о новой версии появится сообщение. Ничего не скачивается без вашего согласия",
                    settings.autoUpdateCheck,
                ) { v -> updateSettings { it.copy(autoUpdateCheck = v) } }
                TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(RELEASES))) }) { Text("Все версии на GitHub") }
            }
        }
    }
}

/** The version the state is about, if any. */
private val UpdateState.update: AvailableUpdate?
    get() = when (this) {
        is UpdateState.Available -> update
        is UpdateState.Downloading -> update
        is UpdateState.Ready -> update
        is UpdateState.Failed -> update
        else -> null
    }

private const val RELEASES = AppUpdater.RELEASES_PAGE

private fun sizeText(bytes: Long): String = if (bytes <= 0) "APK" else String.format(Locale.forLanguageTag("ru"), "%.1f МБ", bytes / 1_048_576.0)
