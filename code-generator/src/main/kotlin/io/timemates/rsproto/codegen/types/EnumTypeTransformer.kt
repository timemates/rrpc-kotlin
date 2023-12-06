package io.timemates.rsproto.codegen.types

import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.EnumType
import com.squareup.wire.schema.Schema
import io.timemates.rsproto.codegen.Annotations
import io.timemates.rsproto.codegen.Types

internal object EnumTypeTransformer {
    fun transform(incoming: EnumType, schema: Schema): TypeSpec {
        return TypeSpec.enumBuilder(incoming.name)
            .addAnnotation(Annotations.OptIn(Types.experimentalSerializationApi))
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
            }.addTypes(incoming.nestedTypes.map { TypeTransformer.transform(it, schema) })
            .build()
    }

}