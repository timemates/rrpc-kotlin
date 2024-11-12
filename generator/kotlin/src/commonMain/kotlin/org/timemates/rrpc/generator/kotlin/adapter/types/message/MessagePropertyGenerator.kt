package org.timemates.rrpc.generator.kotlin.adapter.types.message

import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import org.timemates.rrpc.codegen.typemodel.PoetAnnotations
import org.timemates.rrpc.common.schema.RSType

internal object MessagePropertyGenerator {
    fun generateProperties(incoming: RSType.Message, parameterTypes: List<TypeName>): List<PropertySpec> {
        return incoming.fields.mapIndexed { index, field ->
            PropertySpec.builder(field.name, parameterTypes[index])
                .initializer(field.name)
                .addKdoc(field.documentation?.replace("%", "%%").orEmpty())
                .addAnnotation(PoetAnnotations.ProtoNumber(field.tag))
                .apply {
                    if (field.isRepeated && field.typeUrl.isScalar) addAnnotation(PoetAnnotations.ProtoPacked)
                }
                .build()
        }
    }
}