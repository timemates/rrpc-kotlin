package org.timemates.rsp.codegen.generators.types.message

import com.squareup.kotlinpoet.*
import com.squareup.wire.schema.MessageType
import com.squareup.wire.schema.Schema
import org.timemates.rsp.codegen.typemodel.Annotations
import org.timemates.rsp.codegen.typemodel.Types
import org.timemates.rsp.codegen.ext.asClassName
import org.timemates.rsp.codegen.generators.types.TypeGenerator

internal object MessageTypeGenerator {
    data class Result(val type: TypeSpec, val constructorFun: FunSpec?)

    fun generateMessage(incoming: MessageType, schema: Schema): Result {
        val parameterTypes = MessageParameterTypeGenerator.generateParameterTypes(incoming, schema)
        val oneOfs = incoming.oneOfs.map {
            OneOfGenerator.generateOneOf(it, schema)
        }
        val properties = MessagePropertyGenerator.generateProperties(incoming, parameterTypes)
        val oneOfProperties = oneOfs.map { it.property }
        val className = incoming.type.asClassName(schema)
        val nested = MessageNestedTypeGenerator.generateNestedTypes(incoming, schema)

        val generateCreateFun = incoming.fields.isNotEmpty() || incoming.oneOfs.isNotEmpty()

        val typeSpec = TypeSpec.classBuilder(className)
            .addSuperinterface(Types.ProtoType)
            .addAnnotation(Annotations.OptIn(Types.ExperimentalSerializationApi))
            .addKdoc(incoming.documentation.replace("%", "%%"))
            .addAnnotation(Annotations.Serializable)
            .primaryConstructor(
                MessageConstructorGenerator.generatePrimaryConstructor(
                    incoming,
                    parameterTypes,
                    oneOfs
                )
            )
            .addType(
                MessageCompanionObjectGenerator.generateCompanionObject(
                    className, nested, oneOfs, generateCreateFun, incoming.type.typeUrl!!,
                )
            )
            .addTypes(nested.map(TypeGenerator.Result::typeSpec))
            .apply {
                if (generateCreateFun) {
                    addType(
                        MessageDSLBuilderGenerator.generateMessageBuilder(
                            incoming.name,
                            properties.mapIndexed { index, it -> it to incoming.declaredFields[index] },
                            oneOfProperties
                        )
                    )
                }
            }
            .addProperties(properties)
            .addProperties(oneOfProperties)
            .addProperty(
                PropertySpec.builder("definition", Types.ProtoTypeDefinition(ClassName("", incoming.name)))
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

