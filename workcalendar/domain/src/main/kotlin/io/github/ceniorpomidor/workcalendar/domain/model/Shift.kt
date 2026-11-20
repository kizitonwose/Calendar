@file:UseSerializers(LocalDateSerializer::class, LocalDateTimeSerializer::class)

package io.github.ceniorpomidor.workcalendar.domain.model

import io.github.ceniorpomidor.workcalendar.domain.time.TimeMath
import io.github.ceniorpomidor.workcalendar.domain.util.LocalDateSerializer
import io.github.ceniorpomidor.workcalendar.domain.util.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** Stored lifecycle state of a shift. */
@Serializable
enum class ShiftStatus {
    /** Scheduled; becomes "awaiting confirmation" once the planned end has passed. */
    PLANNED,

    /** Worked hours were entered by the user. */
    CONFIRMED,

    /** The shift did not take place (confirmed with zero hours). */
    MISSED,

    /** Cancelled in advance by the user. */
    CANCELLED,

    /** Moved to another date; kept on the original date as a marker. */
    MOVED,

    /** Overlapped by a vacation / sick leave / other absence; kept for history only. */
    COVERED,

    /** Deleted by the user. Template shifts are kept as tombstones so they are not re-generated. */
    DELETED,
}

@Serializable
enum class ShiftKind {
    /** Part of the regular schedule. */
    REGULAR,

    /** Additional shift on top of the schedule (e.g. on a day off). */
    EXTRA,
}

@Serializable
enum class ShiftOrigin {
    /** Created automatically from a schedule template. */
    TEMPLATE,

    /** Created manually by the user. */
    MANUAL,
}

/** State used for display (colors, icons, texts). Derived from the stored status and the current time. */
enum class ShiftDisplayState {
    UPCOMING,
    IN_PROGRESS,
    AWAITING_CONFIRMATION,
    CONFIRMED,
    MISSED,
    CANCELLED,
    MOVED,
    COVERED,
    DELETED,
}

/**
 * Snapshot of the pay calculation made when the shift was confirmed. Later changes of
 * rates or rules don't change it unless the user explicitly recalculates.
 */
@Serializable
data class ShiftPay(
    val hourlyRate: Money,
    val paidMinutes: Int,
    val holidayMinutes: Int = 0,
    val overtimeMinutes: Int = 0,
    val base: Money,
    val holidayBonus: Money = Money.ZERO,
    val overtimeBonus: Money = Money.ZERO,
    val extraShiftBonus: Money = Money.ZERO,
    val holidayPercent: Int = 0,
    val overtimePercent: Int = 0,
    val extraShiftPercent: Int = 0,
) {
    val total: Money get() = base + holidayBonus + overtimeBonus + extraShiftBonus

    val bonuses: Money get() = holidayBonus + overtimeBonus + extraShiftBonus
}

@Serializable
data class Shift(
    val id: Long = 0,
    /** Date the shift belongs to (the day it starts). */
    val date: LocalDate,
    val plannedStart: LocalDateTime,
    val plannedEnd: LocalDateTime,
    /** Unpaid break inside the planned shift, minutes. */
    val plannedBreakMinutes: Int = 0,
    val kind: ShiftKind = ShiftKind.REGULAR,
    val origin: ShiftOrigin = ShiftOrigin.MANUAL,
    val status: ShiftStatus = ShiftStatus.PLANNED,
    /** A template shift edited by the user; protected from automatic regeneration. */
    val userModified: Boolean = false,
    val assignmentId: Long? = null,
    val title: String = "",
    val note: String = "",
    val actualStart: LocalDateTime? = null,
    val actualEnd: LocalDateTime? = null,
    val actualBreakMinutes: Int? = null,
    /** Confirmed paid minutes. */
    val workedMinutes: Int? = null,
    /** Individual hourly rate for this shift. */
    val hourlyRateOverride: Money? = null,
    val pay: ShiftPay? = null,
    val absenceId: Long? = null,
    val movedToDate: LocalDate? = null,
    val movedFromShiftId: Long? = null,
    val movedFromDate: LocalDate? = null,
    val confirmedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
) {
    /** Shift takes part in the schedule (counts in planned hours). */
    val isActive: Boolean get() = status == ShiftStatus.PLANNED || status == ShiftStatus.CONFIRMED || status == ShiftStatus.MISSED

    val isConfirmed: Boolean get() = status == ShiftStatus.CONFIRMED

    /** Shift is completed in terms of accounting: confirmed or marked as missed. */
    val isClosed: Boolean get() = status == ShiftStatus.CONFIRMED || status == ShiftStatus.MISSED

    val isVisible: Boolean get() = status != ShiftStatus.DELETED

    /** Can be changed by schedule regeneration without losing user input. */
    val isPristineTemplate: Boolean
        get() = origin == ShiftOrigin.TEMPLATE && !userModified && (status == ShiftStatus.PLANNED || status == ShiftStatus.COVERED)

    fun plannedGrossMinutes(zone: ZoneId): Long = TimeMath.elapsedMinutes(plannedStart, plannedEnd, zone)

    /** Planned paid minutes (duration minus unpaid break). */
    fun plannedPaidMinutes(zone: ZoneId): Long = (plannedGrossMinutes(zone) - plannedBreakMinutes).coerceAtLeast(0)

    fun displayState(now: LocalDateTime): ShiftDisplayState = when (status) {
        ShiftStatus.PLANNED -> when {
            !now.isBefore(plannedEnd) -> ShiftDisplayState.AWAITING_CONFIRMATION
            !now.isBefore(plannedStart) -> ShiftDisplayState.IN_PROGRESS
            else -> ShiftDisplayState.UPCOMING
        }
        ShiftStatus.CONFIRMED -> ShiftDisplayState.CONFIRMED
        ShiftStatus.MISSED -> ShiftDisplayState.MISSED
        ShiftStatus.CANCELLED -> ShiftDisplayState.CANCELLED
        ShiftStatus.MOVED -> ShiftDisplayState.MOVED
        ShiftStatus.COVERED -> ShiftDisplayState.COVERED
        ShiftStatus.DELETED -> ShiftDisplayState.DELETED
    }

    /** Unique key used to avoid duplicates of template shifts: one template shift per date. */
    val templateKey: String? get() = if (origin == ShiftOrigin.TEMPLATE) date.toString() else null
}

/** Result of confirming a shift. */
enum class CompletionKind {
    FULL,
    PARTIAL,
    OVERTIME,
    NONE,
}

fun Shift.completionKind(zone: ZoneId): CompletionKind? {
    if (status == ShiftStatus.MISSED) return CompletionKind.NONE
    val worked = workedMinutes ?: return null
    if (status != ShiftStatus.CONFIRMED) return null
    val planned = plannedPaidMinutes(zone)
    return when {
        worked <= 0 -> CompletionKind.NONE
        worked < planned -> CompletionKind.PARTIAL
        worked > planned -> CompletionKind.OVERTIME
        else -> CompletionKind.FULL
    }
}
