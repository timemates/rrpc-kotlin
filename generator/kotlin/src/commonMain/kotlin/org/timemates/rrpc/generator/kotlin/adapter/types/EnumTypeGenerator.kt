package org.timemates.rrpc.generator.kotlin.adapter.types

import com.squareup.kotlinpoet.*
import org.timemates.rrpc.codegen.typemodel.PoetAnnotations
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.RSType

internal object EnumTypeGenerator {
    fun generateEnum(incoming: RSType.Enum, schema: RSResolver): TypeSpec {
        val nested = incoming.nestedTypes.mapNotNull { TypeGenerator.generateType(it, schema) }

        return TypeSpec.enumBuilder(incoming.name)
            .addAnnotation(PoetAnnotations.OptIn(LibClassNames.ExperimentalSerializationApi))
            .addAnnotation(PoetAnnotations.Serializable)
            .apply {
                incoming.constants.forEach { constant ->
                    addEnumConstant(
                        constant.name,
                        TypeSpec.anonymousClassBuilder()
                            .addKdoc(constant.documentation?.replace("%", "%%").orEmpty())
                            .addAnnotation(PoetAnnotations.ProtoNumber(constant.tag))
                            .build()
                    )
                }
            }
            .addTypes(nested.map(TypeGenerator.Result::typeSpec))
            .addType(
                TypeSpec.companionObjectBuilder()
                    .addSuperinterface(LibClassNames.ProtoTypeDefinition(ClassName("", incoming.name)))
                    .addProperty(
                        PropertySpec.builder("Default", ClassName("", incoming.name))
                            .addModifiers(KModifier.OVERRIDE)
                            .initializer(incoming.constants.first { it.tag == 0 }.name)
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("typeUrl", STRING)
                            .addModifiers(KModifier.OVERRIDE)
                            .initializer("%S", incoming.typeUrl)
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("definition", LibClassNames.ProtoTypeDefinition(STAR))
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