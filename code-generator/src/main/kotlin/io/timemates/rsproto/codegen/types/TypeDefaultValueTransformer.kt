package io.timemates.rsproto.codegen.types

import com.squareup.wire.schema.Field
import com.squareup.wire.schema.ProtoType

internal object TypeDefaultValueTransformer {
    fun transform(field: Field): String {
        val type = field.type!!

        if(field.isRepeated)
            return "emptyList()"

        return when (type) {
            ProtoType.INT32,
            ProtoType.INT64,
            ProtoType.DURATION,
            ProtoType.FIXED32,
            ProtoType.FIXED64,
            ProtoType.SFIXED32,
            ProtoType.SFIXED64,
            ProtoType.SINT32,
            ProtoType.SINT64 -> "0"

            ProtoType.UINT32, ProtoType.UINT64 -> "0u"
            ProtoType.STRING -> "\"\""
            ProtoType.BOOL -> "false"
            ProtoType.BYTES -> "byteArrayOf()"
            ProtoType.DOUBLE -> "0.0"
            ProtoType.FLOAT -> "0.0f"
            ProtoType.STRUCT_LIST -> "emptyList()"
            ProtoType.STRUCT_MAP -> "emptyMap()"
            ProtoType.TIMESTAMP -> ""
            else -> "null"
        }
    }

}