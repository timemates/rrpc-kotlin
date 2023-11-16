package io.timemates.rsproto.codegen.types

import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.EnumType
import io.timemates.rsproto.codegen.Annotations

internal object EnumTypeTransformer {
    fun transform(incoming: EnumType): TypeSpec {
        return TypeSpec.enumBuilder(incoming.name)
            .addAnnotation(Annotations.Serializable)
            .apply {
                incoming.constants.forEach { constant ->
                    addEnumConstant(
                        constant.name,
                        TypeSpec.anonymousClassBuilder().addKdoc(constant.documentation)
                            .addAnnotation(Annotations.ProtoNumber(constant.tag))
                            .build()
                    )
                }
            }.addTypes(incoming.nestedTypes.map { io.timemates.rsproto.codegen.types.TypeTransformer.transform(it) })
            .build()
    }

}