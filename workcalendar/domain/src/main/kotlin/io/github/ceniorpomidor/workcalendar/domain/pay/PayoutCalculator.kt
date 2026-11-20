package io.github.ceniorpomidor.workcalendar.domain.pay

import io.github.ceniorpomidor.workcalendar.domain.model.ManualAccrual
import io.github.ceniorpomidor.workcalendar.domain.model.Money
import io.github.ceniorpomidor.workcalendar.domain.model.Payment
import io.github.ceniorpomidor.workcalendar.domain.model.PaymentType
import io.github.ceniorpomidor.workcalendar.domain.model.PayoutAmountMode
import io.github.ceniorpomidor.workcalendar.domain.model.PayoutKind
import io.github.ceniorpomidor.workcalendar.domain.model.PayoutRule
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import io.github.ceniorpomidor.workcalendar.domain.model.WeekendShift
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/** Expected amount of a payout. */
sealed interface PayoutAmount {
    data class Calculated(
        /** Earnings the payout is calculated from. */
        val gross: Money,
        /** Amount to be paid (for the remainder: minus the other payouts of the month). */
        val net: Money,
        /** Based on planned hours of shifts that are not confirmed yet. */
        val isEstimate: Boolean,
        val unconfirmedShifts: Int,
        val upcomingShifts: Int,
        val explanation: List<String>,
    ) : PayoutAmount

    /** Not enough data to calculate; [reason] is shown to the user instead of an amount. */
    data class Insufficient(val reason: String) : PayoutAmount
}

/** One concrete payout, e.g. "advance for October 2026, paid on 23.10.2026". */
data class PayoutInstance(
    val rule: PayoutRule,
    val periodMonth: YearMonth,
    val period: DateRange,
    val payDate: LocalDate,
    val nominalPayDate: LocalDate,
    val amount: PayoutAmount,
    val payment: Payment?,
) {
    val key: String get() = keyOf(rule.id, periodMonth)

    val isReceived: Boolean get() = payment != null

    val expectedNet: Money? get() = (amount as? PayoutAmount.Calculated)?.net

    /** Received amount if registered, otherwise the expected one. */
    val effectiveNet: Money? get() = payment?.amount ?: expectedNet

    val title: String get() = "${rule.name} за ${Formats.monthTitle(periodMonth).lowercase()}"

    companion object {
        fun keyOf(ruleId: Long, month: YearMonth): String = "payout:$ruleId:$month"

        /** Parses a key created by [keyOf]: rule id and period month. */
        fun parseKey(key: String): Pair<Long, YearMonth>? {
            val parts = key.split(':')
            if (parts.size != 3 || parts[0] != "payout") return null
            val id = parts[1].toLongOrNull() ?: return null
            val month = runCatching { YearMonth.parse(parts[2]) }.getOrNull() ?: return null
            return id to month
        }
    }
}

