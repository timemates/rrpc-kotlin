package io.timemates.rsproto.codegen.types

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.wire.schema.MessageType
import com.squareup.wire.schema.Schema
import io.timemates.rsproto.codegen.Annotations
import io.timemates.rsproto.codegen.Types
import io.timemates.rsproto.codegen.asClassName

internal object MessageTypeTransformer {
    fun transform(incoming: MessageType, schema: Schema): TypeSpec {
        println(incoming.oneOfs)
        val parameterTypes = incoming.declaredFields.map { field ->
            val fieldType = field.type!!

            when {
                fieldType.isScalar || fieldType.isWrapper || fieldType.isMap -> BuiltinsTransformer.transform(fieldType)
                    .let {
                        if (field.isRepeated)
                            LIST.parameterizedBy(it)
                        else it
                    }

                else -> fieldType.asClassName(schema).let {
                    when {
                        field.isRepeated -> LIST.parameterizedBy(it)
                        field.isOneOf -> TODO("OneOf fields are unsupported for now.")
                        else -> it.copy(nullable = true)
                    }
                }
            }
        }

        val oneOfs = incoming.oneOfs.map { OneOfGenerator.generate(it, schema) }

        val properties = (incoming.declaredFields).mapIndexed { index, field ->
            PropertySpec.builder(field.name, parameterTypes[index])
                .initializer(field.name)
                .addKdoc(field.documentation)
                .addAnnotation(Annotations.ProtoNumber(field.tag))
                .build()
        }

        val oneOfProperties = oneOfs.map { it.property }

        val className = incoming.type.asClassName(schema)

        return TypeSpec.classBuilder(className)
            .addAnnotation(Annotations.OptIn(Types.experimentalSerializationApi))
            .addKdoc(incoming.documentation)
            .addAnnotation(Annotations.Serializable)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameters(incoming.declaredFields.mapIndexed { index, field ->
                        val type = parameterTypes[index]

                        ParameterSpec.builder(field.name, type)
                            .defaultValue(
                                if (type.isNullable)
                                    "null"
                                else field.default ?: TypeDefaultValueTransformer.transform(field)
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
                    ).apply {
                        if (incoming.fields.isNotEmpty()) {
                            addFunction(
                                FunSpec.builder("create")
                                    .addParameter(
                                        "builder",
                                        LambdaTypeName.get(
                                            receiver = ClassName("", "Builder"),
                                            returnType = UNIT,
                                        )
                                    )
                                    .addCode("return Builder().apply(builder).build()")
                                    .returns(incoming.type.asClassName(schema))
                                    .build()
                            )
                        }
                    }
                    .build()
            )
            .addTypes(incoming.nestedTypes.map { TypeTransformer.transform(it, schema) })
            .apply {
                if(incoming.fields.isNotEmpty()) {
                    addType(
                        MessageBuilderTransformer.transform(
                            incoming.name,
                            properties.mapIndexed { index, it -> it to incoming.declaredFields[index] },
                            oneOfs.map { it.property },
                        )
                    )
                }
            }
            .addProperties(properties)
            .addProperties(oneOfProperties)
            .addTypes(oneOfs.map { it.oneOfClass })
            .build()
    }
}