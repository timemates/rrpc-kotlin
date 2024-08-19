package org.timemates.rsp.codegen.typemodel

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName

internal object Types {
    fun Flow(ofTypeName: TypeName): ParameterizedTypeName =
        ClassName("kotlinx.coroutines.flow", "Flow")
            .parameterizedBy(ofTypeName)

    val ServiceDescriptor = ClassName("org.timemates.rsp.server.module.descriptors", "ServiceDescriptor")

    val ByteReadPacket = ClassName("io.ktor.utils.io.core", "ByteReadPacket")

    val Payload = ClassName("io.rsocket.kotlin.payload", "Payload")

    object ProcedureDescriptor {
        val base = ClassName(
            "org.timemates.rsp.server.module.descriptors", "ProcedureDescriptor"
        )

        val requestResponse = base.nestedClass("RequestResponse")

        val requestStream = base.nestedClass("RequestStream")

        val requestChannel = base.nestedClass("RequestChannel")
    }

    object Option {
        val Base = ClassName("org.timemates.rsp.options", "Option")
        val File = ClassName("org.timemates.rsp.options", "FileOption")
        val Service = ClassName("org.timemates.rsp.options", "ServiceOption")
        val RPC = ClassName("org.timemates.rsp.options", "RPCOption")
    }

    val Options = ClassName("org.timemates.rsp.options", "Options")

    val ClientMetadata = ClassName("org.timemates.rsp.metadata", "ClientMetadata")

    val ExtraMetadata = ClassName("org.timemates.rsp.metadata", "ExtraMetadata")

    val ProtoBuf = ClassName("kotlinx.serialization.protobuf", "ProtoBuf")

    val RSocket = ClassName("io.rsocket.kotlin", "RSocket")

    val RSPClientConfig = ClassName("org.timemates.rsp.client.config", "RSPClientConfig")

    val RSPServerService = ClassName("org.timemates.rsp.server.module", "RSPService")

    val RSPClientService = ClassName("org.timemates.rsp.client", "RSPServiceClient")

    val ExperimentalSerializationApi = ClassName("kotlinx.serialization", "ExperimentalSerializationApi")

    val KSerializer = ClassName("kotlinx.serialization", "KSerializer")

    val Interceptors = ClassName("org.timemates.rsp.interceptors", "Interceptors")

    val RequestContext = ClassName("org.timemates.rsp.server", "RequestContext")
}