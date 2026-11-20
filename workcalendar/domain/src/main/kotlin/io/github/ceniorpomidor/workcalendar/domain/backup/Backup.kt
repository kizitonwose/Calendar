package io.github.ceniorpomidor.workcalendar.domain.backup

import io.github.ceniorpomidor.workcalendar.domain.model.Absence
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.domain.model.ChangeLogEntry
import io.github.ceniorpomidor.workcalendar.domain.model.DayNote
import io.github.ceniorpomidor.workcalendar.domain.model.ExternalEarning
import io.github.ceniorpomidor.workcalendar.domain.model.HolidayOverride
import io.github.ceniorpomidor.workcalendar.domain.model.ManualAccrual
import io.github.ceniorpomidor.workcalendar.domain.model.Payment
import io.github.ceniorpomidor.workcalendar.domain.model.PayoutRule
import io.github.ceniorpomidor.workcalendar.domain.model.RatePeriod
import io.github.ceniorpomidor.workcalendar.domain.model.ScheduleAssignment
import io.github.ceniorpomidor.workcalendar.domain.model.ScheduleTemplate
import io.github.ceniorpomidor.workcalendar.domain.model.Shift
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftOrigin
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Full copy of the user data. */
@Serializable
data class BackupFile(
    val format: String = FORMAT,
    val version: Int = VERSION,
    val appVersion: String = "",
    val createdAt: String = "",
    val settings: AppSettings = AppSettings(),
    val templates: List<ScheduleTemplate> = emptyList(),
    val assignments: List<ScheduleAssignment> = emptyList(),
    val shifts: List<Shift> = emptyList(),
    val rates: List<RatePeriod> = emptyList(),
    val absences: List<Absence> = emptyList(),
    val payoutRules: List<PayoutRule> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val accruals: List<ManualAccrual> = emptyList(),
    val notes: List<DayNote> = emptyList(),
    val holidays: List<HolidayOverride> = emptyList(),
    val externalEarnings: List<ExternalEarning> = emptyList(),
    val changeLog: List<ChangeLogEntry> = emptyList(),
) {
    companion object {
        const val FORMAT: String = "workcalendar-backup"
        const val VERSION: Int = 1
    }
}

class BackupException(message: String, cause: Throwable? = null) : Exception(message, cause)

object BackupCodec {
    val json: Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    fun encode(backup: BackupFile): String = json.encodeToString(BackupFile.serializer(), backup)

    /** Parses and validates a backup; throws [BackupException] with a user-readable message. */
    fun decode(text: String): BackupFile {
        val backup = try {
            json.decodeFromString(BackupFile.serializer(), text)
        } catch (e: SerializationException) {
            throw BackupException("Файл повреждён или не является резервной копией приложения", e)
        } catch (e: IllegalArgumentException) {
            throw BackupException("Файл содержит некорректные данные: ${e.message}", e)
        }
        validate(backup)
        return backup
    }

    fun validate(backup: BackupFile) {
        if (backup.format != BackupFile.FORMAT) throw BackupException("Это не резервная копия «Рабочего календаря»")
        if (backup.version > BackupFile.VERSION) {
            throw BackupException("Резервная копия создана более новой версией приложения (формат ${backup.version}). Обновите приложение")
        }
        val shiftIds = HashSet<Long>()
        for (shift in backup.shifts) {
            if (shift.id != 0L && !shiftIds.add(shift.id)) throw BackupException("Повторяющийся идентификатор смены ${shift.id}")
            if (shift.plannedEnd.isBefore(shift.plannedStart)) throw BackupException("Смена ${shift.date}: окончание раньше начала")
        }
        val templateDates = backup.shifts.filter { it.origin == ShiftOrigin.TEMPLATE }.groupBy { it.date }
        val duplicated = templateDates.filterValues { it.size > 1 }.keys
        if (duplicated.isNotEmpty()) throw BackupException("Повторяющиеся смены по графику: ${duplicated.sorted().take(3).joinToString()}")
        val absenceIds = backup.absences.map { it.id }.toSet()
        for (shift in backup.shifts) {
            if (shift.status == ShiftStatus.COVERED && shift.absenceId != null && shift.absenceId !in absenceIds) {
                throw BackupException("Смена ${shift.date} ссылается на несуществующее отсутствие")
            }
        }
    }
}
