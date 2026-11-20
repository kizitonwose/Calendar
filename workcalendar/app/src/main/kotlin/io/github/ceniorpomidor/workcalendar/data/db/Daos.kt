package io.github.ceniorpomidor.workcalendar.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shifts WHERE date BETWEEN :from AND :to ORDER BY date, plannedStart")
    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<ShiftEntity>>

    @Query("SELECT * FROM shifts WHERE date BETWEEN :from AND :to ORDER BY date, plannedStart")
    suspend fun getRange(from: LocalDate, to: LocalDate): List<ShiftEntity>

    @Query("SELECT * FROM shifts WHERE id = :id")
    suspend fun getById(id: Long): ShiftEntity?

    @Query("SELECT * FROM shifts WHERE id = :id")
    fun observeById(id: Long): Flow<ShiftEntity?>

    @Query("SELECT * FROM shifts WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<ShiftEntity>

    /** Planned shifts that have ended before [until] (hours not entered yet). */
    @Query("SELECT * FROM shifts WHERE status = 'PLANNED' AND plannedEnd <= :until ORDER BY plannedStart")
    fun observePlannedEndedBefore(until: LocalDateTime): Flow<List<ShiftEntity>>

    @Query("SELECT * FROM shifts WHERE status = 'PLANNED' AND plannedEnd <= :until ORDER BY plannedStart")
    suspend fun getPlannedEndedBefore(until: LocalDateTime): List<ShiftEntity>

    @Query("SELECT * FROM shifts WHERE status = 'PLANNED' AND plannedEnd BETWEEN :from AND :to ORDER BY plannedStart")
    suspend fun getPlannedEndingBetween(from: LocalDateTime, to: LocalDateTime): List<ShiftEntity>

    @Query("SELECT * FROM shifts WHERE status IN ('PLANNED', 'CONFIRMED') AND plannedEnd >= :from ORDER BY plannedStart LIMIT :limit")
    fun observeNext(from: LocalDateTime, limit: Int): Flow<List<ShiftEntity>>

    @Query("SELECT * FROM shifts WHERE absenceId = :absenceId")
    suspend fun getByAbsence(absenceId: Long): List<ShiftEntity>

    @Query("SELECT * FROM shifts WHERE (note LIKE :query OR title LIKE :query) AND status != 'DELETED' ORDER BY date DESC LIMIT 200")
    suspend fun search(query: String): List<ShiftEntity>

    @Query("SELECT * FROM shifts")
    suspend fun getAll(): List<ShiftEntity>

    @Query("SELECT MAX(date) FROM shifts WHERE origin = 'TEMPLATE'")
    suspend fun lastTemplateDate(): LocalDate?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(shifts: List<ShiftEntity>): List<Long>

    @Insert
    suspend fun insert(shift: ShiftEntity): Long

    @Insert
    suspend fun insertAll(shifts: List<ShiftEntity>)

    @Update
    suspend fun update(shift: ShiftEntity)

    @Update
    suspend fun updateAll(shifts: List<ShiftEntity>)

    @Delete
    suspend fun delete(shift: ShiftEntity)

    @Query("DELETE FROM shifts WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM shifts")
    suspend fun deleteAll()
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM templates WHERE archived = 0 ORDER BY name")
    fun observeTemplates(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates")
    suspend fun getTemplates(): List<TemplateEntity>

    @Query("SELECT * FROM templates WHERE id = :id")
    suspend fun getTemplate(id: Long): TemplateEntity?

    @Insert
    suspend fun insertTemplate(template: TemplateEntity): Long

    @Insert
    suspend fun insertTemplates(templates: List<TemplateEntity>)

    @Update
    suspend fun updateTemplate(template: TemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: TemplateEntity)

    @Query("SELECT * FROM assignments ORDER BY startDate")
    fun observeAssignments(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments ORDER BY startDate")
    suspend fun getAssignments(): List<AssignmentEntity>

    @Insert
    suspend fun insertAssignment(assignment: AssignmentEntity): Long

    @Insert
    suspend fun insertAssignments(assignments: List<AssignmentEntity>)

    @Update
    suspend fun updateAssignment(assignment: AssignmentEntity)

    @Query("DELETE FROM assignments WHERE id = :id")
    suspend fun deleteAssignment(id: Long)

    @Query("DELETE FROM templates")
    suspend fun deleteAllTemplates()

    @Query("DELETE FROM assignments")
    suspend fun deleteAllAssignments()
}

@Dao
interface RateDao {
    @Query("SELECT * FROM rates ORDER BY effectiveFrom")
    fun observeAll(): Flow<List<RateEntity>>

    @Query("SELECT * FROM rates ORDER BY effectiveFrom")
    suspend fun getAll(): List<RateEntity>

    @Insert
    suspend fun insert(rate: RateEntity): Long

    @Insert
    suspend fun insertAll(rates: List<RateEntity>)

    @Update
    suspend fun update(rate: RateEntity)

    @Delete
    suspend fun delete(rate: RateEntity)

    @Query("DELETE FROM rates")
    suspend fun deleteAll()
}

@Dao
interface AbsenceDao {
    @Query("SELECT * FROM absences ORDER BY startDate DESC")
    fun observeAll(): Flow<List<AbsenceEntity>>

    @Query("SELECT * FROM absences WHERE startDate <= :to AND endDate >= :from ORDER BY startDate")
    fun observeOverlapping(from: LocalDate, to: LocalDate): Flow<List<AbsenceEntity>>

    @Query("SELECT * FROM absences WHERE startDate <= :to AND endDate >= :from ORDER BY startDate")
    suspend fun getOverlapping(from: LocalDate, to: LocalDate): List<AbsenceEntity>

    @Query("SELECT * FROM absences ORDER BY startDate")
    suspend fun getAll(): List<AbsenceEntity>

    @Query("SELECT * FROM absences WHERE id = :id")
    suspend fun getById(id: Long): AbsenceEntity?

    @Query("SELECT * FROM absences WHERE id = :id")
    fun observeById(id: Long): Flow<AbsenceEntity?>

    @Query("SELECT * FROM absences WHERE note LIKE :query OR title LIKE :query ORDER BY startDate DESC LIMIT 100")
    suspend fun search(query: String): List<AbsenceEntity>

    @Insert
    suspend fun insert(absence: AbsenceEntity): Long

    @Insert
    suspend fun insertAll(absences: List<AbsenceEntity>)

    @Update
    suspend fun update(absence: AbsenceEntity)

    @Delete
    suspend fun delete(absence: AbsenceEntity)

    @Query("DELETE FROM absences")
    suspend fun deleteAll()
}

@Dao
interface FinanceDao {
    @Query("SELECT * FROM payout_rules ORDER BY sortOrder, id")
    fun observeRules(): Flow<List<PayoutRuleEntity>>

    @Query("SELECT * FROM payout_rules ORDER BY sortOrder, id")
    suspend fun getRules(): List<PayoutRuleEntity>

    @Query("SELECT * FROM payout_rules WHERE id = :id")
    suspend fun getRule(id: Long): PayoutRuleEntity?

    @Insert
    suspend fun insertRule(rule: PayoutRuleEntity): Long

    @Insert
    suspend fun insertRules(rules: List<PayoutRuleEntity>)

    @Update
    suspend fun updateRule(rule: PayoutRuleEntity)

    @Delete
    suspend fun deleteRule(rule: PayoutRuleEntity)

    @Query("SELECT * FROM payments ORDER BY date DESC, id DESC")
    fun observePayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments ORDER BY date")
    suspend fun getPayments(): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getPayment(id: Long): PaymentEntity?

    @Query("SELECT * FROM payments WHERE payoutKey = :key")
    suspend fun getPaymentByKey(key: String): PaymentEntity?

    @Query("SELECT * FROM payments WHERE note LIKE :query ORDER BY date DESC LIMIT 100")
    suspend fun searchPayments(query: String): List<PaymentEntity>

    @Insert
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Insert
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)

    @Query("SELECT * FROM accruals ORDER BY date DESC, id DESC")
    fun observeAccruals(): Flow<List<AccrualEntity>>

    @Query("SELECT * FROM accruals ORDER BY date")
    suspend fun getAccruals(): List<AccrualEntity>

    @Query("SELECT * FROM accruals WHERE id = :id")
    suspend fun getAccrual(id: Long): AccrualEntity?

    @Query("SELECT * FROM accruals WHERE title LIKE :query OR note LIKE :query ORDER BY date DESC LIMIT 100")
    suspend fun searchAccruals(query: String): List<AccrualEntity>

    @Insert
    suspend fun insertAccrual(accrual: AccrualEntity): Long

    @Insert
    suspend fun insertAccruals(accruals: List<AccrualEntity>)

    @Update
    suspend fun updateAccrual(accrual: AccrualEntity)

    @Delete
    suspend fun deleteAccrual(accrual: AccrualEntity)

    @Query("SELECT * FROM external_earnings ORDER BY year DESC, month DESC")
    fun observeExternal(): Flow<List<ExternalEarningEntity>>

    @Query("SELECT * FROM external_earnings")
    suspend fun getExternal(): List<ExternalEarningEntity>

    @Insert
    suspend fun insertExternal(item: ExternalEarningEntity): Long

    @Insert
    suspend fun insertExternalAll(items: List<ExternalEarningEntity>)

    @Update
    suspend fun updateExternal(item: ExternalEarningEntity)

    @Delete
    suspend fun deleteExternal(item: ExternalEarningEntity)

    @Query("DELETE FROM payout_rules")
    suspend fun deleteAllRules()

    @Query("DELETE FROM payments")
    suspend fun deleteAllPayments()

    @Query("DELETE FROM accruals")
    suspend fun deleteAllAccruals()

    @Query("DELETE FROM external_earnings")
    suspend fun deleteAllExternal()
}

@Dao
interface MiscDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun observeSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettings(): SettingsEntity?

    @Upsert
    suspend fun upsertSettings(settings: SettingsEntity)

    @Query("SELECT * FROM day_notes WHERE date BETWEEN :from AND :to")
    fun observeNotes(from: LocalDate, to: LocalDate): Flow<List<DayNoteEntity>>

    @Query("SELECT * FROM day_notes WHERE date = :date")
    fun observeNote(date: LocalDate): Flow<DayNoteEntity?>

    @Query("SELECT * FROM day_notes")
    suspend fun getNotes(): List<DayNoteEntity>

    @Query("SELECT * FROM day_notes WHERE text LIKE :query ORDER BY date DESC LIMIT 100")
    suspend fun searchNotes(query: String): List<DayNoteEntity>

    @Upsert
    suspend fun upsertNote(note: DayNoteEntity)

    @Insert
    suspend fun insertNotes(notes: List<DayNoteEntity>)

    @Query("DELETE FROM day_notes WHERE date = :date")
    suspend fun deleteNote(date: LocalDate)

    @Query("SELECT * FROM holiday_overrides ORDER BY date")
    fun observeHolidays(): Flow<List<HolidayOverrideEntity>>

    @Query("SELECT * FROM holiday_overrides ORDER BY date")
    suspend fun getHolidays(): List<HolidayOverrideEntity>

    @Upsert
    suspend fun upsertHoliday(holiday: HolidayOverrideEntity)

    @Insert
    suspend fun insertHolidays(holidays: List<HolidayOverrideEntity>)

    @Query("DELETE FROM holiday_overrides WHERE date = :date")
    suspend fun deleteHoliday(date: LocalDate)

    @Query("SELECT * FROM change_log ORDER BY timestamp DESC, id DESC LIMIT :limit")
    fun observeChangeLog(limit: Int): Flow<List<ChangeLogEntity>>

    @Query("SELECT * FROM change_log ORDER BY timestamp")
    suspend fun getChangeLog(): List<ChangeLogEntity>

    @Insert
    suspend fun insertChangeLog(entry: ChangeLogEntity)

    @Insert
    suspend fun insertChangeLogAll(entries: List<ChangeLogEntity>)

    @Query("SELECT * FROM notification_state WHERE `key` = :key")
    suspend fun getNotificationState(key: String): NotificationStateEntity?

    @Query("SELECT * FROM notification_state")
    suspend fun getNotificationStates(): List<NotificationStateEntity>

    @Upsert
    suspend fun upsertNotificationState(state: NotificationStateEntity)

    @Query("DELETE FROM notification_state WHERE `key` = :key")
    suspend fun deleteNotificationState(key: String)

    @Query("DELETE FROM notification_state WHERE deliveredAt IS NOT NULL AND deliveredAt < :before")
    suspend fun deleteDeliveredBefore(before: LocalDateTime)

    @Query("DELETE FROM day_notes")
    suspend fun deleteAllNotes()

    @Query("DELETE FROM holiday_overrides")
    suspend fun deleteAllHolidays()

    @Query("DELETE FROM change_log")
    suspend fun deleteAllChangeLog()

    @Query("DELETE FROM notification_state")
    suspend fun deleteAllNotificationStates()
}
