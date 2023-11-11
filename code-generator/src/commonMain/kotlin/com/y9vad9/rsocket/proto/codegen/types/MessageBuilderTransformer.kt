package com.y9vad9.rsocket.proto.codegen.types

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.ProtoType
import com.y9vad9.rsocket.proto.codegen.Transformer

internal object MessageBuilderTransformer : Transformer<Pair<String, List<Pair<PropertySpec, ProtoType>>>, TypeSpec> {
    override fun transform(incoming: Pair<String, List<Pair<PropertySpec, ProtoType>>>): TypeSpec {
        val (name, properties) = incoming

        return TypeSpec.classBuilder("Builder")
            .addProperties(properties.map { (spec, type) ->
                spec.toBuilder().initializer(TypeDefaultValueTransformer.transform(type)).mutable(true).build()
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