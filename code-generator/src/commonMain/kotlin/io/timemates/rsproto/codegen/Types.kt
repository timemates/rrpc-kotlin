package io.timemates.rsproto.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName

internal object Types {
    fun flow(ofTypeName: TypeName): ParameterizedTypeName =
        ClassName("kotlinx.coroutines.flow", "Flow")
            .parameterizedBy(ofTypeName)

    val serviceDescriptor = ClassName("com.y9vad9.rsproto.services", "ServiceDescriptor")

    @Suppress("ClassName")
    object procedureDescriptor {
        val root = ClassName(
            "com.y9vad9.rsproto.procedures", "ProcedureDescriptor"
        )

        val requestResponse = root.nestedClass("RequestResponse")

        val requestStream = root.nestedClass("RequestStream")

        val requestChannel = root.nestedClass("RequestChannel")
    }

    val metadata = ClassName("com.y9vad9.rsproto", "Metadata")

    val protoBuf = ClassName("kotlinx.serialization.protobuf", "ProtoBuf")

    val rsocket = ClassName("io.rsocket.kotlin", "RSocket")
}