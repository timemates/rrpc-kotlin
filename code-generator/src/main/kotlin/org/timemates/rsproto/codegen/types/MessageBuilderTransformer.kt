package org.timemates.rsproto.codegen.types

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.Field
import org.timemates.rsproto.codegen.Annotations

internal object MessageBuilderTransformer {
    fun transform(
        name: String,
        declaredFields: List<Pair<PropertySpec, Field>>,
        oneOfs: List<PropertySpec>,
    ): TypeSpec {
        return TypeSpec.classBuilder("Builder")
            .addProperties(declaredFields.map { (spec, type) ->
                spec.toBuilder().initializer(TypeDefaultValueTransformer.transform(type)).mutable(true).also {
                    it.annotations.clear()
                }.build()
            })
            .addProperties(oneOfs.map { it.toBuilder().apply { annotations.clear()}.mutable(true).initializer("null").build() })
            .addFunction(
                FunSpec.builder("build")
                    .addCode("return ${name}(${declaredFields.joinToString(", ") { it.first.name }})")
                    .returns(ClassName("", name))
                    .build()
            )
            .build()
    }

}