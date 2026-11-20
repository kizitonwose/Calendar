package io.github.ceniorpomidor.workcalendar.ui.alarm

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ceniorpomidor.workcalendar.appContainer
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.notifications.RingingAlarm
import io.github.ceniorpomidor.workcalendar.ui.theme.AppIcons
import io.github.ceniorpomidor.workcalendar.ui.theme.WorkCalendarTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/** Ringing alarm over the lock screen: snooze or turn off. */
class WakeAlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        showOverLockScreen()
        val container = appContainer
        val alarms = container.wakeAlarms
        val alarm = alarms.ringing.value ?: RingingAlarm.fromIntent(intent)
        if (alarm == null) {
            finish()
            return
        }
        val stopsAtStart = alarms.stops.value
        // The alarm is stopped only with the buttons.
        onBackPressedDispatcher.addCallback(this) {}
        setContent {
            val settings by container.settings.settings.collectAsStateWithLifecycle(initialValue = null)
            val stops by alarms.stops.collectAsStateWithLifecycle()
            LaunchedEffect(stops) { if (stops != stopsAtStart) finish() }
            LaunchedEffect(Unit) {
                // The notification stops ringing after this time as well.
                delay(alarm.ringMinutes.coerceAtLeast(1) * 60_000L)
                finish()
            }
            val s = settings ?: AppSettings()
            WorkCalendarTheme(themeMode = s.theme, dynamicColor = s.dynamicColor, appearance = s.appearance) {
                AlarmContent(
                    alarm = alarm,
                    now = { container.clock.now() },
                    // The application scope: the screen closes before the work is done.
                    onSnooze = { container.scope.launch { alarms.snooze() } },
                    onDismiss = { container.scope.launch { alarms.dismiss() } },
                )
            }
        }
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

@Composable
private fun AlarmContent(alarm: RingingAlarm, now: () -> LocalDateTime, onSnooze: () -> Unit, onDismiss: () -> Unit) {
    var time by remember { mutableStateOf(now()) }
    LaunchedEffect(Unit) {
        while (true) {
            time = now()
            delay(1_000)
        }
    }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.padding(top = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(AppIcons.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text(Formats.time(time), fontSize = 80.sp, fontWeight = FontWeight.Light)
                Text(Formats.dayTitle(time.toLocalDate()).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(32.dp))
                Text(alarm.title, style = MaterialTheme.typography.headlineSmall)
                if (alarm.text.isNotBlank()) {
                    Text(alarm.text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
            Column(Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(onClick = onSnooze, modifier = Modifier.fillMaxWidth().height(64.dp)) {
                    Icon(AppIcons.Snooze, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Отложить на ${alarm.snoozeMinutes} мин", style = MaterialTheme.typography.titleMedium)
                }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(64.dp)) {
                    Icon(AppIcons.AlarmOff, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Выключить", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
