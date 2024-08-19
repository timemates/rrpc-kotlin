package org.timemates.rsp.codegen.generators.types

import com.squareup.kotlinpoet.*
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
                    .addSuperinterface(Types.ProtoTypeDefinition(ClassName("", incoming.name)))
                    .addProperty(
                        PropertySpec.builder("Default", ClassName("", incoming.name))
                            .addModifiers(KModifier.OVERRIDE)
                            .initializer(incoming.constants.first { it.tag == 0 }.name)
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("typeUrl", STRING)
                            .addModifiers(KModifier.OVERRIDE)
                            .initializer("%S", incoming.type.typeUrl)
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("definition", Types.ProtoTypeDefinition(STAR))
                            .addModifiers(KModifier.OVERRIDE)
                            .getter(
                                FunSpec.getterBuilder()
                                    .addCode("return Companion")
                                    .build()
                            )
                            .build()
                    )
                    .addFunctions(nested.mapNotNull(TypeGenerator.Result::constructorFun))
                    .build()
            )
            .build()
    }
}