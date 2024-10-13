package org.timemates.rrpc.generator.kotlin.types

import com.squareup.kotlinpoet.*
import org.timemates.rrpc.common.metadata.value.RMTypeUrl

internal object BuiltinsGenerator {
    fun generateBuiltin(incoming: RMTypeUrl): TypeName {
        return when (incoming) {
            RMTypeUrl.BOOL -> BOOLEAN
            RMTypeUrl.INT32, RMTypeUrl.SINT32, RMTypeUrl.FIXED32, RMTypeUrl.SFIXED32, RMTypeUrl.INT32_VALUE -> INT
            RMTypeUrl.INT64, RMTypeUrl.SINT64, RMTypeUrl.FIXED64, RMTypeUrl.SFIXED64, RMTypeUrl.INT64_VALUE -> LONG
            RMTypeUrl.BYTES, RMTypeUrl.BYTES_VALUE -> BYTE_ARRAY
            RMTypeUrl.FLOAT, RMTypeUrl.FLOAT_VALUE -> FLOAT
            RMTypeUrl.UINT32, RMTypeUrl.UINT32_VALUE -> U_INT
            RMTypeUrl.UINT64, RMTypeUrl.UINT64_VALUE -> U_LONG
            RMTypeUrl.DOUBLE, RMTypeUrl.DOUBLE_VALUE -> DOUBLE
            RMTypeUrl.STRING -> STRING
            else -> error("Unsupported protobuf type $incoming")
        }
    }

}