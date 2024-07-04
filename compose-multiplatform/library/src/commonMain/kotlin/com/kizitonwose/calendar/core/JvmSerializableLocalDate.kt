package com.kizitonwose.calendar.core

import kotlinx.datetime.LocalDate

internal data class JvmSerializableLocalDate(
    val year: Int,
    val monthNumber: Int,
    val dayOfMonth: Int,
) : JvmSerializable

internal fun JvmSerializableLocalDate.toLocalDate() = LocalDate(
    year = year,
    monthNumber = monthNumber,
    dayOfMonth = dayOfMonth,
)

internal fun LocalDate.toJvmSerializableLocalDate() = JvmSerializableLocalDate(
    year = year,
    monthNumber = monthNumber,
    dayOfMonth = dayOfMonth,
)
