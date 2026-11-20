package io.github.ceniorpomidor.workcalendar.domain

import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.RatePeriod
import io.github.ceniorpomidor.workcalendar.domain.model.RateTable
import io.github.ceniorpomidor.workcalendar.domain.model.ScheduleAssignment
import io.github.ceniorpomidor.workcalendar.domain.model.SchedulePattern
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftKind
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftOrigin
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftSpec
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.pay.PayCalculator
import io.github.ceniorpomidor.workcalendar.domain.time.HolidayCalendar
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object TestData {
    val zone: ZoneId = ZoneId.of("Europe/Moscow")

    fun date(text: String): LocalDate = LocalDate.parse(text)

    fun dateTime(text: String): LocalDateTime = LocalDateTime.parse(text)

    /** 09:00–18:00 with a 60 minutes break: 8 paid hours. */
    val officeDay = ShiftSpec(9 * 60, 9 * 60, 60)

    fun fiveTwo(skipHolidays: Boolean = false): SchedulePattern.Weekly =
        SchedulePattern.Weekly(DayOfWeek.entries.filter { it.value <= 5 }.associateWith { officeDay }, skipHolidays)

    fun twoTwo(anchor: LocalDate): SchedulePattern.Cycle = SchedulePattern.Cycle(listOf(officeDay, officeDay, null, null), anchor)

    fun assignment(id: Long, pattern: SchedulePattern, start: String, end: String? = null): ScheduleAssignment =
        ScheduleAssignment(id = id, templateName = "T$id", pattern = pattern, startDate = date(start), endDate = end?.let { date(it) })

    fun shift(
        id: Long,
        date: String,
        start: String = "09:00",
        end: String = "18:00",
        breakMinutes: Int = 60,
        status: ShiftStatus = ShiftStatus.PLANNED,
        origin: ShiftOrigin = ShiftOrigin.TEMPLATE,
        kind: ShiftKind = ShiftKind.REGULAR,
        worked: Int? = null,
        userModified: Boolean = false,
    ): Shift {
        val d = date(date)
        val s = d.atTime(java.time.LocalTime.parse(start))
        var e = d.atTime(java.time.LocalTime.parse(end))
        if (!e.isAfter(s)) e = e.plusDays(1)
        return Shift(
            id = id,
            date = d,
            plannedStart = s,
            plannedEnd = e,
            plannedBreakMinutes = breakMinutes,
            kind = kind,
            origin = origin,
            status = status,
            workedMinutes = worked,
            userModified = userModified,
        )
    }

    fun rates(vararg periods: Pair<String, Long>): RateTable =
        RateTable(periods.mapIndexed { i, (from, rubles) -> RatePeriod(id = i + 1L, effectiveFrom = date(from), hourlyRate = Money.ofRubles(rubles)) })

    fun payCalculator(
        rates: RateTable = rates("2020-01-01" to 300),
        holidays: HolidayCalendar = HolidayCalendar(),
    ): PayCalculator = PayCalculator(rates, holidays, zone)
}
