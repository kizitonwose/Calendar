package io.github.ceniorpomidor.workcalendar.domain.schedule

import io.github.ceniorpomidor.workcalendar.domain.model.ScheduleAssignment
import java.time.LocalDate

/** Changes of the assignment timeline caused by applying a template for a period. */
data class TimelineChange(
    /** Existing assignments whose period was shortened (same id). */
    val updated: List<ScheduleAssignment>,
    /** New assignments: the applied one and remainders of split assignments (id = 0). */
    val created: List<ScheduleAssignment>,
    /** Assignments fully replaced by the new one. */
    val removed: List<ScheduleAssignment>,
) {
    /** Resulting timeline (new ids are still 0). */
    fun applyTo(existing: List<ScheduleAssignment>): List<ScheduleAssignment> {
        val removedIds = removed.map { it.id }.toSet()
        val updatedById = updated.associateBy { it.id }
        return (existing.filter { it.id !in removedIds }.map { updatedById[it.id] ?: it } + created).sortedBy { it.startDate }
    }
}

/**
 * Non-overlapping sequence of applied schedules. Applying a template for a period trims or
 * splits the assignments it overlaps, so each date is covered by at most one assignment.
 */
object AssignmentTimeline {
    fun insert(existing: List<ScheduleAssignment>, new: ScheduleAssignment): TimelineChange {
        val updated = ArrayList<ScheduleAssignment>()
        val created = ArrayList<ScheduleAssignment>()
        val removed = ArrayList<ScheduleAssignment>()
        val newStart = new.startDate
        val newEnd = new.endDate
        for (a in existing) {
            if (!overlaps(a.startDate, a.endDate, newStart, newEnd)) continue
            val before = if (a.startDate < newStart) a.copy(endDate = newStart.minusDays(1)) else null
            val after = if (newEnd != null && (a.endDate == null || a.endDate > newEnd)) a.copy(startDate = newEnd.plusDays(1)) else null
            when {
                before != null && after != null -> {
                    updated += before
                    created += after.copy(id = 0)
                }
                before != null -> updated += before
                after != null -> updated += after
                else -> removed += a
            }
        }
        created += new
        return TimelineChange(updated, created, removed)
    }

    /** Assignment covering [date], if any. */
    fun find(assignments: List<ScheduleAssignment>, date: LocalDate): ScheduleAssignment? = assignments.firstOrNull { it.covers(date) }

    /** Removes an assignment from the timeline; the gap stays empty (no shifts). */
    fun remove(existing: List<ScheduleAssignment>, id: Long): List<ScheduleAssignment> = existing.filter { it.id != id }

    private fun overlaps(aStart: LocalDate, aEnd: LocalDate?, bStart: LocalDate, bEnd: LocalDate?): Boolean {
        val aEndsBeforeB = aEnd != null && aEnd < bStart
        val bEndsBeforeA = bEnd != null && bEnd < aStart
        return !aEndsBeforeB && !bEndsBeforeA
    }
}
