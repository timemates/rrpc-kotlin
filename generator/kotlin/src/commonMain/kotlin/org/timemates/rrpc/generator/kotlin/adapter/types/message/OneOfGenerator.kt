package org.timemates.rrpc.generator.kotlin.adapter.types.message

import com.squareup.kotlinpoet.*
import org.timemates.rrpc.codegen.typemodel.PoetAnnotations
import org.timemates.rrpc.common.schema.RSOneOf
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.RSType
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.asClassName
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.capitalized
import org.timemates.rrpc.generator.kotlin.adapter.types.TypeDefaultValueGenerator

internal object OneOfGenerator {
    data class Result(
        val oneOfClass: TypeSpec,
        val property: PropertySpec,
    )

    fun generateOneOf(oneof: RSOneOf, schema: RSResolver): Result {
        val oneOfName = "${oneof.name.capitalized()}OneOf"
        val oneOfClassName = ClassName("", oneOfName)

        val oneOfClass = TypeSpec.interfaceBuilder(oneOfName)
            .addAnnotation(PoetAnnotations.Serializable)
            .addModifiers(KModifier.SEALED)
            .addTypes(oneof.fields.map { field ->
                val defaultValue = TypeDefaultValueGenerator.generateTypeDefault(field)
                val typeName = field.typeUrl.asClassName(schema)
                val builder = typeName.nestedClass("DSLBuilder")

                val fieldName = field.name.capitalized()

                TypeSpec.classBuilder(fieldName)
                    .addModifiers(KModifier.VALUE)
                    .addAnnotation(PoetAnnotations.Serializable)
                    .addAnnotation(JvmInline::class)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter(
                                ParameterSpec.builder("value", typeName)
                                    .defaultValue(
                                        defaultValue.takeUnless { it == "null" } ?: "%T.Default", typeName,
                                    )
                                    .build()
                            )
                            .build()
                    ).apply {
                        val type = schema.resolveType(field.typeUrl)
                        if (type is RSType.Message && type.fields.isNotEmpty())
                            addFunction(
                                FunSpec.constructorBuilder()
                                    .addParameter(
                                        name = "builder",
                                        type = LambdaTypeName.get(builder, returnType = UNIT),
                                    )
                                    .callThisConstructor(CodeBlock.of("%T().also(builder).build()", builder))
                                    .build()
                            )
                    }
                    .addProperty(
                        PropertySpec.builder("value", typeName).initializer("value")
                            .addAnnotation(PoetAnnotations.ProtoNumber(field.tag))
                            .build()
                    )
                    .addType(
                        TypeSpec.companionObjectBuilder()
                            .addProperty(
                                PropertySpec.builder("Default", ClassName("", fieldName))
                                    .initializer("$fieldName()")
                                    .build()
                            )
                            .build()
                    )
                    .addSuperinterface(oneOfClassName)
                    .build()
            })
            .build()

        val property = PropertySpec.builder(oneof.name, oneOfClassName.copy(nullable = true))
            .addAnnotation(PoetAnnotations.ProtoOneOf)
            .addKdoc(oneof.documentation?.replace("%", "%%").orEmpty())
            .initializer(oneof.name)
            .build()

        return Result(oneOfClass, property)
    }
}