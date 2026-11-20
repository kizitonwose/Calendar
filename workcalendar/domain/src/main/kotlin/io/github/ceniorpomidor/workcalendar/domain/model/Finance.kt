@file:UseSerializers(LocalDateSerializer::class, LocalDateTimeSerializer::class)

package io.github.ceniorpomidor.workcalendar.domain.model

import io.github.ceniorpomidor.workcalendar.domain.util.LocalDateSerializer
import io.github.ceniorpomidor.workcalendar.domain.util.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
enum class PayoutKind {
    ADVANCE,
    SALARY,
    OTHER,
}

@Serializable
enum class PayoutAmountMode {
    /** Fixed amount. */
    FIXED,

    /** Percent of the earnings for the payout period. */
    PERCENT_OF_PERIOD,

    /** Earnings for the whole month minus other payouts for the same month. */
    MONTH_REMAINDER,

    /** User formula, see [io.github.ceniorpomidor.workcalendar.domain.pay.Formula]. */
    FORMULA,
}

/** What to do when a payday falls on a weekend or a public holiday. */
@Serializable
enum class WeekendShift {
    /** Pay on the previous working day (art. 136 of the Russian Labour Code). */
    BEFORE,
    AFTER,
    NONE,
}

/**
 * Rule of a regular payout. The payout for month M covers days from `periodStartDay` to
 * `periodEndDay` of M and is paid on `payDay` of month M + `payMonthOffset`.
 */
@Serializable
data class PayoutRule(
    val id: Long = 0,
    val name: String,
    val kind: PayoutKind,
    val enabled: Boolean = true,
    val payDay: Int,
    val payMonthOffset: Int = 0,
    val periodStartDay: Int = 1,
    val periodEndDay: Int = 31,
    val amountMode: PayoutAmountMode,
    val fixedAmount: Money = Money.ZERO,
    val percent: Int = 100,
    val formula: String = "",
    val weekendShift: WeekendShift = WeekendShift.BEFORE,
    val notify: Boolean = true,
    val notifyDaysBefore: Int = 1,
    val sortOrder: Int = 0,
) {
    init {
        require(payDay in 1..31) { "payDay out of range" }
        require(periodStartDay in 1..31 && periodEndDay in 1..31 && periodStartDay <= periodEndDay) { "Invalid period days" }
        require(payMonthOffset in 0..2) { "payMonthOffset out of range" }
        require(notifyDaysBefore in 0..30) { "notifyDaysBefore out of range" }
    }

    companion object {
        fun defaultAdvance(): PayoutRule = PayoutRule(
            name = "Аванс",
            kind = PayoutKind.ADVANCE,
            payDay = 25,
            payMonthOffset = 0,
            periodStartDay = 1,
            periodEndDay = 15,
            amountMode = PayoutAmountMode.PERCENT_OF_PERIOD,
            percent = 100,
            sortOrder = 0,
        )

        fun defaultSalary(): PayoutRule = PayoutRule(
            name = "Зарплата",
            kind = PayoutKind.SALARY,
            payDay = 10,
            payMonthOffset = 1,
            periodStartDay = 1,
            periodEndDay = 31,
            amountMode = PayoutAmountMode.MONTH_REMAINDER,
            sortOrder = 1,
        )
    }
}

@Serializable
enum class PaymentType {
    ADVANCE,
    SALARY,
    VACATION_PAY,
    SICK_PAY,
    BONUS,
    OTHER,
}

/** Money actually received. Stored separately from accruals so payouts never double-count earnings. */
@Serializable
data class Payment(
    val id: Long = 0,
    val type: PaymentType,
    val amount: Money,
    val date: LocalDate,
    /** Key of the expected payout this payment closes (see PayoutInstance.key). */
    val payoutKey: String? = null,
    val periodStart: LocalDate? = null,
    val periodEnd: LocalDate? = null,
    /** Expected amount at the moment the payment was registered. */
    val expectedAmount: Money? = null,
    val absenceId: Long? = null,
    val note: String = "",
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

@Serializable
enum class AccrualType {
    BONUS,
    ALLOWANCE,
    COMPENSATION,
    DEDUCTION,
    TAX,
    OTHER,
    ;

    /** Deductions and taxes reduce the accrued amount. */
    val isNegative: Boolean get() = this == DEDUCTION || this == TAX
}

/** Manually entered accrual or deduction (bonus, allowance, alimony, tax...). */
@Serializable
data class ManualAccrual(
    val id: Long = 0,
    val date: LocalDate,
    val type: AccrualType,
    val title: String,
    /** Always positive; the sign is defined by [type]. */
    val amount: Money,
    val note: String = "",
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
) {
    val signedAmount: Money get() = if (type.isNegative) -amount else amount
}

/** Earnings received before the app was used; needed for vacation and sick pay calculations. */
@Serializable
data class ExternalEarning(
    val id: Long = 0,
    val year: Int,
    /** 1..12, or null when the amount is for the whole year. */
    val month: Int? = null,
    val amount: Money,
    val note: String = "",
)
