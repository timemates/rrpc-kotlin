package org.timemates.rrpc.generator.kotlin.adapter.types

import com.squareup.kotlinpoet.*
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.value.RMDeclarationUrl

internal object BuiltinsGenerator {
    fun generateBuiltin(incoming: RMDeclarationUrl): TypeName {
        return when (incoming) {
            RMDeclarationUrl.STRING -> STRING
            RMDeclarationUrl.BOOL -> BOOLEAN
            RMDeclarationUrl.INT32, RMDeclarationUrl.SINT32, RMDeclarationUrl.FIXED32, RMDeclarationUrl.SFIXED32 -> INT
            RMDeclarationUrl.INT64, RMDeclarationUrl.SINT64, RMDeclarationUrl.FIXED64, RMDeclarationUrl.SFIXED64 -> LONG
            RMDeclarationUrl.BYTES -> BYTE_ARRAY
            RMDeclarationUrl.FLOAT -> FLOAT
            RMDeclarationUrl.UINT32 -> U_INT
            RMDeclarationUrl.UINT64 -> U_LONG
            RMDeclarationUrl.DOUBLE -> DOUBLE
            RMDeclarationUrl.STRING_VALUE -> LibClassNames.Wrappers.STRING_VALUE
            RMDeclarationUrl.INT32_VALUE -> LibClassNames.Wrappers.INT32_VALUE
            RMDeclarationUrl.INT64_VALUE -> LibClassNames.Wrappers.INT64_VALUE
            RMDeclarationUrl.FLOAT_VALUE -> LibClassNames.Wrappers.FLOAT_VALUE
            RMDeclarationUrl.DOUBLE_VALUE -> LibClassNames.Wrappers.DOUBLE_VALUE
            RMDeclarationUrl.UINT32_VALUE -> LibClassNames.Wrappers.UINT32_VALUE
            RMDeclarationUrl.UINT64_VALUE -> LibClassNames.Wrappers.UINT64_VALUE
            RMDeclarationUrl.BOOL_VALUE -> LibClassNames.Wrappers.BOOL_VALUE
            RMDeclarationUrl.BYTES_VALUE -> LibClassNames.Wrappers.BYTES_VALUE
            else -> error("Unsupported protobuf type $incoming")
        }
    }

}