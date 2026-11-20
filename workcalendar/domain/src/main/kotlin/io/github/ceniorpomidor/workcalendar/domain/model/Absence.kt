@file:UseSerializers(LocalDateSerializer::class, LocalDateTimeSerializer::class)

package io.github.ceniorpomidor.workcalendar.domain.model

import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.util.LocalDateSerializer
import io.github.ceniorpomidor.workcalendar.domain.util.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
enum class AbsenceType {
    VACATION,
    SICK,
    UNPAID,
    OTHER,
}

/** Saved result of an automatic vacation or sick pay calculation with an explanation. */
@Serializable
data class AbsencePayCalculation(
    val method: AbsencePayMethod,
    val total: Money,
    /** Sick pay part paid by the employer (first days). */
    val employerPart: Money? = null,
    /** Sick pay part paid by the social fund. */
    val fundPart: Money? = null,
    val averageDaily: Money? = null,
    val percent: Int? = null,
    /** Days the payment is calculated for. */
    val days: Int = 0,
    val explanation: List<String> = emptyList(),
    /** The calculation used incomplete data; the amount is a rough estimate. */
    val insufficientData: Boolean = false,
    val warnings: List<String> = emptyList(),
    val calculatedAt: LocalDateTime? = null,
)

@Serializable
enum class AbsencePayMethod {
    /** Pay for the planned shifts that fall into the vacation. */
    PLANNED_SHIFTS,

    /** Average daily earnings for 12 months (art. 139 of the Russian Labour Code). */
    AVERAGE_EARNINGS,

    /** Temporary disability benefit (Federal law 255-FZ). */
    SICK_BENEFIT,

    /** Entered manually. */
    MANUAL,
}

@Serializable
data class Absence(
    val id: Long = 0,
    val type: AbsenceType,
    /** Custom name, mostly for [AbsenceType.OTHER]. */
    val title: String = "",
    val startDate: LocalDate,
    val endDate: LocalDate,
    val paid: Boolean = true,
    /** Amount entered by the user; takes priority over the calculation. */
    val manualAmount: Money? = null,
    val calculation: AbsencePayCalculation? = null,
    /** Expected date of the (main) payment; null = estimated automatically. */
    val paymentDate: LocalDate? = null,
    /** Sick leave: expected date of the employer's part; null = estimated automatically. */
    val employerPaymentDate: LocalDate? = null,
    val note: String = "",
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
) {
    init {
        require(!endDate.isBefore(startDate)) { "Absence end $endDate is before start $startDate" }
    }

    val range: DateRange get() = DateRange(startDate, endDate)

    val calendarDays: Int get() = range.days

    /** Amount to account for: manual amount, otherwise the saved calculation. Zero for unpaid absences. */
    val amount: Money?
        get() = when {
            !paid -> Money.ZERO
            manualAmount != null -> manualAmount
            else -> calculation?.total
        }

    fun displayTitle(): String = title.ifBlank { defaultTitle(type) }

    companion object {
        fun defaultTitle(type: AbsenceType): String = when (type) {
            AbsenceType.VACATION -> "Отпуск"
            AbsenceType.SICK -> "Больничный"
            AbsenceType.UNPAID -> "Отпуск без сохранения з/п"
            AbsenceType.OTHER -> "Отсутствие"
        }
    }
}
