package org.timemates.rsproto.codegen.services.client

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.wire.schema.Rpc
import com.squareup.wire.schema.Schema
import com.squareup.wire.schema.Service
import org.timemates.rsproto.codegen.*

internal object ClientServiceApiGenerator {
    fun generate(service: Service, schema: Schema): TypeSpec {
        return TypeSpec.classBuilder("${service.name}Api")
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("rsocket", Types.rsocket)
                    .addParameter("protobuf", Types.protoBuf)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("rsocket", Types.rsocket)
                    .addModifiers(KModifier.PRIVATE)
                    .initializer("rsocket")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("protobuf", Types.protoBuf)
                    .addModifiers(KModifier.PRIVATE)
                    .initializer("protobuf").build()
            )
            .addFunctions(service.rpcs.map { mapRpc(it, service.type.qualifiedName(schema), schema) })
            .build()
    }

    private fun mapRpc(rpc: Rpc, serviceName: String, schema: Schema): FunSpec {
        if (rpc.requestStreaming && !rpc.responseStreaming)
            error("Client-only streaming is not supported.")

        val callCode = when {
            rpc.isRequestChannel ->
                "requestChannel(initialPayload, payloads)"

            rpc.isRequestResponse -> "requestResponse(payload)"
            rpc.isRequestStream -> "requestStream(payload)"
            else -> error("Should never reach this state")
        }

        val deserializationCode = when (rpc.responseStreaming) {
            true -> ".map·{ protobuf.decodeFromByteArray(it.data.readBytes()) }"
            false -> ".let·{ protobuf.decodeFromByteArray(it.data.readBytes()) }"
        }

        val code = when (rpc.requestStreaming) {
            true -> CodeBlock.builder()
                .addStatement("val encodedInitMessage = protobuf.encodeToByteArray(initMessage)")
                .addStatement(
                    format = "val encodedMetadata = protobuf.encodeToByteArray(%1T(serviceName = %2S, procedureName = %3S, extra = extra))",
                    args = arrayOf(Types.metadata, serviceName, rpc.name),
                )
                .addStatement(
                    "val initPayload = Payload(data = %1T(encodedInitMessage), metadata = %1T(encodedMetadata))",
                    Types.byteReadPacket
                )
                .addStatement("val payloads = messages.map { protobuf.encodeToByteArray(it) }")
                .addStatement("return rsocket.$callCode$deserializationCode")
                .build()

            false -> CodeBlock.builder()
                .addStatement("val encodedMessage = protobuf.encodeToByteArray(message)")
                .addStatement(
                    format = "val encodedMetadata = protobuf.encodeToByteArray(%1T(%2S, %3S, extra))",
                    args = arrayOf(Types.metadata, serviceName, rpc.name),
                )
                .addStatement(
                    "val payload = Payload(data = %1T(encodedMessage), metadata = %1T(encodedMetadata))",
                    Types.byteReadPacket
                )
                .addStatement("return rsocket.$callCode$deserializationCode")
                .build()
        }

        return FunSpec.builder(rpc.name.decapitalize())
            .apply {
                val className = rpc.requestType!!
                    .asClassName(schema)

                if (rpc.requestStreaming) {
                    addParameter(
                        name = "initMessage",
                        type = className,
                    )
                    addParameter(
                        name = "messages",
                        type = Types.flow(className)
                    )
                } else {
                    addParameter(
                        name = "message",
                        type = className,
                    )
                }
            }
            .addParameter(
                ParameterSpec.builder("extra", MAP.parameterizedBy(STRING, BYTE_ARRAY))
                    .defaultValue("emptyMap()")
                    .build()
            )
            .apply {
                if (!rpc.requestStreaming && !rpc.responseStreaming) {
                    addModifiers(KModifier.SUSPEND)
                }
            }
            .addCode(code)
            .returns(rpc.responseType!!.asClassName(schema)
                .let { if (rpc.responseStreaming) Types.flow(it) else it })
            .build()
    }
}