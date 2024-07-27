package com.kizitonwose.calendar.core

import com.kizitonwose.calendar.core.serializers.YearComponentSerializer
import com.kizitonwose.calendar.core.serializers.YearIso8601Serializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class YearSerializationTest {
    private fun iso8601Serialization(serializer: KSerializer<Year>) {
        for ((localDate, json) in listOf(
            Pair(Year(2020), "\"2020\""),
            Pair(Year(-2020), "\"-2020\""),
            Pair(Year(2019), "\"2019\""),
        )) {
            assertEquals(json, Json.encodeToString(serializer, localDate))
            assertEquals(localDate, Json.decodeFromString(serializer, json))
        }
    }

    private fun componentSerialization(serializer: KSerializer<Year>) {
        for ((localDate, json) in listOf(
            Pair(Year(2020), "{\"year\":2020}"),
            Pair(Year(-2020), "{\"year\":-2020}"),
            Pair(Year(2019), "{\"year\":2019}"),
        )) {
            assertEquals(json, Json.encodeToString(serializer, localDate))
            assertEquals(localDate, Json.decodeFromString(serializer, json))
        }
        // all components must be present
        assertFailsWith<SerializationException> {
            Json.decodeFromString(serializer, "{}")
        }
        // invalid values must fail to construct
        assertFailsWith<IllegalArgumentException> {
            Json.decodeFromString(serializer, "{\"year\":1000000000000}")
        }
    }

    @Test
    fun testIso8601Serialization() {
        iso8601Serialization(YearIso8601Serializer)
    }

    @Test
    fun testComponentSerialization() {
        componentSerialization(YearComponentSerializer)
    }

    @Test
    fun testDefaultSerializers() {
        // should be the same as the ISO 8601
        iso8601Serialization(Json.serializersModule.serializer())
    }
}
