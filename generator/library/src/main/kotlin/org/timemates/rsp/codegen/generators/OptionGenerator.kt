package org.timemates.rsp.codegen.generators

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.wire.schema.Field
import com.squareup.wire.schema.ProtoType
import com.squareup.wire.schema.Schema
import org.timemates.rsp.codegen.ext.asClassName

public object OptionGenerator {
    public fun generateOption(field: Field, type: ClassName, schema: Schema): PropertySpec =
        PropertySpec.builder(field.name, type.parameterizedBy(toKotlinTypeName(field.type!!, schema)))
            .addKdoc(field.documentation.replace("%", "%%"))
            .receiver(type.nestedClass("Companion"))
            .delegate("lazy·{·%T(%S, %L)·}", type, field.name, field.tag)
            .build()

    private fun toKotlinTypeName(type: ProtoType, schema: Schema): TypeName {
        return when (type) {
            ProtoType.STRING, ProtoType.STRING_VALUE -> return STRING
            ProtoType.DOUBLE, ProtoType.DOUBLE_VALUE -> return DOUBLE
            ProtoType.BYTES, ProtoType.BYTES_VALUE -> return BYTE_ARRAY
            ProtoType.BOOL, ProtoType.BOOL_VALUE -> return BOOLEAN
            ProtoType.UINT64, ProtoType.UINT64_VALUE -> return U_LONG
            ProtoType.UINT32, ProtoType.UINT32_VALUE -> return U_INT
            ProtoType.INT32, ProtoType.INT32_VALUE, ProtoType.SFIXED32, ProtoType.FIXED32 -> return INT
            ProtoType.INT64, ProtoType.INT64_VALUE, ProtoType.SFIXED64, ProtoType.FIXED64 -> LONG
            ProtoType.FLOAT, ProtoType.FLOAT_VALUE -> return FLOAT
            else -> type.asClassName(schema)
        }
    }
}