/** Calculates expected advances and salaries from the payout rules and the earnings. */
class PayoutCalculator(
    private val pay: PayCalculator,
    private val summaries: SummaryCalculator,
) {
    fun nominalPayDate(rule: PayoutRule, periodMonth: YearMonth): LocalDate {
        val payMonth = periodMonth.plusMonths(rule.payMonthOffset.toLong())
        return payMonth.atDay(rule.payDay.coerceAtMost(payMonth.lengthOfMonth()))
    }

    fun payDate(rule: PayoutRule, periodMonth: YearMonth): LocalDate {
        val nominal = nominalPayDate(rule, periodMonth)
        return when (rule.weekendShift) {
            WeekendShift.BEFORE -> pay.holidays.businessDayOnOrBefore(nominal)
            WeekendShift.AFTER -> pay.holidays.businessDayOnOrAfter(nominal)
            WeekendShift.NONE -> nominal
        }
    }

    fun period(rule: PayoutRule, periodMonth: YearMonth): DateRange {
        val len = periodMonth.lengthOfMonth()
        return DateRange(periodMonth.atDay(rule.periodStartDay.coerceAtMost(len)), periodMonth.atDay(rule.periodEndDay.coerceAtMost(len)))
    }

    /** All payouts for the period month [periodMonth]. */
    fun forMonth(
        periodMonth: YearMonth,
        rules: List<PayoutRule>,
        shifts: List<Shift>,
        accruals: List<ManualAccrual>,
        payments: List<Payment>,
        now: LocalDateTime,
    ): List<PayoutInstance> {
        val active = rules.filter { it.enabled }.sortedWith(compareBy<PayoutRule> { it.sortOrder }.thenBy { it.id })
        val ordered = active.sortedBy {
            when (it.amountMode) {
                PayoutAmountMode.FIXED, PayoutAmountMode.PERCENT_OF_PERIOD -> 0
                PayoutAmountMode.FORMULA -> 1
                PayoutAmountMode.MONTH_REMAINDER -> 2
            }
        }
        val monthRange = DateRange.month(periodMonth)
        val monthSummary = summaries.summarize(monthRange, shifts, emptyList(), accruals, now)
        val monthBase = base(monthSummary)
        val results = LinkedHashMap<Long, PayoutInstance>()
        for (rule in ordered) {
            val period = period(rule, periodMonth)
            val key = PayoutInstance.keyOf(rule.id, periodMonth)
            val payment = payments.firstOrNull { it.payoutKey == key }
            val othersNet = results.values.mapNotNull { it.effectiveNet }.fold(Money.ZERO) { a, b -> a + b }
            val amount = when (rule.amountMode) {
                PayoutAmountMode.FIXED -> PayoutAmount.Calculated(
                    gross = rule.fixedAmount,
                    net = rule.fixedAmount,
                    isEstimate = false,
                    unconfirmedShifts = 0,
                    upcomingShifts = 0,
                    explanation = listOf("Фиксированная сумма"),
                )
                PayoutAmountMode.PERCENT_OF_PERIOD -> {
                    val summary = summaries.summarize(period, shifts, emptyList(), accruals, now)
                    percentAmount(rule, summary)
                }
                PayoutAmountMode.FORMULA -> {
                    val summary = summaries.summarize(period, shifts, emptyList(), accruals, now)
                    formulaAmount(rule, summary, monthBase, othersNet)
                }
                PayoutAmountMode.MONTH_REMAINDER -> remainderAmount(rule, monthSummary, monthBase, results.values.toList())
            }
            results[rule.id] = PayoutInstance(
                rule = rule,
                periodMonth = periodMonth,
                period = period,
                payDate = payDate(rule, periodMonth),
                nominalPayDate = nominalPayDate(rule, periodMonth),
                amount = amount,
                payment = payment,
            )
        }
        return results.values.sortedWith(compareBy<PayoutInstance> { it.payDate }.thenBy { it.rule.sortOrder })
    }

    /** Payouts whose pay date is within [from]..[to]. */
    fun between(
        from: LocalDate,
        to: LocalDate,
        rules: List<PayoutRule>,
        shifts: List<Shift>,
        accruals: List<ManualAccrual>,
        payments: List<Payment>,
        now: LocalDateTime,
    ): List<PayoutInstance> {
        if (rules.none { it.enabled }) return emptyList()
        val maxOffset = rules.maxOf { it.payMonthOffset }.toLong()
        var month = YearMonth.from(from).minusMonths(maxOffset)
        val last = YearMonth.from(to)
        val result = ArrayList<PayoutInstance>()
        while (month <= last) {
            result += forMonth(month, rules, shifts, accruals, payments, now).filter { !it.payDate.isBefore(from) && !it.payDate.isAfter(to) }
            month = month.plusMonths(1)
        }
        return result.sortedBy { it.payDate }
    }

    /** Range of shift dates needed to calculate payouts of [periodMonth]. */
    fun requiredRange(periodMonth: YearMonth): DateRange = DateRange.month(periodMonth)

    private data class Base(
        val amount: Money?,
        val isEstimate: Boolean,
        val unconfirmed: Int,
        val upcoming: Int,
        val missingRate: Int,
        val minutes: Long,
        val shifts: Int,
    )

    private fun base(summary: PeriodSummary): Base {
        val manual = summary.bonuses - summary.deductions
        val pending = summary.unconfirmedShifts + summary.upcomingShifts
        val isEstimate = pending > 0
        val amount = if (isEstimate) summary.forecastEarnings?.plus(manual) else summary.confirmedEarnings + manual
        val minutes = summary.workedMinutes + summary.pendingMinutes
        return Base(
            amount = amount,
            isEstimate = isEstimate,
            unconfirmed = summary.unconfirmedShifts,
            upcoming = summary.upcomingShifts,
            missingRate = summary.shiftsWithoutRate,
            minutes = minutes,
            shifts = summary.workedShifts + pending,
        )
    }

    private fun missingRate(base: Base): PayoutAmount.Insufficient? =
        if (base.missingRate > 0 || base.amount == null) {
            PayoutAmount.Insufficient("Не задана почасовая ставка для ${Formats.shifts(base.missingRate)} периода — укажите ставку в настройках")
        } else {
            null
        }

    private fun percentAmount(rule: PayoutRule, summary: PeriodSummary): PayoutAmount {
        val base = base(summary)
        missingRate(base)?.let { return it }
        val earned = base.amount ?: Money.ZERO
        val gross = earned.percent(rule.percent)
        val lines = ArrayList<String>()
        lines += "${rule.percent}% от заработка за ${Formats.period(summary.range.start, summary.range.endInclusive)}: ${Formats.money(earned)}"
        if (summary.scheduledShifts == 0 && summary.bonuses.isZero) lines += "Нет смен за период"
        if (base.isEstimate) lines += estimateNote(base)
        return PayoutAmount.Calculated(gross, gross, base.isEstimate, base.unconfirmed, base.upcoming, lines)
    }

    private fun formulaAmount(rule: PayoutRule, summary: PeriodSummary, monthBase: Base, othersNet: Money): PayoutAmount {
        val base = base(summary)
        missingRate(base)?.let { return it }
        val rate = pay.rates.on(summary.range.endInclusive)?.hourlyRate ?: Money.ZERO
        val variables = mapOf(
            "ЗАРАБОТОК" to (base.amount ?: Money.ZERO).toRubles(),
            "ЧАСЫ" to base.minutes / 60.0,
            "СМЕНЫ" to base.shifts.toDouble(),
            "ПЛАН" to (summary.plannedEarnings ?: Money.ZERO).toRubles(),
            "ПЛАН_ЧАСЫ" to summary.plannedMinutes / 60.0,
            "СТАВКА" to rate.toRubles(),
            "МЕСЯЦ" to (monthBase.amount ?: Money.ZERO).toRubles(),
            "ВЫПЛАЧЕНО" to othersNet.toRubles(),
        )
        val value = try {
            Formula.evaluate(rule.formula, variables)
        } catch (e: Formula.FormulaException) {
            return PayoutAmount.Insufficient("Ошибка в формуле: ${e.message}")
        }
        val gross = Money.ofRubles(value).coerceAtLeast(Money.ZERO)
        val lines = ArrayList<String>()
        lines += "Формула: ${rule.formula}"
        lines += "Заработок за ${Formats.period(summary.range.start, summary.range.endInclusive)}: ${Formats.money(base.amount ?: Money.ZERO)}"
        if (base.isEstimate) lines += estimateNote(base)
        return PayoutAmount.Calculated(gross, gross, base.isEstimate, base.unconfirmed, base.upcoming, lines)
    }

    private fun remainderAmount(rule: PayoutRule, monthSummary: PeriodSummary, monthBase: Base, others: List<PayoutInstance>): PayoutAmount {
        missingRate(monthBase)?.let { return it }
        val monthGross = monthBase.amount ?: Money.ZERO
        val lines = ArrayList<String>()
        lines += "Заработок за ${Formats.monthTitle(YearMonth.from(monthSummary.range.start)).lowercase()}: ${Formats.money(monthGross)}"
        var paid = Money.ZERO
        for (other in others) {
            val value = other.effectiveNet
            if (value == null) {
                return PayoutAmount.Insufficient("Не удалось рассчитать «${other.rule.name}», поэтому остаток неизвестен")
            }
            paid += value
            val label = if (other.isReceived) "получено" else "ожидается"
            lines += "− ${other.rule.name} ($label): ${Formats.money(value)}"
        }
        var net = monthGross - paid
        if (net.isNegative) {
            lines += "Выплачено больше начисленного на ${Formats.money(-net)}"
            net = Money.ZERO
        }
        if (monthBase.isEstimate) lines += estimateNote(monthBase)
        return PayoutAmount.Calculated(monthGross, net, monthBase.isEstimate, monthBase.unconfirmed, monthBase.upcoming, lines)
    }

    private fun estimateNote(base: Base): String {
        val parts = ArrayList<String>()
        if (base.unconfirmed > 0) parts += "${Formats.shifts(base.unconfirmed)} без подтверждённых часов"
        if (base.upcoming > 0) parts += "${Formats.shifts(base.upcoming)} ещё впереди"
        return "Оценка по плану: ${parts.joinToString(", ")}"
    }

    companion object {
        fun paymentTypeFor(kind: PayoutKind): PaymentType = when (kind) {
            PayoutKind.ADVANCE -> PaymentType.ADVANCE
            PayoutKind.SALARY -> PaymentType.SALARY
            PayoutKind.OTHER -> PaymentType.OTHER
        }

        /** Planned shifts of [range] whose hours are still not entered. */
        fun unconfirmedIn(range: DateRange, shifts: List<Shift>, now: LocalDateTime): Int =
            shifts.count { it.date in range && it.status == ShiftStatus.PLANNED && !now.isBefore(it.plannedEnd) }
    }
}
