package org.timemates.rsp.codegen.generators.types

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.wire.schema.MessageType
import com.squareup.wire.schema.Schema
import org.timemates.rsp.codegen.Annotations
import org.timemates.rsp.codegen.Types
import org.timemates.rsp.codegen.asClassName

internal object MessageTypeGenerator {
    data class Result(val type: TypeSpec, val constructorFun: FunSpec?)

    fun generateMessage(incoming: MessageType, schema: Schema): Result {
        val parameterTypes = incoming.declaredFields.map { field ->
            val fieldType = field.type!!

            when {
                fieldType.isScalar || fieldType.isWrapper || fieldType.isMap -> BuiltinsGenerator.generateBuiltin(
                    fieldType
                ).let {
                    if (field.isRepeated)
                        LIST.parameterizedBy(it)
                    else it
                }

                else -> fieldType.asClassName(schema).let {
                    when {
                        field.isRepeated -> LIST.parameterizedBy(it)
                        else -> it.copy(nullable = true)
                    }
                }
            }
        }

        val oneOfs = incoming.oneOfs.map { OneOfGenerator.generateOneOf(it, schema) }

        val properties = (incoming.declaredFields).mapIndexed { index, field ->
            PropertySpec.builder(field.name, parameterTypes[index])
                .initializer(field.name)
                .addKdoc(field.documentation)
                .addAnnotation(Annotations.ProtoNumber(field.tag))
                .apply {
                    if (field.isRepeated)
                        addAnnotation(Annotations.ProtoPacked)
                }
                .build()
        }

        val oneOfProperties = oneOfs.map { it.property }

        val className = incoming.type.asClassName(schema)

        val nested = incoming.nestedTypes.map { TypeGenerator.generateType(it, schema) }

        return TypeSpec.classBuilder(className)
            .addAnnotation(Annotations.OptIn(Types.ExperimentalSerializationApi))
            .addKdoc(incoming.documentation)
            .addAnnotation(Annotations.Serializable)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addModifiers(KModifier.PRIVATE)
                    .addParameters(incoming.declaredFields.mapIndexed { index, field ->
                        val type = parameterTypes[index]

                        ParameterSpec.builder(field.name, type)
                            .defaultValue(
                                if (type.isNullable)
                                    "null"
                                else field.default ?: TypeDefaultValueGenerator.generateTypeDefault(field)
                            )
                            .build()
                    })
                    .addParameters(oneOfs.map {
                        ParameterSpec.builder(it.property.name, it.property.type).defaultValue("null").build()
                    })
                    .build()
            )
            .addType(
                TypeSpec.companionObjectBuilder()
                    .addProperty(
                        PropertySpec.builder("Default", className)
                            .initializer("%T()", className)
                            .build()
                    )
                    .addFunctions(nested.mapNotNull(TypeGenerator.Result::constructorFun))
                    .build()
            )
            .addTypes(nested.map(TypeGenerator.Result::typeSpec))
            .apply {
                if(incoming.fields.isNotEmpty()) {
                    addType(
                        MessageBuilderGenerator.generateMessageBuilder(
                            incoming.name,
                            properties.mapIndexed { index, it -> it to incoming.declaredFields[index] },
                            oneOfs.map { it.property },
                        )
                    )
                }
            }
            .addProperties(properties)
            .addProperties(oneOfProperties)
            .addTypes(oneOfs.map(OneOfGenerator.Result::oneOfClass))
            .build()
            .let {
                Result(
                    type = it,
                    constructorFun = if (incoming.fields.isNotEmpty()) {
                        val nestedClassName = incoming.type.asClassName(schema)
                        FunSpec.builder(it.name!!)
                            .addParameter(
                                "builder",
                                LambdaTypeName.get(
                                    receiver = nestedClassName.nestedClass("Builder"),
                                    returnType = UNIT,
                                )
                            )
                            .addCode("return ${it.name}.Builder().apply(builder).build()")
                            .returns(nestedClassName)
                            .build()
                    } else null,
                )
            }
    }
}