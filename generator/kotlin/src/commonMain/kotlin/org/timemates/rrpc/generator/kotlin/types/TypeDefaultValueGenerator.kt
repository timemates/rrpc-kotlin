package org.timemates.rrpc.generator.kotlin.types

import org.timemates.rrpc.common.metadata.RMField
import org.timemates.rrpc.common.metadata.value.RMTypeUrl

internal object TypeDefaultValueGenerator {
    fun generateTypeDefault(field: RMField): String {
        if (field.isRepeated)
            return "emptyList()"

        return when (field.typeUrl) {
            RMTypeUrl.INT32,
            RMTypeUrl.INT64,
            RMTypeUrl.DURATION,
            RMTypeUrl.FIXED32,
            RMTypeUrl.FIXED64,
            RMTypeUrl.SFIXED32,
            RMTypeUrl.SFIXED64,
            RMTypeUrl.SINT32,
            RMTypeUrl.SINT64,
                -> "0"

            RMTypeUrl.UINT32, RMTypeUrl.UINT64 -> "0u"
            RMTypeUrl.STRING -> "\"\""
            RMTypeUrl.BOOL -> "false"
            RMTypeUrl.BYTES -> "byteArrayOf()"
            RMTypeUrl.DOUBLE -> "0.0"
            RMTypeUrl.FLOAT -> "0.0f"
            else -> "null"
        }
    }

}