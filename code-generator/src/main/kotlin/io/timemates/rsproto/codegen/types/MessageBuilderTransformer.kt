package io.timemates.rsproto.codegen.types

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.ProtoType

internal object MessageBuilderTransformer {
    fun transform(name: String, properties: List<Pair<PropertySpec, ProtoType>>): TypeSpec {
        return TypeSpec.classBuilder("Builder")
            .addProperties(properties.map { (spec, type) ->
                spec.toBuilder().initializer(TypeDefaultValueTransformer.transform(type)).mutable(true).also {
                    it.annotations.clear()
                }.build()
            })
            .addFunction(
                FunSpec.builder("build")
                    .addCode("return ${name}(${properties.joinToString(", ") { it.first.name }})")
                    .returns(ClassName("", name))
                    .build()
            )
            .build()
    }

}