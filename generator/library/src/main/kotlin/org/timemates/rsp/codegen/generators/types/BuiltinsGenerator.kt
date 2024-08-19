package org.timemates.rsp.codegen.generators.types

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.wire.schema.ProtoType

internal object BuiltinsGenerator {
   fun generateBuiltin(incoming: ProtoType): TypeName {
        return when (incoming) {
            ProtoType.BOOL -> BOOLEAN
            ProtoType.INT32, ProtoType.SINT32, ProtoType.FIXED32, ProtoType.SFIXED32, ProtoType.INT32_VALUE -> INT
            ProtoType.INT64, ProtoType.SINT64, ProtoType.FIXED64, ProtoType.SFIXED64, ProtoType.INT64_VALUE -> LONG
            ProtoType.BYTES, ProtoType.BYTES_VALUE -> BYTE_ARRAY
            ProtoType.FLOAT, ProtoType.FLOAT_VALUE -> FLOAT
            ProtoType.UINT32, ProtoType.UINT32_VALUE -> U_INT
            ProtoType.UINT64, ProtoType.UINT64_VALUE -> U_LONG
            ProtoType.DOUBLE, ProtoType.DOUBLE_VALUE -> DOUBLE
            ProtoType.STRING -> STRING
            else -> error("Unsupported protobuf type $incoming")
        }
    }

}