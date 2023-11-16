package io.timemates.rsproto.codegen.services.client

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.wire.schema.Rpc
import com.squareup.wire.schema.Service
import com.y9vad9.rsproto.codegen.*

internal object ClientServiceApiGenerator {
    fun generate(service: Service): TypeSpec {
        return TypeSpec.classBuilder("${service.name}Api")
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("rsocket", Types.rsocket)
                    .addParameter("protobuf", Types.protoBuf)
                    .build()
            )
            .addProperty(PropertySpec.builder("rsocket", Types.rsocket).initializer("rsocket").build())
            .addProperty(PropertySpec.builder("protobuf", Types.protoBuf).initializer("protobuf").build())
            .addFunctions(service.rpcs.map { mapRpc(it, service.name) })
            .build()
    }

    private fun mapRpc(rpc: Rpc, serviceName: String): FunSpec {
        val callCode = when {
            rpc.isRequestChannel ->
                "requestChannel(initialPayload, payloads)"
            rpc.isRequestResponse -> "requestResponse(payload, metadata)"
            rpc.isRequestStream -> "requestStream(payload, metadata)"
            else -> error("Should never reach this state")
        }

        val deserializationCode = when (rpc.requestStreaming) {
            true -> ".let·{ responses -> responses.map·{ protobuf.decodeFromByteArray(it) } }"
            false -> ".let·{ protobuf.decodeFromByteArray(it) }"
        }

        val code = when (rpc.requestStreaming) {
            true -> CodeBlock.builder()
                .addStatement("val encodedInitMessage = protobuf.encodeToByteArray(initMessage)")
                .addStatement(
                    format = "val encodedMetadata = protobuf.encodeToByteArray(%1T(serviceName = %2S, procedureName = %3S, extra = extra))",
                    args = arrayOf(Types.metadata, serviceName, rpc.name),
                )
                .addStatement("val initPayload = Payload(data = encodedInitMessage, metadata = encodedMetadata)")
                .addStatement("val payloads = messages.map { protobuf.encodeToByteArray(it) }")
                .addStatement("return rsocket.$callCode$deserializationCode")
                .build()

            false -> CodeBlock.builder()
                .addStatement("val encodedMessage = protobuf.encodeToByteArray(message)")
                .addStatement(
                    format = "val encodedMetadata = protobuf.encodeToByteArray(%1T(%2S, %3S, extra))",
                    args = arrayOf(Types.metadata, serviceName, rpc.name),
                )
                .addStatement("val payload = Payload(data = encodedMessage, metadata = encodedMetadata)")
                .addStatement("return rsocket.$callCode$deserializationCode")
                .build()
        }

        return FunSpec.builder(rpc.name)
            .addParameter("message", rpc.requestType!!.asClassName())
            .addParameter(
                ParameterSpec.builder("extra", MAP.parameterizedBy(STRING, STRING))
                    .defaultValue("emptyMap()")
                    .build()
            )
            .addModifiers(KModifier.SUSPEND)
            .addCode(code)
            .build()
    }
}