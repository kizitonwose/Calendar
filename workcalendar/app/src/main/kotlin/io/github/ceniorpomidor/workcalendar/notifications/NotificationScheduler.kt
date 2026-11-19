package io.github.ceniorpomidor.workcalendar.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import io.github.ceniorpomidor.workcalendar.data.db.AppDatabase
import io.github.ceniorpomidor.workcalendar.data.db.NotificationStateEntity
import io.github.ceniorpomidor.workcalendar.data.db.toDomain
import io.github.ceniorpomidor.workcalendar.data.repo.AbsenceRepository
import io.github.ceniorpomidor.workcalendar.data.repo.AppClock
import io.github.ceniorpomidor.workcalendar.data.repo.CalcContext
import io.github.ceniorpomidor.workcalendar.data.repo.CalcRepository
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.notify.AbsencePayment
import io.github.ceniorpomidor.workcalendar.domain.notify.NotificationPlanner
import io.github.ceniorpomidor.workcalendar.domain.notify.PlannedNotification
import io.github.ceniorpomidor.workcalendar.domain.pay.PayoutInstance
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime

/**
 * Plans local notifications from the data and keeps AlarmManager in sync. Exact alarms are
 * used when allowed; otherwise inexact alarms are set and late delivery is handled by the
 * planner (overdue items are shown as soon as the alarm fires or the app starts).
 */
