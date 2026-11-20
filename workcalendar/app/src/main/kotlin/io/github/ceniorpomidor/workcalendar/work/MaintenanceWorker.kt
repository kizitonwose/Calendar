package io.github.ceniorpomidor.workcalendar.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.ceniorpomidor.workcalendar.appContainer
import io.github.ceniorpomidor.workcalendar.widget.WidgetUpdater
import java.util.concurrent.TimeUnit

/**
 * Periodic background job: extends generated shifts to the horizon, re-plans notifications
 * (safety net if an alarm was lost), keeps a weekly local backup and refreshes the widget.
 * All steps are idempotent, so repeated or parallel runs cannot duplicate data.
 */
class MaintenanceWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val container = applicationContext.appContainer
        return try {
            container.schedule.ensureHorizon()
            container.notifications.reschedule()
            container.wakeAlarms.reschedule()
            container.backup.autoBackupIfDue()
            WidgetUpdater.updateAll(applicationContext)
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("WorkCalendar", "Maintenance failed", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val PERIODIC = "maintenance"
        private const val ONCE = "maintenance-once"

        fun schedule(context: Context) {
            val manager = WorkManager.getInstance(context)
            val periodic = PeriodicWorkRequestBuilder<MaintenanceWorker>(6, TimeUnit.HOURS).build()
            manager.enqueueUniquePeriodicWork(PERIODIC, ExistingPeriodicWorkPolicy.KEEP, periodic)
        }

        fun runOnce(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(ONCE, ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<MaintenanceWorker>().build())
        }
    }
}
