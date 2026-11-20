package io.github.ceniorpomidor.workcalendar.domain

import io.github.ceniorpomidor.workcalendar.domain.TestData.date
import io.github.ceniorpomidor.workcalendar.domain.TestData.dateTime
import io.github.ceniorpomidor.workcalendar.domain.TestData.shift
import io.github.ceniorpomidor.workcalendar.domain.absence.AbsencePayCalculator
import io.github.ceniorpomidor.workcalendar.domain.absence.AbsencePaymentDates
import io.github.ceniorpomidor.workcalendar.domain.absence.EarningsHistory
import io.github.ceniorpomidor.workcalendar.domain.absence.InsuranceExperience
import io.github.ceniorpomidor.workcalendar.domain.model.Absence
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceRules
import io.github.ceniorpomidor.workcalendar.domain.model.AbsenceType
import io.github.ceniorpomidor.workcalendar.domain.model.ExternalEarning
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.time.HolidayCalendar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AbsencePayTest {
    private val now = dateTime("2026-10-04T12:00")
    private val holidays = HolidayCalendar()

    private fun history(external: List<ExternalEarning>, employment: String? = "2015-01-01") = EarningsHistory.build(
        shifts = emptyList(),
        absences = emptyList(),
        accruals = emptyList(),
        external = external,
        pay = TestData.payCalculator(),
        employmentStart = employment?.let { date(it) },
        trackingStart = date("2026-10-01"),
    )

    @Test
    fun `vacation pay by planned shifts`() {
        val vacation = Absence(id = 5, type = AbsenceType.VACATION, startDate = date("2026-10-12"), endDate = date("2026-10-18"))
        val covered = (12..16).map { shift(it.toLong(), "2026-10-%02d".format(it), status = ShiftStatus.COVERED).copy(absenceId = 5) }
        val result = AbsencePayCalculator.byPlannedShifts(vacation, covered, TestData.payCalculator(), now)
        assertEquals(Money.ofRubles(5 * 2400), result.total)
        assertFalse(result.insufficientData)
    }

    @Test
    fun `vacation pay by average earnings excludes public holidays`() {
        val vacation = Absence(type = AbsenceType.VACATION, startDate = date("2026-11-02"), endDate = date("2026-11-15"))
        val external = (0 until 12).map { i ->
            val m = java.time.YearMonth.of(2025, 11).plusMonths(i.toLong())
            ExternalEarning(year = m.year, month = m.monthValue, amount = Money.ofRubles(50_000))
        }
        val result = AbsencePayCalculator.byAverageEarnings(vacation, history(external), emptyList(), holidays, AbsenceRules(), now)
        // 600 000 / (12 × 29.3) = 1 706.48 per day; 14 days minus 04.11 = 13 paid days.
        assertEquals(13, result.days)
        assertEquals(Money(170648), result.averageDaily)
        assertEquals(Money(170648 * 13), result.total)
        assertFalse(result.insufficientData)
    }

    @Test
    fun `average earnings without data is reported as insufficient`() {
        val vacation = Absence(type = AbsenceType.VACATION, startDate = date("2026-11-02"), endDate = date("2026-11-15"))
        val result = AbsencePayCalculator.byAverageEarnings(vacation, history(emptyList()), emptyList(), holidays, AbsenceRules(), now)
        assertTrue(result.insufficientData)
        assertTrue(result.warnings.isNotEmpty())
    }

    @Test
    fun `sick benefit by 255-FZ with experience percent and employer part`() {
        val sick = Absence(type = AbsenceType.SICK, startDate = date("2026-10-05"), endDate = date("2026-10-11"))
        val external = listOf(
            ExternalEarning(year = 2024, amount = Money.ofRubles(600_000)),
            ExternalEarning(year = 2025, amount = Money.ofRubles(720_000)),
        )
        val months = InsuranceExperience.months(date("2020-04-01"), 0, sick.startDate)
        assertEquals(78, months)
        val result = AbsencePayCalculator.sickBenefit(sick, history(external), AbsenceRules(), months, now)
        assertEquals(80, result.percent)
        assertEquals(Money(180822), result.averageDaily)
        assertEquals(Money(144658 * 7), result.total)
        assertEquals(Money(144658 * 3), result.employerPart)
        assertEquals(result.total - result.employerPart!!, result.fundPart)
        assertFalse(result.insufficientData)
    }

    @Test
    fun `sick benefit uses the minimum wage when data is missing`() {
        val sick = Absence(type = AbsenceType.SICK, startDate = date("2026-10-05"), endDate = date("2026-10-06"))
        val result = AbsencePayCalculator.sickBenefit(sick, history(emptyList()), AbsenceRules(), 120, now)
        assertTrue(result.insufficientData)
        assertEquals(Money.ofRubles(27_093).scale(24, 730), result.averageDaily)
    }

    @Test
    fun `experience percent thresholds`() {
        assertEquals(60, InsuranceExperience.sickPercent(59))
        assertEquals(80, InsuranceExperience.sickPercent(60))
        assertEquals(80, InsuranceExperience.sickPercent(95))
        assertEquals(100, InsuranceExperience.sickPercent(96))
        assertEquals("6 лет 6 мес.", InsuranceExperience.describe(78))
    }

    @Test
    fun `payment dates`() {
        val vacation = Absence(type = AbsenceType.VACATION, startDate = date("2026-11-09"), endDate = date("2026-11-22"))
        // Two working days before Monday 09.11 (04.11 is a holiday): Thursday 05.11.
        assertEquals(date("2026-11-05"), AbsencePaymentDates.vacationPayDate(vacation, holidays, 2))
        val sick = Absence(type = AbsenceType.SICK, startDate = date("2026-10-05"), endDate = date("2026-10-11"))
        assertEquals(date("2026-10-23"), AbsencePaymentDates.sickEmployerDate(sick, listOf(date("2026-10-09"), date("2026-10-23")), holidays))
        assertEquals(date("2026-10-23"), AbsencePaymentDates.sickFundDate(sick, holidays, 10))
    }
}
