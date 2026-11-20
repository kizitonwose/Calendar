@file:UseSerializers(LocalDateSerializer::class, LocalDateTimeSerializer::class)

package io.github.ceniorpomidor.workcalendar.domain.model

import io.github.ceniorpomidor.workcalendar.domain.util.LocalDateSerializer
import io.github.ceniorpomidor.workcalendar.domain.util.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate
import java.time.LocalDateTime

/** Free text note attached to a calendar day. */
@Serializable
data class DayNote(
    val date: LocalDate,
    val text: String,
    val updatedAt: LocalDateTime? = null,
)

@Serializable
enum class HolidayOverrideType {
    /** Additional day off (e.g. a transferred holiday). */
    DAY_OFF,

    /** Additional working day (e.g. a working Saturday). */
    WORKDAY,

    /** Additional public holiday that is paid as a holiday. */
    HOLIDAY,
}

@Serializable
data class HolidayOverride(
    val date: LocalDate,
    val type: HolidayOverrideType,
    val title: String = "",
)

@Serializable
enum class ChangeCategory {
    SHIFT,
    SCHEDULE,
    RATE,
    ABSENCE,
    PAYMENT,
    ACCRUAL,
    SETTINGS,
    BACKUP,
}

/** Entry of the change history; explains how earlier amounts were obtained. */
@Serializable
data class ChangeLogEntry(
    val id: Long = 0,
    val timestamp: LocalDateTime,
    val category: ChangeCategory,
    val action: String,
    val description: String,
    val entityId: Long? = null,
)
