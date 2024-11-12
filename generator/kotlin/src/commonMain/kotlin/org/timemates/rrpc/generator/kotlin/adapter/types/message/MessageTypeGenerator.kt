package org.timemates.rrpc.generator.kotlin.adapter.types.message

import com.squareup.kotlinpoet.*
import org.timemates.rrpc.codegen.typemodel.PoetAnnotations
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.RSType
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.asClassName
import org.timemates.rrpc.generator.kotlin.adapter.types.TypeGenerator

internal object MessageTypeGenerator {
    data class Result(val type: TypeSpec, val constructorFun: FunSpec?)

    fun generateMessage(incoming: RSType.Message, resolver: RSResolver): Result {
        val parameterTypes = MessageParameterTypeGenerator.generateParameterTypes(incoming, resolver)
        val oneOfs = incoming.oneOfs.map {
            OneOfGenerator.generateOneOf(it, resolver)
        }
        val properties = MessagePropertyGenerator.generateProperties(incoming, parameterTypes)
        val oneOfProperties = oneOfs.map { it.property }
        val className = incoming.typeUrl.asClassName(resolver)
        val nested = MessageNestedTypeGenerator.generateNestedTypes(incoming, resolver)

        val generateCreateFun = incoming.fields.isNotEmpty() || incoming.oneOfs.isNotEmpty()

        val typeSpec = TypeSpec.classBuilder(className)
            .addSuperinterface(LibClassNames.ProtoType)
            .addAnnotation(PoetAnnotations.OptIn(LibClassNames.ExperimentalSerializationApi))
            .addKdoc(incoming.documentation?.replace("%", "%%").orEmpty())
            .addAnnotation(PoetAnnotations.Serializable)
            .primaryConstructor(
                MessageConstructorGenerator.generatePrimaryConstructor(
                    incoming,
                    parameterTypes,
                    oneOfs
                )
            )
            .addType(
                MessageCompanionObjectGenerator.generateCompanionObject(
                    className, nested, generateCreateFun, incoming.typeUrl,
                )
            )
            .addTypes(nested.map(TypeGenerator.Result::typeSpec))
            .apply {
                if (generateCreateFun) {
                    addType(
                        MessageDSLBuilderGenerator.generateMessageBuilder(
                            incoming.name,
                            properties.mapIndexed { index, it -> it to incoming.fields[index] },
                            oneOfProperties
                        )
                    )
                }
            }
            .addProperties(properties)
            .addProperties(oneOfProperties)
            .addProperty(
                PropertySpec.builder("definition", LibClassNames.ProtoTypeDefinition(ClassName("", incoming.name)))
                    .addModifiers(KModifier.OVERRIDE)
                    .getter(
                        FunSpec.getterBuilder()
                            .addCode("return Companion")
                            .build()
                    )
                    .build()
            )
            .addTypes(oneOfs.map(OneOfGenerator.Result::oneOfClass))
            .build()

        val constructorFun = if (generateCreateFun) {
            FunSpec.builder(typeSpec.name!!)
                .addParameter(
                    "builder",
                    LambdaTypeName.get(
                        receiver = className.nestedClass("DSLBuilder"),
                        returnType = UNIT
                    )
                )
                .addCode("return ${typeSpec.name}.create(builder)")
                .returns(className)
                .build()
        } else null

        return Result(type = typeSpec, constructorFun = constructorFun)
    }
}

