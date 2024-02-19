package org.timemates.rsproto.codegen.types

import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.EnumType
import com.squareup.wire.schema.Schema
import org.timemates.rsproto.codegen.Annotations
import org.timemates.rsproto.codegen.Types

internal object EnumTypeTransformer {
    fun transform(incoming: EnumType, schema: Schema): TypeSpec {
        val nested = incoming.nestedTypes.map { TypeTransformer.transform(it, schema) }

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
            }
            .addTypes(nested.map(TypeTransformer.Result::typeSpec))
            .addType(
                TypeSpec.companionObjectBuilder()
                    .addFunctions(nested.mapNotNull(TypeTransformer.Result::constructorFun))
                    .build()
            )
            .build()
    }
}