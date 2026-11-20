package io.github.ceniorpomidor.workcalendar.domain.schedule

import io.github.ceniorpomidor.workcalendar.domain.model.Absence
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange

/** Effect of saving an absence on the shifts of its dates. */
data class CoveragePlan(
    /** Planned shifts that become "covered" (kept for history, excluded from accounting). */
    val toCover: List<Shift>,
    /** Shifts that were covered by this absence but are outside its new period. */
    val toRestore: List<Shift>,
    /** Confirmed or missed shifts inside the period; they are not changed but the user is warned. */
    val conflicts: List<Shift>,
) {
    val isEmpty: Boolean get() = toCover.isEmpty() && toRestore.isEmpty() && conflicts.isEmpty()
}

object AbsenceCoverage {
    /**
     * [shifts] must contain all shifts of the new absence period and all shifts currently
     * covered by this absence (for an edited absence).
     */
    fun plan(absence: Absence, shifts: List<Shift>): CoveragePlan {
        val range = absence.range
        val toCover = shifts.filter { it.date in range && it.status == ShiftStatus.PLANNED }
            .map { it.copy(status = ShiftStatus.COVERED, absenceId = absence.id) }
        val toRestore = shifts.filter { it.status == ShiftStatus.COVERED && it.absenceId == absence.id && it.date !in range && absence.id != 0L }
            .map { it.copy(status = ShiftStatus.PLANNED, absenceId = null) }
        val conflicts = shifts.filter { it.date in range && it.isClosed }
        return CoveragePlan(toCover, toRestore, conflicts)
    }

    /** Shifts to return to the schedule when an absence is deleted. */
    fun release(absenceId: Long, shifts: List<Shift>): List<Shift> =
        shifts.filter { it.status == ShiftStatus.COVERED && it.absenceId == absenceId }
            .map { it.copy(status = ShiftStatus.PLANNED, absenceId = null) }

    /** Checks that the new absence does not overlap other absences. */
    fun findOverlap(absence: Absence, others: List<Absence>): Absence? =
        others.firstOrNull { it.id != absence.id && it.range.overlaps(absence.range) }

    fun overlapsRange(absence: Absence, range: DateRange): Boolean = absence.range.overlaps(range)
}
