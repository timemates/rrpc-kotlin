package io.timemates.rsproto.codegen.types

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.wire.schema.ProtoType

internal object BuiltinsTransformer {
   fun transform(incoming: ProtoType): TypeName {
        return when (incoming) {
            ProtoType.BOOL -> BOOLEAN
            ProtoType.INT32, ProtoType.SINT32, ProtoType.FIXED32, ProtoType.SFIXED32 -> INT
            ProtoType.INT64, ProtoType.SINT64, ProtoType.FIXED64, ProtoType.SFIXED64 -> LONG
            ProtoType.BYTES -> BYTE_ARRAY
            ProtoType.FLOAT -> FLOAT
            ProtoType.UINT32 -> U_INT
            ProtoType.UINT64 -> U_LONG
            ProtoType.DOUBLE -> DOUBLE
            ProtoType.DURATION -> LONG // todo
            ProtoType.EMPTY -> UNIT
            ProtoType.STRING, ProtoType.TIMESTAMP -> STRING
            ProtoType.STRUCT_LIST ->
                LIST.parameterizedBy(ClassName(incoming.enclosingTypeOrPackage ?: "", incoming.simpleName))
            else -> ANY
        }
    }

}