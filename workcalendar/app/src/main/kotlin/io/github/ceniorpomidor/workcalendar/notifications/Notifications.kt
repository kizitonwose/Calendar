package io.github.ceniorpomidor.workcalendar.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import io.github.ceniorpomidor.workcalendar.MainActivity
import io.github.ceniorpomidor.workcalendar.R
import io.github.ceniorpomidor.workcalendar.domain.notify.NotificationKind
import io.github.ceniorpomidor.workcalendar.domain.notify.PlannedNotification
import io.github.ceniorpomidor.workcalendar.domain.util.Formats

/** Notification channels, builders and deep links. */
object Notifications {
    const val CHANNEL_SHIFTS = "shifts"
    const val CHANNEL_REMINDERS = "reminders"
    const val CHANNEL_PAYOUTS = "payouts"
    const val CHANNEL_ABSENCES = "absences"
    const val CHANNEL_ALARM = "wake_alarm"
    const val CHANNEL_ALARM_STATUS = "wake_alarm_status"

    const val KEY_HOURS = "hours"
    const val KEY_AMOUNT = "amount"
    const val EXTRA_ROUTE = "route"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channels = listOf(
            NotificationChannel(CHANNEL_SHIFTS, context.getString(R.string.channel_shifts), NotificationManager.IMPORTANCE_HIGH).apply {
                description = context.getString(R.string.channel_shifts_description)
            },
            NotificationChannel(CHANNEL_REMINDERS, context.getString(R.string.channel_reminders), NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = context.getString(R.string.channel_reminders_description)
            },
            NotificationChannel(CHANNEL_PAYOUTS, context.getString(R.string.channel_payouts), NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = context.getString(R.string.channel_payouts_description)
            },
            NotificationChannel(CHANNEL_ABSENCES, context.getString(R.string.channel_absences), NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = context.getString(R.string.channel_absences_description)
            },
            NotificationChannel(CHANNEL_ALARM, context.getString(R.string.channel_alarm), NotificationManager.IMPORTANCE_HIGH).apply {
                description = context.getString(R.string.channel_alarm_description)
                val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                // Played on the alarm stream: rings in silent and vibrate modes, passes "Do not disturb" when alarms are allowed.
                setSound(sound, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 600, 800, 600)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            },
            NotificationChannel(CHANNEL_ALARM_STATUS, context.getString(R.string.channel_alarm_status), NotificationManager.IMPORTANCE_LOW).apply {
                description = context.getString(R.string.channel_alarm_status_description)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            },
        )
        manager.createNotificationChannels(channels)
    }

    fun canPost(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun idFor(key: String): Int = key.hashCode()

    /** Intent that opens the app on [route] (see the navigation routes). */
    fun openAppIntent(context: Context, route: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("workcalendar://open/${Uri.encode(route)}")
            putExtra(EXTRA_ROUTE, route)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun actionIntent(context: Context, action: String, key: String, shiftId: Long?, mutable: Boolean = false): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
            data = Uri.parse("workcalendar://action/${Uri.encode(action)}/${Uri.encode(key)}")
            putExtra(NotificationActionReceiver.EXTRA_KEY, key)
            if (shiftId != null) putExtra(NotificationActionReceiver.EXTRA_SHIFT_ID, shiftId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or if (mutable) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, (key + action).hashCode(), intent, flags)
    }

    fun routeFor(n: PlannedNotification): String = when (n.kind) {
        NotificationKind.SHIFT_END -> n.shiftId?.let { "confirm/$it" } ?: "unconfirmed"
        NotificationKind.UNCONFIRMED -> "unconfirmed"
        NotificationKind.PAYOUT -> n.payoutKey?.let { "payment/new?key=$it" } ?: "finance"
        NotificationKind.VACATION_PAY, NotificationKind.SICK_PAY -> n.absenceId?.let { "absence/$it" } ?: "finance"
    }

    private fun channelFor(kind: NotificationKind): String = when (kind) {
        NotificationKind.SHIFT_END -> CHANNEL_SHIFTS
        NotificationKind.UNCONFIRMED -> CHANNEL_REMINDERS
        NotificationKind.PAYOUT -> CHANNEL_PAYOUTS
        NotificationKind.VACATION_PAY, NotificationKind.SICK_PAY -> CHANNEL_ABSENCES
    }

    fun build(context: Context, n: PlannedNotification, errorText: String? = null, allowPayoutReply: Boolean = false): Notification {
        val id = idFor(n.key)
        val builder = NotificationCompat.Builder(context, channelFor(n.kind))
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setContentTitle(n.title)
            .setContentText(errorText ?: n.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(errorText ?: n.text))
            .setContentIntent(openAppIntent(context, routeFor(n), id))
            .setAutoCancel(true)
            .setOnlyAlertOnce(errorText != null)
            .setCategory(if (n.kind == NotificationKind.SHIFT_END) NotificationCompat.CATEGORY_REMINDER else NotificationCompat.CATEGORY_EVENT)
        if (n.kind == NotificationKind.SHIFT_END && n.shiftId != null) {
            val planned = n.plannedMinutes ?: 0
            builder.addAction(
                NotificationCompat.Action.Builder(0, "Полностью (${Formats.hoursDecimal(planned)} ч)", actionIntent(context, NotificationActionReceiver.ACTION_CONFIRM_FULL, n.key, n.shiftId)).build(),
            )
            val remoteInput = RemoteInput.Builder(KEY_HOURS).setLabel("Часы, например 7,5 или 7:30").build()
            builder.addAction(
                NotificationCompat.Action.Builder(0, "Ввести часы", actionIntent(context, NotificationActionReceiver.ACTION_REPLY_HOURS, n.key, n.shiftId, mutable = true))
                    .addRemoteInput(remoteInput)
                    .setAllowGeneratedReplies(false)
                    .build(),
            )
        }
        val payoutKey = n.payoutKey
        if (n.kind == NotificationKind.PAYOUT && payoutKey != null) {
            val action = if (allowPayoutReply) {
                NotificationCompat.Action.Builder(0, "Внести сумму", actionIntent(context, NotificationActionReceiver.ACTION_REPLY_AMOUNT, payoutKey, null, mutable = true))
                    .addRemoteInput(RemoteInput.Builder(KEY_AMOUNT).setLabel("Полученная сумма, например 12 500,50").build())
                    .setAllowGeneratedReplies(false)
                    .setAuthenticationRequired(true)
            } else {
                NotificationCompat.Action.Builder(0, "Внести сумму", openAppIntent(context, routeFor(n), id))
            }
            builder.addAction(action.build())
        }
        builder.addAction(NotificationCompat.Action.Builder(0, "Отложить", actionIntent(context, NotificationActionReceiver.ACTION_SNOOZE, n.key, n.shiftId)).build())
        return builder.build()
    }

    /** Short feedback for a notification action. */
    fun buildSaved(context: Context, key: String, title: String, text: String, route: String, channel: String = CHANNEL_SHIFTS): Notification =
        NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openAppIntent(context, route, idFor(key)))
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setTimeoutAfter(10_000)
            .build()

    fun show(context: Context, key: String, notification: Notification) {
        if (!canPost(context)) return
        try {
            NotificationManagerCompat.from(context).notify(idFor(key), notification)
        } catch (e: SecurityException) {
            // Permission was revoked meanwhile.
        }
    }

    fun cancel(context: Context, key: String) {
        NotificationManagerCompat.from(context).cancel(idFor(key))
    }
}
