package io.github.ceniorpomidor.workcalendar.data.db

import androidx.room.TypeConverter
import io.github.ceniorpomidor.workcalendar.domain.model.AbsencePayCalculation
import io.github.ceniorpomidor.workcalendar.domain.model.SchedulePattern
import io.github.ceniorpomidor.workcalendar.domain.model.ShiftPay
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Room converters. Dates are stored as ISO text so that they sort and compare as strings. */
class Converters {
    @TypeConverter
    fun dateToString(value: LocalDate): String = value.toString()

    @TypeConverter
    fun stringToDate(value: String): LocalDate = LocalDate.parse(value)

    @TypeConverter
    fun dateTimeToString(value: LocalDateTime): String = value.format(DATE_TIME)

    @TypeConverter
    fun stringToDateTime(value: String): LocalDateTime = LocalDateTime.parse(value)

    @TypeConverter
    fun payToJson(value: ShiftPay): String = json.encodeToString(ShiftPay.serializer(), value)

    @TypeConverter
    fun jsonToPay(value: String): ShiftPay = json.decodeFromString(ShiftPay.serializer(), value)

    @TypeConverter
    fun patternToJson(value: SchedulePattern): String = json.encodeToString(SchedulePattern.serializer(), value)

    @TypeConverter
    fun jsonToPattern(value: String): SchedulePattern = json.decodeFromString(SchedulePattern.serializer(), value)

    @TypeConverter
    fun calculationToJson(value: AbsencePayCalculation): String = json.encodeToString(AbsencePayCalculation.serializer(), value)

    @TypeConverter
    fun jsonToCalculation(value: String): AbsencePayCalculation = json.decodeFromString(AbsencePayCalculation.serializer(), value)

    companion object {
        /** Fixed-width format keeps lexicographic order equal to chronological order. */
        val DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        val json: Json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        fun format(value: LocalDateTime): String = value.format(DATE_TIME)
    }
}
