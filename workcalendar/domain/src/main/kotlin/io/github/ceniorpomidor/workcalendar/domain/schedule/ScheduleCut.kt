package io.github.ceniorpomidor.workcalendar.domain.schedule

import io.github.ceniorpomidor.workcalendar.domain.model.ScheduleAssignment
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftKind
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftOrigin
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import java.time.LocalDate

/** What happens when the schedule stops from a day, so that a new one can be set. */
data class ScheduleCut(
    /** Applied schedules that end the day before. */
    val ended: List<ScheduleAssignment>,
    /** Applied schedules that start on the day or later. */
    val removed: List<ScheduleAssignment>,
    /** Shifts deleted from the calendar. */
    val deleted: List<Shift>,
) {
    /** Planned shifts among [deleted], for the text of the confirmation. */
    val plannedCount: Int get() = deleted.count { it.status == ShiftStatus.PLANNED }

    companion object {
        /**
         * Stops the schedule from [date]: shifts of the schedule from this day on are deleted, as are
         * extra and manual shifts when [includeManual]. Shifts with entered hours (confirmed or missed)
         * always stay.
         */
        fun plan(assignments: List<ScheduleAssignment>, shifts: List<Shift>, date: LocalDate, includeManual: Boolean): ScheduleCut {
            val removed = assignments.filter { !it.startDate.isBefore(date) }
            val ended = assignments
                .filter { it.startDate.isBefore(date) && (it.endDate == null || !it.endDate.isBefore(date)) }
                .map { it.copy(endDate = date.minusDays(1)) }
            val deleted = shifts.filter { shift ->
                !shift.date.isBefore(date) && !shift.isClosed && (shift.origin == ShiftOrigin.TEMPLATE || includeManual)
            }
            return ScheduleCut(ended, removed, deleted)
        }

        /** Extra and manual planned shifts from [date] that are kept unless the user asks to delete them too. */
        fun manualCount(shifts: List<Shift>, date: LocalDate): Int = shifts.count {
            !it.date.isBefore(date) && it.status == ShiftStatus.PLANNED && (it.origin == ShiftOrigin.MANUAL || it.kind == ShiftKind.EXTRA)
        }
    }
}
