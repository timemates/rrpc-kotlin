package com.y9vad9.rsproto.codegen.types

import com.squareup.wire.schema.ProtoType

internal object TypeDefaultValueTransformer {
    fun transform(incoming: ProtoType): String {
        return when (incoming) {
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