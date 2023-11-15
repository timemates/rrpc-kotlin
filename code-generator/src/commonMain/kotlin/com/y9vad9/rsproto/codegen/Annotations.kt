package com.y9vad9.rsproto.codegen

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName

@Suppress("FunctionName")
internal object Annotations {
    fun ProtoNumber(number: Int): AnnotationSpec =
        AnnotationSpec.builder(
            ClassName("kotlinx.serialization.protobuf", "ProtoNumber")
        ).addMember(number.toString()).build()

    val Serializable = AnnotationSpec.builder(ClassName("kotlinx.serialization", "Serializable")).build()
}