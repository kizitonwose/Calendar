package io.github.ceniorpomidor.workcalendar.domain

import io.github.ceniorpomidor.workcalendar.domain.TestData.assignment
import io.github.ceniorpomidor.workcalendar.domain.TestData.date
import io.github.ceniorpomidor.workcalendar.domain.TestData.shift
import io.github.ceniorpomidor.workcalendar.domain.model.Absence
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceType
import io.github.ceniorpomidor.workcalendar.domain.model.SchedulePattern
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftKind
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftOrigin
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftSpec
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.schedule.AbsenceCoverage
import io.github.ceniorpomidor.workcalendar.domain.schedule.AssignmentTimeline
import io.github.ceniorpomidor.workcalendar.domain.schedule.ChangeType
import io.github.ceniorpomidor.workcalendar.domain.schedule.RegenerationPlanner
import io.github.ceniorpomidor.workcalendar.domain.schedule.ScheduleGenerator
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.time.HolidayCalendar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScheduleTest {
    private val holidays = HolidayCalendar()

    @Test
    fun `weekly pattern generates weekdays only`() {
        val shifts = ScheduleGenerator.generate(TestData.fiveTwo(), DateRange(date("2026-10-05"), date("2026-10-11")), holidays)
        assertEquals(5, shifts.size)
        assertEquals(date("2026-10-05").atTime(9, 0), shifts.first().start)
        assertEquals(date("2026-10-05").atTime(18, 0), shifts.first().end)
    }

    @Test
    fun `weekly pattern can skip public holidays`() {
        val range = DateRange(date("2026-11-02"), date("2026-11-06"))
        assertEquals(5, ScheduleGenerator.generate(TestData.fiveTwo(), range, holidays).size)
        assertEquals(4, ScheduleGenerator.generate(TestData.fiveTwo(skipHolidays = true), range, holidays).size)
    }

    @Test
    fun `cycle pattern keeps its phase from the anchor date`() {
        val pattern = TestData.twoTwo(date("2026-10-01"))
        val dates = ScheduleGenerator.generate(pattern, DateRange(date("2026-09-27"), date("2026-10-08")), holidays).map { it.date.dayOfMonth }
        // Cycle W W O O anchored at 01.10: 27.09 and 28.09 are work days, 29.09 and 30.09 are days off.
        assertEquals(listOf(27, 28, 1, 2, 5, 6), dates)
    }

    @Test
    fun `day-night cycle produces overnight shift`() {
        val pattern = SchedulePattern.Cycle(listOf(ShiftSpec(20 * 60, 12 * 60, 60), null), date("2026-10-01"))
        val shift = ScheduleGenerator.generate(pattern, DateRange.single(date("2026-10-01")), holidays).single()
        assertEquals(date("2026-10-02").atTime(8, 0), shift.end)
    }

    @Test
    fun `applying a template splits the existing timeline`() {
        val base = assignment(1, TestData.fiveTwo(), "2026-01-01")
        val vacationTemplate = assignment(0, TestData.twoTwo(date("2026-11-01")), "2026-11-01", "2026-11-30")
        val change = AssignmentTimeline.insert(listOf(base), vacationTemplate)
        assertEquals(date("2026-10-31"), change.updated.single().endDate)
        val remainder = change.created.first { it.templateName == "T1" }
        assertEquals(date("2026-12-01"), remainder.startDate)
        assertNull(remainder.endDate)
        val timeline = change.applyTo(listOf(base))
        assertEquals(3, timeline.size)
        assertEquals("T0", AssignmentTimeline.find(timeline, date("2026-11-15"))!!.templateName)
    }

    @Test
    fun `scenario 1 - setting a schedule fills the calendar`() {
        val timeline = listOf(assignment(1, TestData.fiveTwo(), "2026-10-01"))
        val plan = RegenerationPlanner.plan(DateRange.month(java.time.YearMonth.of(2026, 10)), timeline, emptyList(), emptyList(), holidays)
        assertEquals(22, plan.inserts.size)
        assertTrue(plan.inserts.all { it.origin == ShiftOrigin.TEMPLATE && it.status == ShiftStatus.PLANNED })
        // Running again over the generated shifts changes nothing (no duplicates).
        val existing = plan.inserts.mapIndexed { i, s -> s.copy(id = i + 1L) }
        val again = RegenerationPlanner.plan(DateRange.month(java.time.YearMonth.of(2026, 10)), timeline, existing, emptyList(), holidays)
        assertTrue(again.isEmpty)
    }

    @Test
    fun `scenario 2 - schedule change keeps confirmed and edited days`() {
        val old = listOf(assignment(1, TestData.fiveTwo(), "2026-10-01"))
        val existing = listOf(
            shift(1, "2026-11-02", status = ShiftStatus.CONFIRMED, worked = 480),
            shift(2, "2026-11-03", start = "10:00", end = "19:00", userModified = true),
            shift(3, "2026-11-05"),
            shift(4, "2026-11-06"),
        )
        val newAssignment = assignment(0, TestData.twoTwo(date("2026-11-01")), "2026-11-01")
        val timeline = AssignmentTimeline.insert(old, newAssignment).applyTo(old)
        val plan = RegenerationPlanner.plan(DateRange(date("2026-11-01"), date("2026-11-08")), timeline, existing, emptyList(), holidays)
        // 2/2 from 01.11: work 1,2,5,6 ; off 3,4,7,8
        assertEquals(ChangeType.KEEP_CLOSED, plan.changes.single { it.date == date("2026-11-02") }.type)
        assertEquals(ChangeType.KEEP_USER_CHANGE, plan.changes.single { it.date == date("2026-11-03") }.type)
        assertEquals(listOf(date("2026-11-01")), plan.inserts.map { it.date })
        assertTrue(plan.deletes.isEmpty())
        assertTrue(plan.updates.isEmpty())

        val overwrite = RegenerationPlanner.plan(DateRange(date("2026-11-01"), date("2026-11-08")), timeline, existing, emptyList(), holidays, overwriteUserChanges = true)
        assertEquals(ChangeType.REMOVE_USER_CHANGE, overwrite.changes.single { it.date == date("2026-11-03") }.type)
        assertEquals(ChangeType.KEEP_CLOSED, overwrite.changes.single { it.date == date("2026-11-02") }.type)
    }

    @Test
    fun `pristine shifts are retimed and removed by a new schedule`() {
        val newTimeline = listOf(assignment(2, SchedulePattern.Weekly(mapOf(java.time.DayOfWeek.MONDAY to ShiftSpec(8 * 60, 8 * 60))), "2026-11-01"))
        val existing = listOf(shift(1, "2026-11-02"), shift(2, "2026-11-03"))
        val plan = RegenerationPlanner.plan(DateRange(date("2026-11-02"), date("2026-11-03")), newTimeline, existing, emptyList(), holidays)
        assertEquals(date("2026-11-02").atTime(8, 0), plan.updates.single().plannedStart)
        assertEquals(2L, plan.deletes.single().id)
    }

    @Test
    fun `deleted template shift is not generated again and manual regular shift blocks the template`() {
        val timeline = listOf(assignment(1, TestData.fiveTwo(), "2026-10-01"))
        val existing = listOf(
            shift(1, "2026-10-05", status = ShiftStatus.DELETED, userModified = true),
            shift(2, "2026-10-06", origin = ShiftOrigin.MANUAL, start = "12:00", end = "20:00"),
        )
        val plan = RegenerationPlanner.planMissing(DateRange(date("2026-10-05"), date("2026-10-07")), timeline, existing, emptyList(), holidays)
        assertEquals(listOf(date("2026-10-07")), plan.inserts.map { it.date })
    }

    @Test
    fun `extra manual shift does not block the template`() {
        val timeline = listOf(assignment(1, TestData.fiveTwo(), "2026-10-01"))
        val existing = listOf(shift(2, "2026-10-06", origin = ShiftOrigin.MANUAL, kind = ShiftKind.EXTRA, start = "19:00", end = "23:00", breakMinutes = 0))
        val plan = RegenerationPlanner.planMissing(DateRange.single(date("2026-10-06")), timeline, existing, emptyList(), holidays)
        assertEquals(1, plan.inserts.size)
    }

    @Test
    fun `shifts generated inside an absence are covered`() {
        val timeline = listOf(assignment(1, TestData.fiveTwo(), "2026-10-01"))
        val vacation = Absence(id = 7, type = AbsenceType.VACATION, startDate = date("2026-10-12"), endDate = date("2026-10-18"))
        val plan = RegenerationPlanner.plan(DateRange(date("2026-10-12"), date("2026-10-18")), timeline, emptyList(), listOf(vacation), holidays)
        assertEquals(5, plan.inserts.size)
        assertTrue(plan.inserts.all { it.status == ShiftStatus.COVERED && it.absenceId == 7L })
    }

    @Test
    fun `scenario 8 - absence covers planned shifts and warns about confirmed`() {
        val sick = Absence(id = 3, type = AbsenceType.SICK, startDate = date("2026-10-05"), endDate = date("2026-10-07"))
        val shifts: List<Shift> = listOf(
            shift(1, "2026-10-05", status = ShiftStatus.CONFIRMED, worked = 480),
            shift(2, "2026-10-06"),
            shift(3, "2026-10-07"),
            shift(4, "2026-10-08"),
        )
        val coverage = AbsenceCoverage.plan(sick, shifts)
        assertEquals(listOf(2L, 3L), coverage.toCover.map { it.id })
        assertEquals(listOf(1L), coverage.conflicts.map { it.id })
        val restored = AbsenceCoverage.release(3, coverage.toCover)
        assertTrue(restored.all { it.status == ShiftStatus.PLANNED && it.absenceId == null })
        // Shortening the absence restores shifts outside the new period.
        val shortened = sick.copy(endDate = date("2026-10-06"))
        val replan = AbsenceCoverage.plan(shortened, coverage.toCover)
        assertEquals(listOf(3L), replan.toRestore.map { it.id })
    }
}
