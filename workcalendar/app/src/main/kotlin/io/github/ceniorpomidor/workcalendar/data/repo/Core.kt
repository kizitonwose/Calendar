package io.github.ceniorpomidor.workcalendar.data.repo

import io.github.ceniorpomidor.workcalendar.data.db.AppDatabase
import io.github.ceniorpomidor.workcalendar.data.db.ChangeLogEntity
import io.github.ceniorpomidor.workcalendar.data.db.Converters
import io.github.ceniorpomidor.workcalendar.data.db.SettingsEntity
import io.github.ceniorpomidor.workcalendar.data.db.toDomain
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.domain.model.ChangeCategory
import io.github.ceniorpomidor.workcalendar.domain.model.RateTable
import io.github.ceniorpomidor.workcalendar.domain.pay.PayCalculator
import io.github.ceniorpomidor.workcalendar.domain.pay.PayoutCalculator
import io.github.ceniorpomidor.workcalendar.domain.pay.SummaryCalculator
import io.github.ceniorpomidor.workcalendar.domain.time.HolidayCalendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Source of the current time; all "now" values in the app come from here. */
interface AppClock {
    fun zone(): ZoneId

    fun now(): LocalDateTime

    fun today(): LocalDate = now().toLocalDate()
}

object SystemAppClock : AppClock {
    override fun zone(): ZoneId = ZoneId.systemDefault()

    override fun now(): LocalDateTime = LocalDateTime.now(zone()).truncatedTo(ChronoUnit.SECONDS)
}

/** Everything needed for pay calculations, consistent across all screens. */
class CalcContext(
    val settings: AppSettings,
    val rates: RateTable,
    val holidays: HolidayCalendar,
    val zone: ZoneId,
) {
    val pay: PayCalculator = PayCalculator(rates, holidays, zone)
    val summaries: SummaryCalculator = SummaryCalculator(pay)
    val payouts: PayoutCalculator = PayoutCalculator(pay, summaries)
}

/** Emits after every write so that notifications and widgets can be refreshed. */
class DataChanges {
    private val events = MutableSharedFlow<Unit>(extraBufferCapacity = 16)
    val flow: SharedFlow<Unit> get() = events

    fun notifyChanged() {
        events.tryEmit(Unit)
    }
}

class SettingsRepository(private val db: AppDatabase, private val changes: DataChanges) {
    private val mutex = Mutex()

    val settings: Flow<AppSettings> = db.miscDao().observeSettings().map { decode(it) }.distinctUntilChanged()

    suspend fun get(): AppSettings = decode(db.miscDao().getSettings())

    suspend fun update(transform: (AppSettings) -> AppSettings): AppSettings = mutex.withLock {
        val updated = transform(get())
        db.miscDao().upsertSettings(SettingsEntity(json = Converters.json.encodeToString(AppSettings.serializer(), updated)))
        changes.notifyChanged()
        updated
    }

    suspend fun replace(settings: AppSettings) = update { settings }

    private fun decode(entity: SettingsEntity?): AppSettings {
        if (entity == null) return AppSettings()
        return try {
            val settings = Converters.json.decodeFromString(AppSettings.serializer(), entity.json)
            // The single alarm of version 1.1.0 becomes an alarm of the list.
            settings.copy(alarm = settings.alarm.migrated())
        } catch (e: Exception) {
            AppSettings()
        }
    }
}

/** Builds [CalcContext] from settings, rates and holidays. */
class CalcRepository(private val db: AppDatabase, private val settings: SettingsRepository, private val clock: AppClock) {
    val context: Flow<CalcContext> = combine(
        settings.settings,
        db.rateDao().observeAll(),
        db.miscDao().observeHolidays(),
    ) { s, rates, holidays ->
        CalcContext(s, RateTable(rates.map { it.toDomain() }), HolidayCalendar(holidays.map { it.toDomain() }, s.russianHolidays), clock.zone())
    }

    suspend fun current(): CalcContext {
        val s = settings.get()
        return CalcContext(
            s,
            RateTable(db.rateDao().getAll().map { it.toDomain() }),
            HolidayCalendar(db.miscDao().getHolidays().map { it.toDomain() }, s.russianHolidays),
            clock.zone(),
        )
    }
}

/** Change history used to explain amounts and edits later. */
class ChangeLogger(private val db: AppDatabase, private val clock: AppClock) {
    suspend fun log(category: ChangeCategory, action: String, description: String, entityId: Long? = null) {
        db.miscDao().insertChangeLog(ChangeLogEntity(timestamp = clock.now(), category = category, action = action, description = description, entityId = entityId))
    }
}
