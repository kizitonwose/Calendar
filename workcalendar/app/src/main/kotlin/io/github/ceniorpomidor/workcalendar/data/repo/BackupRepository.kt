package io.github.ceniorpomidor.workcalendar.data.repo

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import io.github.ceniorpomidor.workcalendar.BuildConfig
import io.github.ceniorpomidor.workcalendar.data.db.AppDatabase
import io.github.ceniorpomidor.workcalendar.data.db.toDomain
import io.github.ceniorpomidor.workcalendar.data.db.toEntity
import io.github.ceniorpomidor.workcalendar.domain.backup.BackupCodec
import io.github.ceniorpomidor.workcalendar.domain.backup.BackupException
import io.github.ceniorpomidor.workcalendar.domain.backup.BackupFile
import io.github.ceniorpomidor.workcalendar.domain.export.CsvExport
import io.github.ceniorpomidor.workcalendar.domain.export.IcsExport
import io.github.ceniorpomidor.workcalendar.domain.model.AppSettings
import io.github.ceniorpomidor.workcalendar.domain.model.ChangeCategory
import io.github.ceniorpomidor.workcalendar.domain.time.DateRange
import io.github.ceniorpomidor.workcalendar.domain.util.Formats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

enum class CsvKind(val title: String, val fileName: String) {
    SHIFTS("Смены", "shifts.csv"),
    ABSENCES("Отпуска и больничные", "absences.csv"),
    PAYMENTS("Полученные выплаты", "payments.csv"),
    ACCRUALS("Начисления и удержания", "accruals.csv"),
}

/** Local automatic copy of the data. */
data class LocalBackup(val file: File, val title: String)

class BackupRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val settings: SettingsRepository,
    private val calc: CalcRepository,
    private val log: ChangeLogger,
    private val changes: DataChanges,
    private val clock: AppClock,
) {
    private val localDir: File get() = File(context.filesDir, "backups").apply { mkdirs() }

    suspend fun createBackup(): BackupFile = withContext(Dispatchers.IO) {
        BackupFile(
            appVersion = BuildConfig.VERSION_NAME,
            createdAt = clock.now().toString(),
            settings = settings.get(),
            templates = db.scheduleDao().getTemplates().map { it.toDomain() },
            assignments = db.scheduleDao().getAssignments().map { it.toDomain() },
            shifts = db.shiftDao().getAll().map { it.toDomain() },
            rates = db.rateDao().getAll().map { it.toDomain() },
            absences = db.absenceDao().getAll().map { it.toDomain() },
            payoutRules = db.financeDao().getRules().map { it.toDomain() },
            payments = db.financeDao().getPayments().map { it.toDomain() },
            accruals = db.financeDao().getAccruals().map { it.toDomain() },
            notes = db.miscDao().getNotes().map { it.toDomain() },
            holidays = db.miscDao().getHolidays().map { it.toDomain() },
            externalEarnings = db.financeDao().getExternal().map { it.toDomain() },
            changeLog = db.miscDao().getChangeLog().map { it.toDomain() },
        )
    }

    suspend fun exportBackup(uri: Uri) {
        val backup = createBackup()
        write(uri, BackupCodec.encode(backup).toByteArray(Charsets.UTF_8))
        log.log(ChangeCategory.BACKUP, "Резервная копия создана", "${Formats.shifts(backup.shifts.size)}, ${backup.payments.size} выплат")
    }

    /** Reads and validates a backup file; nothing is changed yet. */
    suspend fun readBackup(uri: Uri): BackupFile = withContext(Dispatchers.IO) {
        val text = try {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                ?: throw BackupException("Не удалось открыть файл")
        } catch (e: IOException) {
            throw BackupException("Не удалось прочитать файл: ${e.message}", e)
        }
        BackupCodec.decode(text)
    }

    /** Replaces all data with the backup. A copy of the current data is saved first. */
    suspend fun restore(backup: BackupFile) {
        BackupCodec.validate(backup)
        saveLocalCopy("before-restore")
        withContext(Dispatchers.IO) {
            db.withTransaction {
                clearAllTables()
                db.scheduleDao().insertTemplates(backup.templates.map { it.toEntity() })
                db.scheduleDao().insertAssignments(backup.assignments.map { it.toEntity() })
                db.rateDao().insertAll(backup.rates.map { it.toEntity() })
                db.absenceDao().insertAll(backup.absences.map { it.toEntity() })
                db.shiftDao().insertAll(backup.shifts.map { it.toEntity() })
                db.financeDao().insertRules(backup.payoutRules.map { it.toEntity() })
                db.financeDao().insertPayments(backup.payments.map { it.toEntity() })
                db.financeDao().insertAccruals(backup.accruals.map { it.toEntity() })
                db.financeDao().insertExternalAll(backup.externalEarnings.map { it.toEntity() })
                db.miscDao().insertNotes(backup.notes.map { it.toEntity() })
                db.miscDao().insertHolidays(backup.holidays.map { it.toEntity() })
                db.miscDao().insertChangeLogAll(backup.changeLog.map { it.toEntity().copy(id = 0) })
            }
        }
        settings.replace(backup.settings.copy(onboardingDone = true))
        log.log(ChangeCategory.BACKUP, "Данные восстановлены", "Копия от ${backup.createdAt.take(16).replace('T', ' ')}: ${Formats.shifts(backup.shifts.size)}")
        changes.notifyChanged()
    }

    private suspend fun clearAllTables() {
        db.shiftDao().deleteAll()
        db.scheduleDao().deleteAllTemplates()
        db.scheduleDao().deleteAllAssignments()
        db.rateDao().deleteAll()
        db.absenceDao().deleteAll()
        db.financeDao().deleteAllRules()
        db.financeDao().deleteAllPayments()
        db.financeDao().deleteAllAccruals()
        db.financeDao().deleteAllExternal()
        db.miscDao().deleteAllNotes()
        db.miscDao().deleteAllHolidays()
        db.miscDao().deleteAllChangeLog()
        db.miscDao().deleteAllNotificationStates()
    }

    /** Deletes all user data (after a double confirmation in the UI). */
    suspend fun deleteAllData() {
        saveLocalCopy("before-delete")
        withContext(Dispatchers.IO) { db.withTransaction { clearAllTables() } }
        settings.replace(AppSettings())
        changes.notifyChanged()
    }

    /** Resets settings to defaults keeping the employment data and the data itself. */
    suspend fun resetSettings() {
        settings.update { old ->
            AppSettings(
                onboardingDone = old.onboardingDone,
                employmentStartDate = old.employmentStartDate,
                priorExperienceMonths = old.priorExperienceMonths,
                trackingStartDate = old.trackingStartDate,
            )
        }
        log.log(ChangeCategory.SETTINGS, "Настройки сброшены", "Данные календаря и финансов не изменены")
    }

    suspend fun exportCsv(uri: Uri, kind: CsvKind, range: DateRange?) {
        write(uri, csv(kind, range).toByteArray(Charsets.UTF_8))
    }

    /** All CSV tables in one ZIP archive. */
    suspend fun exportCsvZip(uri: Uri, range: DateRange?) {
        val entries = CsvKind.entries.map { it.fileName to csv(it, range) }
        withContext(Dispatchers.IO) {
            val stream = context.contentResolver.openOutputStream(uri, "wt") ?: throw IOException("Не удалось создать файл")
            ZipOutputStream(stream).use { zip ->
                for ((name, content) in entries) {
                    zip.putNextEntry(ZipEntry(name))
                    zip.write(content.toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                }
            }
        }
    }

    suspend fun exportIcs(uri: Uri, range: DateRange) {
        val shifts = db.shiftDao().getRange(range.start, range.endInclusive).map { it.toDomain() }
        write(uri, IcsExport.shifts(shifts, clock.now()).toByteArray(Charsets.UTF_8))
    }

    private suspend fun csv(kind: CsvKind, range: DateRange?): String = withContext(Dispatchers.IO) {
        val context = calc.current()
        when (kind) {
            CsvKind.SHIFTS -> {
                val shifts = if (range != null) db.shiftDao().getRange(range.start, range.endInclusive) else db.shiftDao().getAll()
                CsvExport.shifts(shifts.map { it.toDomain() }, context.pay)
            }
            CsvKind.ABSENCES -> CsvExport.absences(db.absenceDao().getAll().map { it.toDomain() }.filter { range == null || it.range.overlaps(range) })
            CsvKind.PAYMENTS -> CsvExport.payments(db.financeDao().getPayments().map { it.toDomain() }.filter { range == null || it.date in range })
            CsvKind.ACCRUALS -> CsvExport.accruals(db.financeDao().getAccruals().map { it.toDomain() }.filter { range == null || it.date in range })
        }
    }

    private suspend fun write(uri: Uri, bytes: ByteArray) = withContext(Dispatchers.IO) {
        val stream = context.contentResolver.openOutputStream(uri, "wt") ?: throw IOException("Не удалось создать файл")
        stream.use { it.write(bytes) }
    }

    /** Saves a copy into the app storage; keeps the latest [KEEP_LOCAL] copies. */
    suspend fun saveLocalCopy(reason: String): File = withContext(Dispatchers.IO) {
        val stamp = clock.now().format(STAMP_FORMAT)
        val file = File(localDir, "auto_${stamp}_$reason.json")
        file.writeText(BackupCodec.encode(createBackup()))
        localDir.listFiles()?.sortedByDescending { it.lastModified() }?.drop(KEEP_LOCAL)?.forEach { it.delete() }
        file
    }

    /** Weekly automatic copy in the app storage. */
    suspend fun autoBackupIfDue() {
        val newest = withContext(Dispatchers.IO) { localDir.listFiles()?.maxOfOrNull { it.lastModified() } }
        val weekMillis = 7L * 24 * 60 * 60 * 1000
        if (newest == null || System.currentTimeMillis() - newest > weekMillis) {
            if (db.shiftDao().lastTemplateDate() != null || db.financeDao().getPayments().isNotEmpty()) saveLocalCopy("weekly")
        }
    }

    fun localCopies(): List<LocalBackup> = localDir.listFiles()
        ?.filter { it.name.endsWith(".json") }
        ?.sortedByDescending { it.lastModified() }
        ?.map { file ->
            val reason = when {
                file.name.contains("before-restore") -> "перед восстановлением"
                file.name.contains("before-delete") -> "перед удалением данных"
                else -> "автоматическая"
            }
            val stamp = runCatching {
                java.time.LocalDateTime.parse(file.name.substring(5, 24), STAMP_FORMAT)
            }.getOrNull()
            LocalBackup(file, "${stamp?.let { Formats.dateTime(it) } ?: file.name} — $reason")
        }
        .orEmpty()

    suspend fun readLocal(file: File): BackupFile = withContext(Dispatchers.IO) { BackupCodec.decode(file.readText()) }

    companion object {
        const val KEEP_LOCAL: Int = 8
        private val STAMP_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
    }
}
