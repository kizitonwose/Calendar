package io.github.ceniorpomidor.workcalendar.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import io.github.ceniorpomidor.workcalendar.appContainer
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import io.github.ceniorpomidor.workcalendar.domain.util.HoursParser
import io.github.ceniorpomidor.workcalendar.work.MaintenanceWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

/** Runs [block] off the main thread while keeping the broadcast alive. */
private fun BroadcastReceiver.runAsync(block: suspend () -> Unit) {
    val pending = goAsync()
    receiverScope.launch {
        try {
            block()
        } catch (e: Exception) {
            android.util.Log.e("WorkCalendar", "Broadcast handling failed", e)
        } finally {
            pending.finish()
        }
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val key = intent.getStringExtra(EXTRA_KEY) ?: return
        val snooze = intent.getBooleanExtra(EXTRA_SNOOZE, false)
        runAsync { context.appContainer.notifications.onAlarm(key, snooze) }
    }

    companion object {
        const val ACTION_ALARM = "io.github.ceniorpomidor.workcalendar.ALARM"
        const val EXTRA_KEY = "key"
        const val EXTRA_SNOOZE = "snooze"
    }
}

/** Actions from notifications: confirm the planned hours, enter hours inline, snooze. */
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val key = intent.getStringExtra(EXTRA_KEY) ?: return
        val shiftId = intent.getLongExtra(EXTRA_SHIFT_ID, -1L)
        val container = context.appContainer
        // Results are shown as a separate short notification; the original one is removed.
        val savedKey = "$key:saved"
        when (intent.action) {
            ACTION_SNOOZE -> runAsync { container.notifications.snooze(key) }
            ACTION_CONFIRM_FULL -> runAsync {
                if (shiftId <= 0) return@runAsync
                try {
                    val shift = container.shifts.confirmAsPlanned(shiftId)
                    container.notifications.dismiss(key)
                    val amount = shift.pay?.total?.let { " · ${Formats.money(it)}" } ?: ""
                    Notifications.show(
                        context,
                        savedKey,
                        Notifications.buildSaved(
                            context,
                            savedKey,
                            "Часы сохранены",
                            "${Formats.shortDate(shift.date)}: ${Formats.hours((shift.workedMinutes ?: 0).toLong())}$amount",
                            "confirm/$shiftId",
                        ),
                    )
                } catch (e: Exception) {
                    Notifications.show(context, savedKey, Notifications.buildSaved(context, savedKey, "Не удалось сохранить", e.message ?: "Ошибка", "confirm/$shiftId"))
                }
            }
            ACTION_REPLY_AMOUNT -> runAsync { receiveAmount(context, key, intent) }
            ACTION_REPLY_HOURS -> runAsync {
                if (shiftId <= 0) return@runAsync
                val text = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(Notifications.KEY_HOURS)?.toString().orEmpty()
                val minutes = HoursParser.parse(text)
                if (minutes == null) {
                    val current = container.notifications.current(key)
                    if (current != null) {
                        Notifications.show(context, key, Notifications.build(context, current, "Не удалось распознать «$text». Введите часы, например 7,5 или 7:30"))
                    }
                    return@runAsync
                }
                try {
                    val shift = if (minutes == 0) {
                        container.shifts.markMissed(shiftId)
                        null
                    } else {
                        container.shifts.confirm(shiftId, minutes)
                    }
                    container.notifications.dismiss(key)
                    val body = if (shift == null) {
                        "Смена отмечена как несостоявшаяся"
                    } else {
                        "${Formats.shortDate(shift.date)}: ${Formats.hours(minutes.toLong())}" + (shift.pay?.total?.let { " · ${Formats.money(it)}" } ?: "")
                    }
                    Notifications.show(context, savedKey, Notifications.buildSaved(context, savedKey, "Часы сохранены", body, "confirm/$shiftId"))
                } catch (e: Exception) {
                    Notifications.show(context, savedKey, Notifications.buildSaved(context, savedKey, "Не удалось сохранить", e.message ?: "Ошибка", "confirm/$shiftId"))
                }
            }
        }
    }

    private suspend fun receiveAmount(context: Context, key: String, intent: Intent) {
        val container = context.appContainer
        val text = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(Notifications.KEY_AMOUNT)?.toString().orEmpty()
        val amount = Money.parse(text)
        val route = "payment/new?key=$key"
        val savedKey = "$key:saved"
        val error = if (amount == null || amount.isNegative) {
            "Введите сумму от нуля, например 12 500,50"
        } else {
            try {
                val saved = container.finance.receivePayoutReply(key, amount)
                container.notifications.dismiss(key)
                val body = if (saved) Formats.money(amount, container.settings.get().currency) else "Эта выплата уже отмечена"
                Notifications.show(context, savedKey, Notifications.buildSaved(context, savedKey, "Выплата получена", body, route, Notifications.CHANNEL_PAYOUTS))
                return
            } catch (e: Exception) {
                e.message ?: "Не удалось сохранить выплату"
            }
        }
        val current = container.notifications.current(key)
        if (current != null) {
            val inline = !container.settings.get().security.financeLock
            Notifications.show(context, key, Notifications.build(context, current, error, allowPayoutReply = inline))
        } else {
            Notifications.cancel(context, key)
            Notifications.show(context, savedKey, Notifications.buildSaved(context, savedKey, "Проверьте выплату", error, route, Notifications.CHANNEL_PAYOUTS))
        }
    }

    companion object {
        const val ACTION_REPLY_AMOUNT = "io.github.ceniorpomidor.workcalendar.REPLY_AMOUNT"
        const val ACTION_CONFIRM_FULL = "io.github.ceniorpomidor.workcalendar.CONFIRM_FULL"
        const val ACTION_REPLY_HOURS = "io.github.ceniorpomidor.workcalendar.REPLY_HOURS"
        const val ACTION_SNOOZE = "io.github.ceniorpomidor.workcalendar.SNOOZE"
        const val EXTRA_KEY = "key"
        const val EXTRA_SHIFT_ID = "shiftId"
    }
}

/** Restores alarms after a reboot, an app update or a change of the system time / time zone. */
class SystemEventsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val container = context.appContainer
        runAsync {
            container.schedule.ensureHorizon()
            container.notifications.reschedule()
            container.wakeAlarms.reschedule()
        }
        MaintenanceWorker.schedule(context)
    }
}

/** Wake-up alarm: the alarm time has come, or a button of the alarm notification was pressed. */
class WakeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarms = context.appContainer.wakeAlarms
        when (intent.action) {
            ACTION_FIRE -> runAsync { alarms.onFired(intent) }
            ACTION_SNOOZE -> runAsync { alarms.snooze() }
            ACTION_DISMISS -> runAsync { alarms.dismiss() }
            ACTION_CANCEL_SNOOZE -> runAsync { alarms.cancelSnooze() }
        }
    }

    companion object {
        const val ACTION_FIRE = "io.github.ceniorpomidor.workcalendar.WAKE_ALARM"
        const val ACTION_SNOOZE = "io.github.ceniorpomidor.workcalendar.WAKE_ALARM_SNOOZE"
        const val ACTION_DISMISS = "io.github.ceniorpomidor.workcalendar.WAKE_ALARM_DISMISS"
        const val ACTION_CANCEL_SNOOZE = "io.github.ceniorpomidor.workcalendar.WAKE_ALARM_CANCEL_SNOOZE"
    }
}
