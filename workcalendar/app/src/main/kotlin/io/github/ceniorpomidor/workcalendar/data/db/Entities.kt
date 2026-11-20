package io.github.ceniorpomidor.workcalendar.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.ceniorpomidor.workcalendar.domain.model.AbsencePayCalculation
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceType
import io.github.ceniorpomidor.workcalendar.domain.model.AccrualType
import io.github.ceniorpomidor.workcalendar.domain.model.ChangeCategory
import io.github.ceniorpomidor.workcalendar.domain.model.HolidayOverrideType
import io.github.ceniorpomidor.workcalendar.domain.model.PaymentType
import io.github.ceniorpomidor.workcalendar.domain.model.PayoutAmountMode
import io.github.ceniorpomidor.workcalendar.domain.model.PayoutKind
import io.github.ceniorpomidor.workcalendar.domain.model.SchedulePattern
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftKind
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftOrigin
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftPay
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.model.WeekendShift
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Concrete shifts in the calendar. [templateKey] is the date for shifts created from a schedule
 * and null for manual ones; the unique index makes repeated generation idempotent.
 */
@Entity(
    tableName = "shifts",
    indices = [
        Index(value = ["date"]),
        Index(value = ["templateKey"], unique = true),
        Index(value = ["status", "plannedEnd"]),
        Index(value = ["absenceId"]),
    ],
)
data class ShiftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val plannedStart: LocalDateTime,
    val plannedEnd: LocalDateTime,
    val plannedBreakMinutes: Int,
    val kind: ShiftKind,
    val origin: ShiftOrigin,
    val status: ShiftStatus,
    val userModified: Boolean,
    val templateKey: String?,
    val assignmentId: Long?,
    val title: String,
    val note: String,
    val actualStart: LocalDateTime?,
    val actualEnd: LocalDateTime?,
    val actualBreakMinutes: Int?,
    val workedMinutes: Int?,
    val hourlyRateOverride: Long?,
    val pay: ShiftPay?,
    /** Denormalized total of [pay] for fast sums. */
    val payTotal: Long?,
    val absenceId: Long?,
    val movedToDate: LocalDate?,
    val movedFromShiftId: Long?,
    val movedFromDate: LocalDate?,
    val confirmedAt: LocalDateTime?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
)

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val pattern: SchedulePattern,
    val colorIndex: Int,
    val archived: Boolean,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
)

@Entity(tableName = "assignments", indices = [Index(value = ["startDate"])])
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long?,
    val templateName: String,
    val pattern: SchedulePattern,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val createdAt: LocalDateTime?,
)

@Entity(tableName = "rates", indices = [Index(value = ["effectiveFrom"])])
data class RateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val effectiveFrom: LocalDate,
    val hourlyRate: Long,
    /** Unused: night surcharge was removed; the column is kept so the database schema stays the same. */
    val nightBonusPercent: Int = 0,
    val holidayBonusPercent: Int,
    val overtimeBonusPercent: Int,
    val extraShiftBonusPercent: Int,
    val note: String,
)

@Entity(tableName = "absences", indices = [Index(value = ["startDate"]), Index(value = ["endDate"])])
data class AbsenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: AbsenceType,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val paid: Boolean,
    val manualAmount: Long?,
    val calculation: AbsencePayCalculation?,
    val paymentDate: LocalDate?,
    val employerPaymentDate: LocalDate?,
    val note: String,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
)

@Entity(tableName = "payout_rules")
data class PayoutRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kind: PayoutKind,
    val enabled: Boolean,
    val payDay: Int,
    val payMonthOffset: Int,
    val periodStartDay: Int,
    val periodEndDay: Int,
    val amountMode: PayoutAmountMode,
    val fixedAmount: Long,
    val percent: Int,
    val formula: String,
    val weekendShift: WeekendShift,
    val notify: Boolean,
    val notifyDaysBefore: Int,
    /** Not used since income tax was removed from the app; kept so the database schema stays the same. */
    val applyTax: Boolean = false,
    val sortOrder: Int,
)

@Entity(
    tableName = "payments",
    indices = [Index(value = ["date"]), Index(value = ["payoutKey"], unique = true)],
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: PaymentType,
    val amount: Long,
    val date: LocalDate,
    val payoutKey: String?,
    val periodStart: LocalDate?,
    val periodEnd: LocalDate?,
    val expectedAmount: Long?,
    val absenceId: Long?,
    val note: String,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
)

@Entity(tableName = "accruals", indices = [Index(value = ["date"])])
data class AccrualEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val type: AccrualType,
    val title: String,
    val amount: Long,
    val note: String,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
)

@Entity(tableName = "external_earnings")
data class ExternalEarningEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val year: Int,
    val month: Int?,
    val amount: Long,
    val note: String,
)

@Entity(tableName = "day_notes")
data class DayNoteEntity(
    @PrimaryKey val date: LocalDate,
    val text: String,
    val updatedAt: LocalDateTime?,
)

@Entity(tableName = "holiday_overrides")
data class HolidayOverrideEntity(
    @PrimaryKey val date: LocalDate,
    val type: HolidayOverrideType,
    val title: String,
)

/** Single row with the app settings as JSON (new fields get defaults without migrations). */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val json: String,
)

@Entity(tableName = "change_log", indices = [Index(value = ["timestamp"])])
data class ChangeLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: LocalDateTime,
    val category: ChangeCategory,
    val action: String,
    val description: String,
    val entityId: Long?,
)

/** Scheduled and delivered notifications; prevents duplicates after reboots and re-planning. */
@Entity(tableName = "notification_state")
data class NotificationStateEntity(
    @PrimaryKey val key: String,
    val triggerAt: LocalDateTime?,
    val scheduled: Boolean,
    val deliveredAt: LocalDateTime?,
)
