package io.github.ceniorpomidor.workcalendar.domain

import io.github.ceniorpomidor.workcalendar.domain.TestData.assignment
import io.github.ceniorpomidor.workcalendar.domain.TestData.date
import io.github.ceniorpomidor.workcalendar.domain.TestData.shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftKind
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftOrigin
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.schedule.ScheduleCut
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ScheduleCutTest {
    private val pattern = TestData.fiveTwo()

    @Test
    fun `schedule stops from a day and keeps shifts with entered hours`() {
        val assignments = listOf(
            assignment(1, pattern, "2026-01-01", "2026-06-30"),
            assignment(2, pattern, "2026-07-01"),
            assignment(3, pattern, "2026-11-01"),
        )
        val shifts = listOf(
            shift(1, "2026-10-09"),
            shift(2, "2026-10-12", status = ShiftStatus.CONFIRMED, worked = 480),
            shift(3, "2026-10-13"),
            shift(4, "2026-10-14", status = ShiftStatus.CANCELLED),
            shift(5, "2026-10-17", origin = ShiftOrigin.MANUAL, kind = ShiftKind.EXTRA),
            shift(6, "2026-10-19", status = ShiftStatus.MISSED),
        )
        val cut = ScheduleCut.plan(assignments, shifts, date("2026-10-12"), includeManual = false)
        assertEquals(listOf(2L), cut.ended.map { it.id })
        assertEquals(date("2026-10-11"), cut.ended.single().endDate)
        assertEquals(listOf(3L), cut.removed.map { it.id })
        assertEquals(listOf(3L, 4L), cut.deleted.map { it.id })
        assertEquals(1, cut.plannedCount)
        assertEquals(1, ScheduleCut.manualCount(shifts, date("2026-10-12")))
        assertEquals(listOf(3L, 4L, 5L), ScheduleCut.plan(assignments, shifts, date("2026-10-12"), includeManual = true).deleted.map { it.id })
    }
}
