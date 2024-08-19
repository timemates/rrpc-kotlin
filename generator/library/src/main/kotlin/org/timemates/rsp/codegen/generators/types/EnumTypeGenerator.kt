package org.timemates.rsp.codegen.generators.types

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.EnumType
import com.squareup.wire.schema.Schema
import org.timemates.rsp.codegen.typemodel.Annotations
import org.timemates.rsp.codegen.typemodel.Types

internal object EnumTypeGenerator {
    fun generateEnum(incoming: EnumType, schema: Schema): TypeSpec {
        val nested = incoming.nestedTypes.map { TypeGenerator.generateType(it, schema) }

        return TypeSpec.enumBuilder(incoming.name)
            .addAnnotation(Annotations.OptIn(Types.ExperimentalSerializationApi))
            .addAnnotation(Annotations.Serializable)
            .apply {
                incoming.constants.forEach { constant ->
                    addEnumConstant(
                        constant.name,
                        TypeSpec.anonymousClassBuilder().addKdoc(constant.documentation.replace("%", "%%"))
                            .addAnnotation(Annotations.ProtoNumber(constant.tag))
                            .build()
                    )
                }
            }
            .addTypes(nested.map(TypeGenerator.Result::typeSpec))
            .addType(
                TypeSpec.companionObjectBuilder()
                    .addProperty(
                        PropertySpec.builder("Default", ClassName("", incoming.name))
                            .initializer(incoming.constants.first { it.tag == 0 }.name)
                            .build()
                    )
                    .addFunctions(nested.mapNotNull(TypeGenerator.Result::constructorFun))
                    .build()
            )
            .build()
    }
}