package com.y9vad9.rsocket.proto.codegen.types

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.y9vad9.rsocket.proto.codegen.Transformer

internal object MessageBuilderTransformer : Transformer<Pair<String, List<PropertySpec>>, TypeSpec> {
    override fun transform(incoming: Pair<String, List<PropertySpec>>): TypeSpec {
        val (name, properties) = incoming

        return TypeSpec.classBuilder("Builder")
            .addProperties(properties.map { it.toBuilder().mutable(true).build() })
            .addFunction(
                FunSpec.builder("build")
                    .addCode("return ${name}(${properties.joinToString(", ") { it.name }})")
                    .returns(ClassName("", name))
                .build()
            )
            .build()
    }

}