package io.github.ceniorpomidor.workcalendar.domain

import io.github.ceniorpomidor.workcalendar.domain.TestData.date
import io.github.ceniorpomidor.workcalendar.domain.TestData.dateTime
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.Payment
import io.github.ceniorpomidor.workcalendar.domain.model.PaymentType
import io.github.ceniorpomidor.workcalendar.domain.model.PayoutAmountMode
import io.github.ceniorpomidor.workcalendar.domain.model.PayoutRule
import io.github.ceniorpomidor.workcalendar.domain.model.RateTable
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.pay.PayCalculator
import io.github.ceniorpomidor.workcalendar.domain.pay.PayoutAmount
import io.github.ceniorpomidor.workcalendar.domain.pay.PayoutCalculator
import io.github.ceniorpomidor.workcalendar.domain.pay.PayoutInstance
import io.github.ceniorpomidor.workcalendar.domain.pay.SummaryCalculator
import io.github.ceniorpomidor.workcalendar.domain.schedule.ScheduleGenerator
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.YearMonth

class PayoutTest {
    private val october = YearMonth.of(2026, 10)
    private val advance = PayoutRule.defaultAdvance().copy(id = 1)
    private val salary = PayoutRule.defaultSalary().copy(id = 2)

    /** All October weekdays confirmed with 8 hours at 300 ₽: 2 400 ₽ per shift. */
    private fun octoberShifts(confirmed: Boolean = true): List<Shift> =
        ScheduleGenerator.generate(TestData.fiveTwo(), DateRange.month(october), io.github.ceniorpomidor.workcalendar.domain.time.HolidayCalendar())
            .mapIndexed { i, g ->
                TestData.shift(i + 1L, g.date.toString()).let {
                    if (confirmed) it.copy(status = ShiftStatus.CONFIRMED, workedMinutes = 480) else it
                }
            }

    private fun calculator(rates: RateTable = TestData.rates("2020-01-01" to 300)): PayoutCalculator {
        val pay = PayCalculator(rates, io.github.ceniorpomidor.workcalendar.domain.time.HolidayCalendar(), TestData.zone)
        return PayoutCalculator(pay, SummaryCalculator(pay))
    }

    private fun PayoutInstance.net(): Money = (amount as PayoutAmount.Calculated).net

    @Test
    fun `advance and salary from earnings`() {
        val now = dateTime("2026-11-01T00:00")
        val payouts = calculator().forMonth(october, listOf(advance, salary), octoberShifts(), emptyList(), emptyList(), now)
        val a = payouts.single { it.rule.id == 1L }
        val s = payouts.single { it.rule.id == 2L }
        // 11 weekdays in 01–15.10.2026, 22 in the month.
        assertEquals(Money.ofRubles(11 * 2400), a.net())
        assertEquals(Money.ofRubles(11 * 2400), s.net())
        // 25.10.2026 is Sunday: paid on Friday 23.10.
        assertEquals(date("2026-10-23"), a.payDate)
        assertEquals(date("2026-11-10"), s.payDate)
        assertEquals("payout:1:2026-10", a.key)
    }

    @Test
    fun `scenario 9 - received advance reduces the salary remainder`() {
        val now = dateTime("2026-11-01T00:00")
        val received = Payment(id = 1, type = PaymentType.ADVANCE, amount = Money.ofRubles(25_000), date = date("2026-10-23"), payoutKey = "payout:1:2026-10")
        val payouts = calculator().forMonth(october, listOf(advance, salary), octoberShifts(), emptyList(), listOf(received), now)
        val a = payouts.single { it.rule.id == 1L }
        assertTrue(a.isReceived)
        assertEquals(Money.ofRubles(25_000), a.effectiveNet)
        assertEquals(Money.ofRubles(22 * 2400 - 25_000), payouts.single { it.rule.id == 2L }.net())
    }

    @Test
    fun `fixed and formula advances`() {
        val now = dateTime("2026-11-01T00:00")
        val fixed = advance.copy(amountMode = PayoutAmountMode.FIXED, fixedAmount = Money.ofRubles(20_000))
        val p1 = calculator().forMonth(october, listOf(fixed, salary), octoberShifts(), emptyList(), emptyList(), now)
        assertEquals(Money.ofRubles(20_000), p1.single { it.rule.id == 1L }.net())
        assertEquals(Money.ofRubles(22 * 2400 - 20_000), p1.single { it.rule.id == 2L }.net())

        val formula = advance.copy(amountMode = PayoutAmountMode.FORMULA, formula = "ЗАРАБОТОК * 50%")
        val p2 = calculator().forMonth(october, listOf(formula, salary), octoberShifts(), emptyList(), emptyList(), now)
        assertEquals(Money.ofRubles(11 * 1200), p2.single { it.rule.id == 1L }.net())
    }

    @Test
    fun `estimate is flagged and missing rate is reported`() {
        val now = dateTime("2026-10-01T08:00")
        val estimate = calculator().forMonth(october, listOf(advance), octoberShifts(confirmed = false), emptyList(), emptyList(), now).single()
        val amount = estimate.amount as PayoutAmount.Calculated
        assertTrue(amount.isEstimate)
        assertEquals(Money.ofRubles(11 * 2400), amount.net)

        val noRate = calculator(rates = RateTable(emptyList())).forMonth(october, listOf(advance), octoberShifts(), emptyList(), emptyList(), now).single()
        assertTrue(noRate.amount is PayoutAmount.Insufficient)
    }

    @Test
    fun `payouts between dates`() {
        val now = dateTime("2026-10-04T12:00")
        val list = calculator().between(date("2026-10-04"), date("2026-11-30"), listOf(advance, salary), octoberShifts(), emptyList(), emptyList(), now)
        assertEquals(listOf("payout:2:2026-09", "payout:1:2026-10", "payout:2:2026-10", "payout:1:2026-11"), list.map { it.key })
        assertEquals(2L to october, PayoutInstance.parseKey("payout:2:2026-10"))
    }
}
