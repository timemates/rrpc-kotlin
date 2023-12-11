package io.timemates.rsproto.codegen.types

import com.squareup.kotlinpoet.*
import com.squareup.wire.schema.OneOf
import com.squareup.wire.schema.Schema
import io.timemates.rsproto.codegen.Annotations
import io.timemates.rsproto.codegen.asClassName
import io.timemates.rsproto.codegen.capitalized

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
                val typeName = field.type!!.asClassName(schema).copy(nullable = defaultValue == "null")

                TypeSpec.valueClassBuilder(field.name.capitalized())
                    .addAnnotation(JvmInline::class)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter(
                                ParameterSpec.builder("value", typeName)
                                    .defaultValue(defaultValue)
                                    .build()
                            )
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("value", typeName).initializer("value")
                            .addAnnotation(Annotations.ProtoNumber(field.tag))
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