package org.timemates.rsp.codegen.types

import com.squareup.kotlinpoet.*
import com.squareup.wire.schema.MessageType
import com.squareup.wire.schema.OneOf
import com.squareup.wire.schema.Schema
import org.timemates.rsp.codegen.Annotations
import org.timemates.rsp.codegen.asClassName
import org.timemates.rsp.codegen.capitalized

internal object OneOfGenerator {
    data class Result(
        val oneOfClass: TypeSpec,
        val property: PropertySpec,
    )

    fun generate(oneof: OneOf, schema: Schema): Result {
        val oneOfName = "${oneof.name.capitalized()}OneOf"
        val oneOfClassName = ClassName("", oneOfName)

        val oneOfClass = TypeSpec.interfaceBuilder(oneOfName)
            .addAnnotation(Annotations.Serializable)
            .addModifiers(KModifier.SEALED)
            .addTypes(oneof.fields.map { field ->
                val defaultValue = TypeDefaultValueTransformer.transform(field)
                val typeName = field.type!!.asClassName(schema)
                val builder = typeName.nestedClass("Builder")

                val fieldName = field.name.capitalized()

                TypeSpec.valueClassBuilder(fieldName)
                    .addAnnotation(Annotations.Serializable)
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
                        val type = schema.getType(field.type!!)
                        if (type is MessageType && type.fields.isNotEmpty())
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
                            .addAnnotation(Annotations.ProtoNumber(field.tag))
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
            .addKdoc(oneof.documentation)
            .initializer(oneof.name)
            .build()

        return Result(oneOfClass, property)
    }
}