class NotificationScheduler(
    private val context: Context,
    private val db: AppDatabase,
    private val calc: CalcRepository,
    private val absences: AbsenceRepository,
    private val clock: AppClock,
) {
    private val mutex = Mutex()
    private val alarmManager: AlarmManager? get() = context.getSystemService(AlarmManager::class.java)

    private class Inputs(
        val context: CalcContext,
        val planner: NotificationPlanner,
        val shifts: List<Shift>,
        val payouts: List<PayoutInstance>,
        val absencePayments: List<AbsencePayment>,
    )

    private suspend fun loadInputs(now: LocalDateTime, until: LocalDateTime): Inputs {
        val context = calc.current()
        val settings = context.settings
        val today = now.toLocalDate()
        val latestEnd = until.minusMinutes(settings.notifications.shiftEndDelayMinutes.coerceAtMost(0).toLong())
        val shifts = db.shiftDao().getPlannedEndedBefore(latestEnd).map { it.toDomain() }
        val periodShifts = db.shiftDao().getRange(today.minusMonths(3).withDayOfMonth(1), until.toLocalDate().plusMonths(2)).map { it.toDomain() }
        val rules = db.financeDao().getRules().map { it.toDomain() }
        val payments = db.financeDao().getPayments().map { it.toDomain() }
        val accruals = db.financeDao().getAccruals().map { it.toDomain() }
        val payouts = context.payouts.between(today.minusDays(1), until.toLocalDate().plusDays(31), rules, periodShifts, accruals, payments, now)
        val paydays = context.payouts.between(today.minusDays(60), today.plusDays(90), rules, periodShifts, accruals, payments, now).map { it.payDate }
        val received = payments.mapNotNull { it.payoutKey }.toSet()
        val relevantAbsences = absences.getAll().filter { it.endDate >= today.minusDays(60) && it.startDate <= today.plusDays(90) }
        val absencePayments = absences.payments(relevantAbsences, context, paydays).filter { it.key !in received }
        val planner = NotificationPlanner(settings.notifications, context.zone, settings.currency)
        return Inputs(context, planner, shifts, payouts, absencePayments)
    }

    /** Recomputes all upcoming notifications and updates the alarms. */
    suspend fun reschedule() = mutex.withLock {
        val now = clock.now()
        val until = now.plusDays(PLAN_DAYS)
        val inputs = loadInputs(now, until)
        val states = db.miscDao().getNotificationStates().associateBy { it.key }
        val delivered = states.values.filter { it.deliveredAt != null }.map { it.key }.toSet()
        val planned = inputs.planner.plan(now, until, inputs.shifts, inputs.payouts, inputs.absencePayments, delivered)
        val plannedKeys = planned.map { it.key }.toSet()
        for (state in states.values) {
            if (state.scheduled && state.key !in plannedKeys) {
                cancelAlarm(state.key)
                db.miscDao().upsertNotificationState(state.copy(scheduled = false))
            }
        }
        for (n in planned) {
            setAlarm(n.key, n.triggerAt, snooze = false)
            db.miscDao().upsertNotificationState(NotificationStateEntity(n.key, n.triggerAt, scheduled = true, deliveredAt = null))
        }
        // Remove shown notifications that are no longer relevant (hours entered, payout received...).
        for (state in states.values) {
            val deliveredAt = state.deliveredAt ?: continue
            if (deliveredAt.isBefore(now.minusDays(2))) continue
            if (inputs.planner.find(state.key, now, inputs.shifts, inputs.payouts, inputs.absencePayments) == null) {
                Notifications.cancel(context, state.key)
            }
        }
        db.miscDao().deleteDeliveredBefore(now.minusDays(120))
    }

    /** Called when an alarm fires: shows the notification with fresh data if still relevant. */
    suspend fun onAlarm(key: String, snooze: Boolean) {
        val shown = mutex.withLock {
            val now = clock.now()
            val state = db.miscDao().getNotificationState(key)
            if (!snooze && state?.deliveredAt != null) return@withLock false
            val inputs = loadInputs(now, now.plusDays(1))
            val n = inputs.planner.findDue(key, now, inputs.shifts, inputs.payouts, inputs.absencePayments, snoozed = snooze)
            if (n == null) {
                if (state != null) db.miscDao().upsertNotificationState(state.copy(scheduled = false))
                return@withLock false
            }
            Notifications.show(context, key, Notifications.build(context, n, allowPayoutReply = !inputs.context.settings.security.financeLock))
            db.miscDao().upsertNotificationState(NotificationStateEntity(key, n.triggerAt, scheduled = false, deliveredAt = now))
            true
        }
        if (shown) reschedule()
    }

    /** Current version of a notification (used when an action needs to re-post it). */
    suspend fun current(key: String): PlannedNotification? {
        val now = clock.now()
        val inputs = loadInputs(now, now.plusDays(1))
        return inputs.planner.find(key, now, inputs.shifts, inputs.payouts, inputs.absencePayments)
    }

    suspend fun snooze(key: String) {
        val minutes = calc.current().settings.notifications.snoozeMinutes.coerceAtLeast(5)
        Notifications.cancel(context, key)
        setAlarm(key, clock.now().plusMinutes(minutes.toLong()), snooze = true)
    }

    /** Marks a notification as handled (e.g. hours were entered) and removes it. */
    suspend fun dismiss(key: String) {
        Notifications.cancel(context, key)
        val state = db.miscDao().getNotificationState(key)
        db.miscDao().upsertNotificationState(
            (state ?: NotificationStateEntity(key, null, scheduled = false, deliveredAt = null)).copy(scheduled = false, deliveredAt = clock.now()),
        )
        cancelAlarm(key)
    }

    fun canScheduleExact(): Boolean {
        val manager = alarmManager ?: return false
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()
    }

    private fun setAlarm(key: String, at: LocalDateTime, snooze: Boolean) {
        val manager = alarmManager ?: return
        val millis = at.atZone(clock.zone()).toInstant().toEpochMilli()
        val intent = alarmPendingIntent(key, snooze)
        try {
            if (canScheduleExact()) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, intent)
            } else {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, intent)
            }
        } catch (e: SecurityException) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, intent)
        }
    }

    private fun cancelAlarm(key: String) {
        val manager = alarmManager ?: return
        manager.cancel(alarmPendingIntent(key, snooze = false))
        manager.cancel(alarmPendingIntent(key, snooze = true))
    }

    private fun alarmPendingIntent(key: String, snooze: Boolean): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM
            data = Uri.parse("workcalendar://alarm/${if (snooze) "snooze" else "main"}/${Uri.encode(key)}")
            putExtra(AlarmReceiver.EXTRA_KEY, key)
            putExtra(AlarmReceiver.EXTRA_SNOOZE, snooze)
        }
        return PendingIntent.getBroadcast(context, (key + snooze).hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        /** How far ahead alarms are set; the planner runs again on every data change and periodically. */
        const val PLAN_DAYS: Long = 14
    }
}
