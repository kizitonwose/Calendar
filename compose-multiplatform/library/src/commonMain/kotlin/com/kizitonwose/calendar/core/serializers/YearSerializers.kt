package com.kizitonwose.calendar.core.serializers

import com.kizitonwose.calendar.core.Year
import com.kizitonwose.calendar.core.format.fromIso8601Year
import com.kizitonwose.calendar.core.format.toIso8601String
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

/**
 * A serializer for [Year] that uses the ISO 8601 representation.
 *
 * JSON example: `"2020"`
 *
 * @see Year.toString
 */
public object YearIso8601Serializer : KSerializer<Year> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.kizitonwose.calendar.core.Year", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Year =
        decoder.decodeString().fromIso8601Year()

    override fun serialize(encoder: Encoder, value: Year) {
        encoder.encodeString(value.toIso8601String())
    }
}

/**
 * A serializer for [Year] that represents a value as its components.
 *
 * JSON example: `{"year":2020}`
 */
public object YearComponentSerializer : KSerializer<Year> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("com.kizitonwose.calendar.core.Year") {
            element<Int>("year")
        }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): Year =
        decoder.decodeStructure(descriptor) {
            var year: Int? = null
            loop@ while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> year = decodeIntElement(descriptor, 0)
                    CompositeDecoder.DECODE_DONE -> break@loop // https://youtrack.jetbrains.com/issue/KT-42262
                    else -> throw SerializationException("Unexpected index: $index")
                }
            }
            if (year == null) throw MissingFieldException(missingField = "year", serialName = descriptor.serialName)
            Year(year)
        }

    override fun serialize(encoder: Encoder, value: Year) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.value)
        }
    }
}
