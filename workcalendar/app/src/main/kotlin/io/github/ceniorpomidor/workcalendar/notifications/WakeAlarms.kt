package io.github.ceniorpomidor.workcalendar.notifications

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.github.ceniorpomidor.workcalendar.R
import io.github.ceniorpomidor.workcalendar.data.db.AppDatabase
import io.github.ceniorpomidor.workcalendar.data.db.toDomain
import io.github.ceniorpomidor.workcalendar.data.repo.AppClock
import io.github.ceniorpomidor.workcalendar.data.repo.SettingsRepository
import io.github.ceniorpomidor.workcalendar.domain.alarm.AlarmPlanner
import io.github.ceniorpomidor.workcalendar.domain.alarm.PlannedAlarm
import io.github.ceniorpomidor.workcalendar.domain.model.AlarmRepeat
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.ui.alarm.WakeAlarmActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime

/** The alarm that is ringing now. */
data class RingingAlarm(
    val title: String,
    val text: String,
    val snoozeMinutes: Int,
    val ringMinutes: Int,
) {
    fun toIntent(intent: Intent): Intent = intent
        .putExtra(EXTRA_TITLE, title)
        .putExtra(EXTRA_TEXT, text)
        .putExtra(EXTRA_SNOOZE_MINUTES, snoozeMinutes)
        .putExtra(EXTRA_RING_MINUTES, ringMinutes)

    companion object {
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_TEXT = "text"
        private const val EXTRA_SNOOZE_MINUTES = "snoozeMinutes"
        private const val EXTRA_RING_MINUTES = "ringMinutes"

        fun fromIntent(intent: Intent?): RingingAlarm? {
            val title = intent?.getStringExtra(EXTRA_TITLE) ?: return null
            return RingingAlarm(title, intent.getStringExtra(EXTRA_TEXT).orEmpty(), intent.getIntExtra(EXTRA_SNOOZE_MINUTES, 10), intent.getIntExtra(EXTRA_RING_MINUTES, 10))
        }
    }
}

/**
 * Wake-up alarms of the app. Only the nearest ring (or the snoozed one) is set in AlarmManager,
 * as an exact alarm that fires in Doze; after it fires the next one is set. It is deliberately
 * not an "alarm clock" (setAlarmClock): the system shows only the alarms of the clock app in the
 * status bar, on the lock screen and in the quick settings tile. The alarm rings with an
 * insistent notification on the alarm sound channel that opens [WakeAlarmActivity] over the lock
 * screen.
 */
