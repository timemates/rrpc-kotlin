package org.timemates.rrpc.generator.kotlin.adapter.types.message

import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.RSType
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.asClassName
import org.timemates.rrpc.generator.kotlin.adapter.types.BuiltinsGenerator

internal object MessageParameterTypeGenerator {
    fun generateParameterTypes(incoming: RSType.Message, schema: RSResolver): List<TypeName> {
        return incoming.fields.map { field ->
            val fieldType = field.typeUrl
            when {
                fieldType.isScalar || fieldType.isWrapper || fieldType.isMap -> BuiltinsGenerator.generateBuiltin(
                    fieldType
                ).let {
                    if (field.isRepeated) LIST.parameterizedBy(it) else if (fieldType.isWrapper) it.copy(nullable = true) else it
                }

                else -> fieldType.asClassName(schema).let {
                    if (field.isRepeated) LIST.parameterizedBy(it) else it.copy(nullable = true)
                }
            }
        }
    }
}