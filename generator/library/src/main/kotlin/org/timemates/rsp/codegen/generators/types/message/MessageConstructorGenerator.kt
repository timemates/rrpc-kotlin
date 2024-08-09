package org.timemates.rsp.codegen.generators.types.message

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.wire.schema.MessageType
import org.timemates.rsp.codegen.generators.types.TypeDefaultValueGenerator

internal object MessageConstructorGenerator {
    fun generatePrimaryConstructor(incoming: MessageType, parameterTypes: List<TypeName>, oneOfs: List<OneOfGenerator.Result>): FunSpec {
        return FunSpec.constructorBuilder()
            .addModifiers(KModifier.PRIVATE)
            .addParameters(incoming.declaredFields.mapIndexed { index, field ->
                val type = parameterTypes[index]
                ParameterSpec.builder(field.name, type)
                    .defaultValue(if (type.isNullable) "null" else field.default ?: TypeDefaultValueGenerator.generateTypeDefault(
                        field
                    )
                    )
                    .build()
            })
            .addParameters(oneOfs.map {
                ParameterSpec.builder(it.property.name, it.property.type).defaultValue("null").build()
            })
            .build()
    }
}