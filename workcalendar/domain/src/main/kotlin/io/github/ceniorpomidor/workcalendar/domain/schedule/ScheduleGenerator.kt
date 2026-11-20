package io.github.ceniorpomidor.workcalendar.domain.schedule

import io.github.ceniorpomidor.workcalendar.domain.model.ScheduleAssignment
import io.github.ceniorpomidor.workcalendar.domain.model.SchedulePattern
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftSpec
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.time.HolidayCalendar
import java.time.LocalDate
import java.time.LocalDateTime

/** Planned shift produced by a schedule pattern for one date. */
data class GeneratedShift(
    val date: LocalDate,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val breakMinutes: Int,
    val title: String,
    val assignmentId: Long?,
)

/** Expands schedule patterns into concrete dates. */
object ScheduleGenerator {
    fun shiftFor(pattern: SchedulePattern, date: LocalDate, holidays: HolidayCalendar): ShiftSpec? {
        val spec = pattern.specFor(date) ?: return null
        if (pattern.skipHolidays && holidays.isNonWorkingDay(date) && holidays.holidayName(date) != null) return null
        return spec
    }

    fun generate(pattern: SchedulePattern, range: DateRange, holidays: HolidayCalendar, assignmentId: Long? = null): List<GeneratedShift> =
        range.mapNotNull { date ->
            shiftFor(pattern, date, holidays)?.let { spec -> spec.toGenerated(date, assignmentId) }
        }

    /** Shifts produced by the assignment timeline for the dates of [range]. */
    fun generate(assignments: List<ScheduleAssignment>, range: DateRange, holidays: HolidayCalendar): List<GeneratedShift> {
        val result = ArrayList<GeneratedShift>()
        for (assignment in assignments.sortedBy { it.startDate }) {
            val assignmentRange = DateRange(assignment.startDate, assignment.endDate ?: range.endInclusive.coerceAtLeast(assignment.startDate))
            val part = assignmentRange.intersect(range) ?: continue
            result += generate(assignment.pattern, part, holidays, assignment.id)
        }
        return result.sortedBy { it.date }
    }

    fun ShiftSpec.toGenerated(date: LocalDate, assignmentId: Long?): GeneratedShift = GeneratedShift(
        date = date,
        start = startOn(date),
        end = endOn(date),
        breakMinutes = breakMinutes,
        title = title,
        assignmentId = assignmentId,
    )
}

/** Ready-made patterns offered when a template is created. */
object SchedulePresets {
    data class Preset(val id: String, val name: String, val description: String)

    val all: List<Preset> = listOf(
        Preset("5x2", "5/2", "Пн–Пт, выходные Сб и Вс"),
        Preset("2x2", "2/2", "Два дня работы, два дня отдыха"),
        Preset("3x3", "3/3", "Три дня работы, три дня отдыха"),
        Preset("1x3", "Сутки через трое", "24 часа работы, трое суток отдыха"),
        Preset("1x2", "Сутки через двое", "24 часа работы, двое суток отдыха"),
        Preset("day-night", "День/ночь/2 выходных", "Дневная, ночная смена и два выходных"),
        Preset("custom", "Свой цикл", "Произвольная последовательность рабочих и выходных дней"),
    )

    /** Pattern for a preset; [startMinute] and [breakMinutes] apply to all presets, 24-hour presets ignore the duration. */
    fun create(id: String, anchorDate: LocalDate, startMinute: Int = 9 * 60, durationMinutes: Int = 9 * 60, breakMinutes: Int = 60): SchedulePattern {
        val day = ShiftSpec(startMinute, durationMinutes, breakMinutes.coerceIn(0, durationMinutes - 1))
        val fullDay = ShiftSpec(startMinute, 24 * 60, breakMinutes.coerceIn(0, 24 * 60 - 1))
        return when (id) {
            "5x2" -> SchedulePattern.Weekly(
                days = java.time.DayOfWeek.entries.filter { it.value <= 5 }.associateWith { day },
                skipHolidays = true,
            )
            "2x2" -> SchedulePattern.Cycle(listOf(day, day, null, null), anchorDate)
            "3x3" -> SchedulePattern.Cycle(listOf(day, day, day, null, null, null), anchorDate)
            "1x3" -> SchedulePattern.Cycle(listOf(fullDay, null, null, null), anchorDate)
            "1x2" -> SchedulePattern.Cycle(listOf(fullDay, null, null), anchorDate)
            "day-night" -> SchedulePattern.Cycle(
                listOf(
                    ShiftSpec(startMinute, 12 * 60, breakMinutes.coerceIn(0, 12 * 60 - 1), "День"),
                    ShiftSpec((startMinute + 12 * 60) % (24 * 60), 12 * 60, breakMinutes.coerceIn(0, 12 * 60 - 1), "Ночь"),
                    null,
                    null,
                ),
                anchorDate,
            )
            else -> SchedulePattern.Cycle(listOf(day, null), anchorDate)
        }
    }
}
