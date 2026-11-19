package io.github.ceniorpomidor.workcalendar

import android.app.Application
import android.content.Context
import io.github.ceniorpomidor.workcalendar.data.db.AppDatabase
import io.github.ceniorpomidor.workcalendar.data.repo.AbsenceRepository
import io.github.ceniorpomidor.workcalendar.data.repo.AppClock
import io.github.ceniorpomidor.workcalendar.data.repo.BackupRepository
import io.github.ceniorpomidor.workcalendar.data.repo.CalcRepository
import io.github.ceniorpomidor.workcalendar.data.repo.ChangeLogger
import io.github.ceniorpomidor.workcalendar.data.repo.DataChanges
import io.github.ceniorpomidor.workcalendar.data.repo.FinanceRepository
import io.github.ceniorpomidor.workcalendar.data.repo.ScheduleRepository
import io.github.ceniorpomidor.workcalendar.data.repo.SettingsRepository
import io.github.ceniorpomidor.workcalendar.data.repo.ShiftRepository
import io.github.ceniorpomidor.workcalendar.data.repo.SystemAppClock
import io.github.ceniorpomidor.workcalendar.notifications.NotificationScheduler
import io.github.ceniorpomidor.workcalendar.notifications.Notifications
import io.github.ceniorpomidor.workcalendar.notifications.WakeAlarmScheduler
import io.github.ceniorpomidor.workcalendar.update.AppUpdater
import io.github.ceniorpomidor.workcalendar.widget.WidgetUpdater
import io.github.ceniorpomidor.workcalendar.work.MaintenanceWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

/** Manual dependency container shared by the UI, receivers, workers and the widget. */
class AppContainer(val context: Context) {
    val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val clock: AppClock = SystemAppClock
    val db: AppDatabase = AppDatabase.build(context)
    val changes: DataChanges = DataChanges()
    val log: ChangeLogger = ChangeLogger(db, clock)
    val settings: SettingsRepository = SettingsRepository(db, changes)
    val calc: CalcRepository = CalcRepository(db, settings, clock)
    val schedule: ScheduleRepository = ScheduleRepository(db, settings, calc, log, changes, clock)
    val shifts: ShiftRepository = ShiftRepository(db, calc, log, changes, clock)
    val absences: AbsenceRepository = AbsenceRepository(db, schedule, calc, log, changes, clock)
    val finance: FinanceRepository = FinanceRepository(db, calc, log, changes, clock)
    val backup: BackupRepository = BackupRepository(context, db, settings, calc, log, changes, clock)
    val notifications: NotificationScheduler = NotificationScheduler(context, db, calc, absences, clock)
    val wakeAlarms: WakeAlarmScheduler = WakeAlarmScheduler(context, db, settings, clock)
    val updater: AppUpdater = AppUpdater(context, settings)

    @OptIn(FlowPreview::class)
    fun start() {
        Notifications.createChannels(context)
        scope.launch {
            try {
                finance.ensureDefaultRules()
                schedule.ensureHorizon()
                notifications.reschedule()
                wakeAlarms.reschedule()
            } catch (e: Exception) {
                android.util.Log.e("WorkCalendar", "Startup tasks failed", e)
            }
        }
        scope.launch {
            changes.flow.debounce(700).collect {
                try {
                    notifications.reschedule()
                    wakeAlarms.reschedule()
                    WidgetUpdater.updateAll(context)
                } catch (e: Exception) {
                    android.util.Log.e("WorkCalendar", "Refresh after change failed", e)
                }
            }
        }
        MaintenanceWorker.schedule(context)
    }
}

class WorkCalendarApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.start()
    }
}

val Context.appContainer: AppContainer get() = (applicationContext as WorkCalendarApp).container
