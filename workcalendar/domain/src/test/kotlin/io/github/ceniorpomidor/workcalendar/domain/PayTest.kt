package io.github.ceniorpomidor.workcalendar.domain

import io.github.ceniorpomidor.workcalendar.domain.TestData.date
import io.github.ceniorpomidor.workcalendar.domain.TestData.dateTime
import io.github.ceniorpomidor.workcalendar.domain.TestData.shift
import io.github.ceniorpomidor.workcalendar.domain.model.AccrualType
import io.github.ceniorpomidor.workcalendar.domain.model.ManualAccrual
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.RatePeriod
import io.github.ceniorpomidor.workcalendar.domain.model.RateTable
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftKind
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftOrigin
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.pay.SummaryCalculator
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.YearMonth

class PayTest {
    private val surcharges = RateTable(
        listOf(
            RatePeriod(
                id = 1,
                effectiveFrom = date("2026-01-01"),
                hourlyRate = Money.ofRubles(300),
                holidayBonusPercent = 100,
                overtimeBonusPercent = 50,
                extraShiftBonusPercent = 100,
            ),
        ),
    )

    @Test
    fun `base pay for a regular shift`() {
        val pay = TestData.payCalculator().calculate(shift(1, "2026-10-05"), 480)!!
        assertEquals(Money.ofRubles(2400), pay.total)
        assertEquals(0, pay.overtimeMinutes)
    }

    @Test
    fun `holiday, overtime and extra surcharges`() {
        val calc = TestData.payCalculator(rates = surcharges)
        // Night hours have no surcharge.
        val night = calc.calculate(shift(1, "2026-10-05", start = "22:00", end = "06:00", breakMinutes = 0), 480)!!
        assertEquals(Money.ofRubles(2400), night.total)

        val holiday = calc.calculate(shift(2, "2026-11-04"), 480)!!
        assertEquals(480, holiday.holidayMinutes)
        assertEquals(Money.ofRubles(4800), holiday.total)

        val overtime = calc.calculate(shift(3, "2026-10-06"), 600)!!
        assertEquals(120, overtime.overtimeMinutes)
        assertEquals(Money.ofRubles(3000 + 300), overtime.total)

        val extra = calc.calculate(shift(4, "2026-10-10", origin = ShiftOrigin.MANUAL, kind = ShiftKind.EXTRA), 480)!!
        assertEquals(Money.ofRubles(4800), extra.total)
    }

    @Test
    fun `individual rate overrides the default`() {
        val s = shift(1, "2026-10-05").copy(hourlyRateOverride = Money.ofRubles(500))
        assertEquals(Money.ofRubles(4000), TestData.payCalculator().calculate(s, 480)!!.total)
    }

    @Test
    fun `scenario 7 - new rate applies from its date, confirmed shifts keep the old one`() {
        val before = TestData.payCalculator(rates = TestData.rates("2026-01-01" to 300))
        val confirmed = shift(1, "2026-10-20", status = ShiftStatus.CONFIRMED, worked = 480).let { it.copy(pay = before.calculate(it, 480)) }
        val after = TestData.payCalculator(rates = TestData.rates("2026-01-01" to 300, "2026-10-15" to 350))
        assertEquals(Money.ofRubles(2400), after.earned(confirmed))
        assertEquals(Money.ofRubles(2800), after.recalculated(confirmed).pay!!.total)
        assertEquals(Money.ofRubles(300), after.hourlyRateFor(shift(2, "2026-10-14")))
        assertEquals(Money.ofRubles(350), after.hourlyRateFor(shift(3, "2026-10-15")))
    }

    @Test
    fun `no rate means no amount instead of a fake one`() {
        val calc = TestData.payCalculator(rates = RateTable(emptyList()))
        assertNull(calc.calculate(shift(1, "2026-10-05"), 480))
        val summary = SummaryCalculator(calc).summarize(
            DateRange.single(date("2026-10-05")),
            listOf(shift(1, "2026-10-05")),
            emptyList(),
            emptyList(),
            dateTime("2026-10-01T00:00"),
        )
        assertNull(summary.forecastEarnings)
        assertEquals(1, summary.shiftsWithoutRate)
    }

    @Test
    fun `scenario 4, 6 and 10 - summary separates planned, confirmed and unconfirmed`() {
        val calc = TestData.payCalculator()
        val shifts = listOf(
            shift(1, "2026-10-05", status = ShiftStatus.CONFIRMED, worked = 480),
            shift(2, "2026-10-06", status = ShiftStatus.CONFIRMED, worked = 300),
            shift(3, "2026-10-07"),
            shift(4, "2026-10-08", status = ShiftStatus.MISSED, worked = 0),
            shift(5, "2026-10-09", status = ShiftStatus.CANCELLED),
            shift(6, "2026-10-12"),
            shift(7, "2026-10-10", origin = ShiftOrigin.MANUAL, kind = ShiftKind.EXTRA, status = ShiftStatus.CONFIRMED, worked = 240),
        )
        val accruals = listOf(
            ManualAccrual(date = date("2026-10-20"), type = AccrualType.BONUS, title = "Премия", amount = Money.ofRubles(5000)),
            ManualAccrual(date = date("2026-10-20"), type = AccrualType.DEDUCTION, title = "Удержание", amount = Money.ofRubles(1000)),
        )
        val now = dateTime("2026-10-09T12:00")
        val s = SummaryCalculator(calc).summarize(DateRange.month(YearMonth.of(2026, 10)), shifts, emptyList(), accruals, now)
        assertEquals(6, s.scheduledShifts)
        assertEquals(3, s.workedShifts)
        assertEquals(1, s.missedShifts)
        assertEquals(1, s.cancelledShifts)
        assertEquals(1, s.unconfirmedShifts)
        assertEquals(1, s.upcomingShifts)
        assertEquals(480L + 300 + 240, s.workedMinutes)
        assertEquals(240L, s.extraMinutes)
        assertEquals(Money.ofRubles(2400 + 1500 + 1200), s.confirmedEarnings)
        // Forecast adds two unconfirmed planned shifts (8 h each).
        assertEquals(Money.ofRubles(2400 + 1500 + 1200 + 2400 + 2400), s.forecastEarnings)
        assertEquals(Money.ofRubles(5100 + 5000 - 1000), s.grossAccrued)
        assertEquals(Money.ofRubles(1700), s.averagePerShift)
    }
}
