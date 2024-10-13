package org.timemates.rrpc.generator.kotlin

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.timemates.rrpc.common.metadata.RMField
import org.timemates.rrpc.common.metadata.RMResolver
import org.timemates.rrpc.common.metadata.value.RMTypeUrl
import org.timemates.rrpc.generator.kotlin.ext.asClassName

public object OptionGenerator {
    public fun generateOption(field: RMField, type: ClassName, resolver: RMResolver): PropertySpec =
        PropertySpec.builder(field.name, type.parameterizedBy(toKotlinTypeName(field.typeUrl, resolver)))
            .addKdoc(field.documentation?.replace("%", "%%").orEmpty())
            .receiver(type.nestedClass("Companion"))
            .delegate("lazy·{·%T(%S, %L)·}", type, field.name, field.tag)
            .build()

    private fun toKotlinTypeName(type: RMTypeUrl, resolver: RMResolver): TypeName {
        return when (type) {
            RMTypeUrl.STRING, RMTypeUrl.STRING_VALUE -> return STRING
            RMTypeUrl.DOUBLE, RMTypeUrl.DOUBLE_VALUE -> return DOUBLE
            RMTypeUrl.BYTES, RMTypeUrl.BYTES_VALUE -> return BYTE_ARRAY
            RMTypeUrl.BOOL, RMTypeUrl.BOOL_VALUE -> return BOOLEAN
            RMTypeUrl.UINT64, RMTypeUrl.UINT64_VALUE -> return U_LONG
            RMTypeUrl.UINT32, RMTypeUrl.UINT32_VALUE -> return U_INT
            RMTypeUrl.INT32, RMTypeUrl.INT32_VALUE, RMTypeUrl.SFIXED32, RMTypeUrl.FIXED32 -> return INT
            RMTypeUrl.INT64, RMTypeUrl.INT64_VALUE, RMTypeUrl.SFIXED64, RMTypeUrl.FIXED64 -> LONG
            RMTypeUrl.FLOAT, RMTypeUrl.FLOAT_VALUE -> return FLOAT
            else -> type.asClassName(resolver)
        }
    }
}