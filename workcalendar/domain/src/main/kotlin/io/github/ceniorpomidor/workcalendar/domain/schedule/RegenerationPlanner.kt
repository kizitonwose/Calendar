package io.github.ceniorpomidor.workcalendar.domain.schedule

import io.github.ceniorpomidor.workcalendar.domain.model.Absence
import io.github.ceniorpomidor.workcalendar.domain.model.ScheduleAssignment
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftKind
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftOrigin
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.time.HolidayCalendar
import java.time.LocalDate

enum class ChangeType {
    /** A new shift will be created. */
    ADD,

    /** An automatically created shift will get new times. */
    UPDATE,

    /** An automatically created shift will be removed (day off in the new schedule). */
    REMOVE,

    /** A day changed by the user is kept as is. */
    KEEP_USER_CHANGE,

    /** A confirmed, missed or moved shift is never changed automatically. */
    KEEP_CLOSED,

    /** A day changed by the user will be reset to the schedule (only when overwriting was chosen). */
    RESET_USER_CHANGE,

    /** A day changed by the user will be removed (only when overwriting was chosen). */
    REMOVE_USER_CHANGE,
}

data class PlannedChange(
    val date: LocalDate,
    val type: ChangeType,
    val before: Shift?,
    val after: Shift?,
)

data class RegenerationPlan(
    val range: DateRange?,
    val changes: List<PlannedChange>,
) {
    val inserts: List<Shift> get() = changes.filter { it.type == ChangeType.ADD }.mapNotNull { it.after }

    val updates: List<Shift>
        get() = changes.filter { it.type == ChangeType.UPDATE || it.type == ChangeType.RESET_USER_CHANGE }.mapNotNull { it.after }

    val deletes: List<Shift>
        get() = changes.filter { it.type == ChangeType.REMOVE || it.type == ChangeType.REMOVE_USER_CHANGE }.mapNotNull { it.before }

    val kept: List<PlannedChange> get() = changes.filter { it.type == ChangeType.KEEP_USER_CHANGE || it.type == ChangeType.KEEP_CLOSED }

    val isEmpty: Boolean get() = inserts.isEmpty() && updates.isEmpty() && deletes.isEmpty()

    /** Existing shifts will be changed or removed: the user must confirm. */
    val touchesExistingShifts: Boolean get() = updates.isNotEmpty() || deletes.isNotEmpty()

    fun count(type: ChangeType): Int = changes.count { it.type == type }

    companion object {
        val EMPTY: RegenerationPlan = RegenerationPlan(null, emptyList())
    }
}

/**
 * Computes how template-generated shifts must change so that the calendar matches the
 * assignment timeline, while preserving everything the user did by hand.
 *
 * Rules:
 * - one template shift per date at most (its date is the duplicate-protection key);
 * - untouched template shifts follow the schedule (added, retimed or removed);
 * - shifts edited, cancelled, deleted or moved by the user are kept unless [overwriteUserChanges];
 * - confirmed and missed shifts are never changed automatically;
 * - a manual regular shift on a date replaces the template shift of that date;
 * - shifts falling into an absence are created as "covered" and excluded from accounting.
 */
object RegenerationPlanner {
    fun plan(
        range: DateRange,
        assignments: List<ScheduleAssignment>,
        existing: List<Shift>,
        absences: List<Absence>,
        holidays: HolidayCalendar,
        overwriteUserChanges: Boolean = false,
    ): RegenerationPlan {
        val generated = ScheduleGenerator.generate(assignments, range, holidays).associateBy { it.date }
        val byDate = existing.filter { it.date in range }.groupBy { it.date }
        val changes = ArrayList<PlannedChange>()
        for (date in range) {
            val desired = generated[date]
            val shifts = byDate[date].orEmpty()
            val template = shifts.firstOrNull { it.origin == ShiftOrigin.TEMPLATE }
            val blockedByManual = shifts.any { it.origin == ShiftOrigin.MANUAL && it.kind == ShiftKind.REGULAR && it.status != ShiftStatus.DELETED }
            val absence = absences.firstOrNull { date in it.range }

            if (template == null) {
                if (desired != null && !blockedByManual) {
                    changes += PlannedChange(date, ChangeType.ADD, null, newShift(desired, absence))
                }
                continue
            }

            if (template.isPristineTemplate) {
                if (desired == null || blockedByManual) {
                    changes += PlannedChange(date, ChangeType.REMOVE, template, null)
                } else {
                    val updated = applyPlan(template, desired, absence)
                    // A different assignment id alone is not a visible change.
                    if (updated.copy(assignmentId = template.assignmentId) != template) {
                        changes += PlannedChange(date, ChangeType.UPDATE, template, updated)
                    }
                }
                continue
            }

            // Shift touched by the user.
            when {
                template.isClosed || template.status == ShiftStatus.MOVED -> {
                    changes += PlannedChange(date, ChangeType.KEEP_CLOSED, template, template)
                }
                template.status == ShiftStatus.DELETED && desired == null -> Unit // tombstone of a day off: nothing to show
                overwriteUserChanges -> {
                    if (desired == null || blockedByManual) {
                        changes += PlannedChange(date, ChangeType.REMOVE_USER_CHANGE, template, null)
                    } else {
                        val reset = applyPlan(template, desired, absence).copy(
                            userModified = false,
                            hourlyRateOverride = null,
                        )
                        changes += PlannedChange(date, ChangeType.RESET_USER_CHANGE, template, reset)
                    }
                }
                else -> changes += PlannedChange(date, ChangeType.KEEP_USER_CHANGE, template, template)
            }
        }
        return RegenerationPlan(range, changes)
    }

    /** Only the missing shifts (used by the background horizon extension). */
    fun planMissing(
        range: DateRange,
        assignments: List<ScheduleAssignment>,
        existing: List<Shift>,
        absences: List<Absence>,
        holidays: HolidayCalendar,
    ): RegenerationPlan {
        val full = plan(range, assignments, existing, absences, holidays)
        return RegenerationPlan(range, full.changes.filter { it.type == ChangeType.ADD })
    }

    private fun newShift(g: GeneratedShift, absence: Absence?): Shift = Shift(
        date = g.date,
        plannedStart = g.start,
        plannedEnd = g.end,
        plannedBreakMinutes = g.breakMinutes,
        kind = ShiftKind.REGULAR,
        origin = ShiftOrigin.TEMPLATE,
        status = if (absence != null) ShiftStatus.COVERED else ShiftStatus.PLANNED,
        assignmentId = g.assignmentId,
        title = g.title,
        absenceId = absence?.id,
    )

    private fun applyPlan(shift: Shift, g: GeneratedShift, absence: Absence?): Shift = shift.copy(
        plannedStart = g.start,
        plannedEnd = g.end,
        plannedBreakMinutes = g.breakMinutes,
        title = g.title,
        assignmentId = g.assignmentId,
        status = if (absence != null) ShiftStatus.COVERED else ShiftStatus.PLANNED,
        absenceId = absence?.id,
    )
}
