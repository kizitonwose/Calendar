package io.github.ceniorpomidor.workcalendar.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ShiftEntity::class,
        TemplateEntity::class,
        AssignmentEntity::class,
        RateEntity::class,
        AbsenceEntity::class,
        PayoutRuleEntity::class,
        PaymentEntity::class,
        AccrualEntity::class,
        ExternalEarningEntity::class,
        DayNoteEntity::class,
        HolidayOverrideEntity::class,
        SettingsEntity::class,
        ChangeLogEntity::class,
        NotificationStateEntity::class,
    ],
    version = AppDatabase.VERSION,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shiftDao(): ShiftDao

    abstract fun scheduleDao(): ScheduleDao

    abstract fun rateDao(): RateDao

    abstract fun absenceDao(): AbsenceDao

    abstract fun financeDao(): FinanceDao

    abstract fun miscDao(): MiscDao

    companion object {
        const val VERSION: Int = 1
        const val NAME: String = "workcalendar.db"

        fun build(context: Context): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, NAME)
            // Future schema changes must add migrations here; data is never dropped silently.
            .build()
    }
}
