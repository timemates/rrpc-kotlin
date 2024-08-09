package org.timemates.rsp.codegen.generators.types.message

import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.wire.schema.MessageType
import com.squareup.wire.schema.Schema
import org.timemates.rsp.codegen.ext.asClassName
import org.timemates.rsp.codegen.generators.types.BuiltinsGenerator

internal object MessageParameterTypeGenerator {
    fun generateParameterTypes(incoming: MessageType, schema: Schema): List<TypeName> {
        return incoming.declaredFields.map { field ->
            val fieldType = field.type!!
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