class WakeAlarmScheduler(
    private val context: Context,
    private val db: AppDatabase,
    private val settings: SettingsRepository,
    private val clock: AppClock,
) {
    private val mutex = Mutex()
    private val prefs by lazy { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    private val alarmManager: AlarmManager? get() = context.getSystemService(AlarmManager::class.java)

    private val ringingState = MutableStateFlow<RingingAlarm?>(null)
    private val stopsState = MutableStateFlow(0)

    /** The alarm that rings now. */
    val ringing: StateFlow<RingingAlarm?> get() = ringingState

    /** Grows every time a ringing alarm is stopped: the alarm screen closes. */
    val stops: StateFlow<Int> get() = stopsState

    /** Alarms after now, nearest first. */
    suspend fun upcoming(limit: Int): List<PlannedAlarm> {
        val now = clock.now()
        return AlarmPlanner.upcoming(settings.get().alarm, shiftsFrom(now), now, skipKey = prefs.getString(KEY_LAST, null)).take(limit)
    }

    /** Time of the snoozed alarm, if any. */
    fun snoozedUntil(): LocalDateTime? = prefs.getString(KEY_SNOOZE_AT, null)
        ?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
        ?.takeIf { it.isAfter(clock.now().minusMinutes(1)) }

    private suspend fun shiftsFrom(now: LocalDateTime): List<Shift> {
        val today = now.toLocalDate()
        return db.shiftDao().getRange(today, today.plusDays(AlarmPlanner.LOOKAHEAD_DAYS + 2L)).map { it.toDomain() }
    }

    /** Sets the nearest alarm in AlarmManager (or removes it when there is none). */
    suspend fun reschedule() = mutex.withLock {
        val now = clock.now()
        val next = AlarmPlanner.upcoming(settings.get().alarm, shiftsFrom(now), now, skipKey = prefs.getString(KEY_LAST, null)).firstOrNull()
        val snoozeAt = snoozedUntil()?.takeIf { it.isAfter(now) }
        when {
            snoozeAt != null && (next == null || !next.at.isBefore(snoozeAt)) -> set(snoozeAt, key = null, text = prefs.getString(KEY_SNOOZE_TEXT, null).orEmpty(), snooze = true)
            next != null -> set(next.at, key = next.key, text = next.description(), snooze = false, alarmId = next.alarm.id)
            else -> alarmManager?.cancel(firePendingIntent(null))
        }
    }

    /** Called by AlarmManager: rings unless the alarm comes too late (the phone was off). */
    suspend fun onFired(intent: Intent) {
        val at = intent.getStringExtra(EXTRA_AT)?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
        val key = intent.getStringExtra(EXTRA_KEY)
        val text = intent.getStringExtra(EXTRA_TEXT).orEmpty()
        if (intent.getBooleanExtra(EXTRA_SNOOZE, false)) clearSnooze()
        if (key != null) prefs.edit().putString(KEY_LAST, key).apply()
        // A one-time alarm has done its job.
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, 0L)
        if (alarmId != 0L) {
            settings.update { s ->
                val item = s.alarm.item(alarmId)
                if (item?.repeat == AlarmRepeat.ONCE) s.copy(alarm = s.alarm.remove(alarmId).cleaned(clock.today())) else s
            }
        }
        val now = clock.now()
        if (at == null || !at.plusMinutes(LATE_LIMIT_MINUTES).isBefore(now)) {
            val alarm = settings.get().alarm
            ring(RingingAlarm("Будильник ${Formats.time(at ?: now)}", text, alarm.snoozeMinutes, alarm.ringMinutes))
        }
        reschedule()
    }

    /** Rings right now to check the sound and the screen. */
    suspend fun test() {
        val alarm = settings.get().alarm
        val ringing = RingingAlarm("Будильник ${Formats.time(clock.now())}", "Проверка будильника", alarm.snoozeMinutes, alarm.ringMinutes)
        ring(ringing)
        context.startActivity(activityIntent(ringing))
    }

    suspend fun snooze() {
        val current = ringingState.value
        val minutes = (current?.snoozeMinutes ?: settings.get().alarm.snoozeMinutes).coerceAtLeast(1)
        stopRinging()
        val at = clock.now().plusMinutes(minutes.toLong())
        prefs.edit().putString(KEY_SNOOZE_AT, at.toString()).putString(KEY_SNOOZE_TEXT, current?.text.orEmpty()).apply()
        Notifications.show(context, KEY_SNOOZED, buildSnoozed(at))
        reschedule()
    }

    suspend fun dismiss() {
        stopRinging()
        reschedule()
    }

    /** Turns off the snoozed alarm. */
    suspend fun cancelSnooze() {
        clearSnooze()
        reschedule()
    }

    fun canUseFullScreen(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
        return context.getSystemService(NotificationManager::class.java)?.canUseFullScreenIntent() ?: false
    }

    private fun ring(alarm: RingingAlarm) {
        ringingState.value = alarm
        Notifications.show(context, KEY_RING, buildRing(alarm))
    }

    private fun stopRinging() {
        ringingState.value = null
        stopsState.value += 1
        Notifications.cancel(context, KEY_RING)
    }

    private fun clearSnooze() {
        prefs.edit().remove(KEY_SNOOZE_AT).remove(KEY_SNOOZE_TEXT).apply()
        Notifications.cancel(context, KEY_SNOOZED)
    }

    private fun set(at: LocalDateTime, key: String?, text: String, snooze: Boolean, alarmId: Long = 0L) {
        val manager = alarmManager ?: return
        val millis = at.atZone(clock.zone()).toInstant().toEpochMilli()
        val operation = firePendingIntent(
            Intent(context, WakeAlarmReceiver::class.java)
                .setAction(WakeAlarmReceiver.ACTION_FIRE)
                .putExtra(EXTRA_AT, at.toString())
                .putExtra(EXTRA_KEY, key)
                .putExtra(EXTRA_TEXT, text)
                .putExtra(EXTRA_SNOOZE, snooze)
                .putExtra(EXTRA_ALARM_ID, alarmId),
        )
        // The same PendingIntent replaces the previous ring, including an alarm clock set by version 1.1.0.
        try {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, operation)
        } catch (e: SecurityException) {
            // Exact alarms are not allowed: the alarm may be a few minutes late.
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, operation)
        }
    }

    private fun firePendingIntent(intent: Intent?): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_FIRE,
        intent ?: Intent(context, WakeAlarmReceiver::class.java).setAction(WakeAlarmReceiver.ACTION_FIRE),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun activityIntent(alarm: RingingAlarm): Intent = alarm.toIntent(
        Intent(context, WakeAlarmActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION),
    )

    private fun actionIntent(action: String): PendingIntent = PendingIntent.getBroadcast(
        context,
        action.hashCode(),
        Intent(context, WakeAlarmReceiver::class.java).setAction(action),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun buildRing(alarm: RingingAlarm): Notification {
        val screen = PendingIntent.getActivity(context, REQUEST_SCREEN, activityIntent(alarm), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, Notifications.CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_alarm)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setContentTitle(alarm.title)
            .setContentText(alarm.text.ifBlank { null })
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(screen, true)
            .setContentIntent(screen)
            .setDeleteIntent(actionIntent(WakeAlarmReceiver.ACTION_DISMISS))
            .addAction(0, "Отложить на ${alarm.snoozeMinutes} мин", actionIntent(WakeAlarmReceiver.ACTION_SNOOZE))
            .addAction(0, "Выключить", actionIntent(WakeAlarmReceiver.ACTION_DISMISS))
            .setTimeoutAfter(alarm.ringMinutes.coerceAtLeast(1) * 60_000L)
            .build()
        // The sound repeats until the alarm is stopped.
        notification.flags = notification.flags or Notification.FLAG_INSISTENT
        return notification
    }

    private fun buildSnoozed(at: LocalDateTime): Notification = NotificationCompat.Builder(context, Notifications.CHANNEL_ALARM_STATUS)
        .setSmallIcon(R.drawable.ic_alarm)
        .setColor(ContextCompat.getColor(context, R.color.notification_accent))
        .setContentTitle("Будильник отложен до ${Formats.time(at)}")
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setOngoing(true)
        .setSilent(true)
        .setContentIntent(Notifications.openAppIntent(context, "alarm", REQUEST_SHOW))
        .addAction(0, "Выключить", actionIntent(WakeAlarmReceiver.ACTION_CANCEL_SNOOZE))
        .build()

    companion object {
        private const val PREFS = "wake_alarm"
        private const val KEY_LAST = "last"
        private const val KEY_SNOOZE_AT = "snoozeAt"
        private const val KEY_SNOOZE_TEXT = "snoozeText"
        const val KEY_RING = "wake-alarm"
        const val KEY_SNOOZED = "wake-alarm-snoozed"

        private const val EXTRA_AT = "at"
        private const val EXTRA_KEY = "key"
        private const val EXTRA_TEXT = "text"
        private const val EXTRA_SNOOZE = "snooze"
        private const val EXTRA_ALARM_ID = "alarmId"

        private const val REQUEST_FIRE = 7001
        private const val REQUEST_SHOW = 7002
        private const val REQUEST_SCREEN = 7003

        /** An alarm delivered later than this (the phone was off) does not ring. */
        private const val LATE_LIMIT_MINUTES = 30L
    }
}
