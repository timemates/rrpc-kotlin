package org.timemates.rsp.codegen.generators.types.message

import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.wire.schema.MessageType
import org.timemates.rsp.codegen.typemodel.Annotations

internal object MessagePropertyGenerator {
    fun generateProperties(incoming: MessageType, parameterTypes: List<TypeName>): List<PropertySpec> {
        return incoming.declaredFields.mapIndexed { index, field ->
            PropertySpec.builder(field.name, parameterTypes[index])
                .initializer(field.name)
                .addKdoc(field.documentation.replace("%", "%%"))
                .addAnnotation(Annotations.ProtoNumber(field.tag))
                .apply {
                    if (field.isRepeated) addAnnotation(Annotations.ProtoPacked)
                }
                .build()
        }
    }
}