@file:UseSerializers(LocalDateSerializer::class)

package io.github.ceniorpomidor.workcalendar.domain.model

import io.github.ceniorpomidor.workcalendar.domain.util.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate

/**
 * Hourly rate and surcharges effective from [effectiveFrom] until the next period starts.
 * Surcharges are additional percents of the hourly rate (e.g. 100 = double pay for holiday hours).
 */
@Serializable
data class RatePeriod(
    val id: Long = 0,
    val effectiveFrom: LocalDate,
    val hourlyRate: Money,
    val holidayBonusPercent: Int = 0,
    val overtimeBonusPercent: Int = 0,
    val extraShiftBonusPercent: Int = 0,
    val note: String = "",
)

/** Resolves the rate period effective on a date. */
class RateTable(periods: List<RatePeriod>) {
    val periods: List<RatePeriod> = periods.sortedWith(compareBy<RatePeriod> { it.effectiveFrom }.thenBy { it.id })

    val isEmpty: Boolean get() = periods.isEmpty()

    fun on(date: LocalDate): RatePeriod? = periods.lastOrNull { !it.effectiveFrom.isAfter(date) }

    /** Period that starts after the given one, if any. */
    fun next(period: RatePeriod): RatePeriod? = periods.firstOrNull { it.effectiveFrom.isAfter(period.effectiveFrom) }
}
