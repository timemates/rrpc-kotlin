package org.timemates.rrpc.generator.kotlin.client

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.wire.schema.ProtoType
import com.squareup.wire.schema.Rpc
import org.timemates.rrpc.common.metadata.RMResolver
import org.timemates.rrpc.codegen.ext.*
import org.timemates.rrpc.codegen.typemodel.Types

public object ClientRpcGenerator {
    public fun generateRpc(serviceName: String, rpc: Rpc, schema: RMResolver): FunSpec {
        when {
            rpc.requestType == ProtoType.ACK && rpc.requestStreaming ->
                error("Ack type cannot be used as streaming type.")
            rpc.responseType == ProtoType.ACK && rpc.responseStreaming ->
                error("Ack type cannot be used as streaming type.")
            rpc.requestStreaming && !rpc.responseStreaming ->
                error("Client-only streaming is not supported.")
        }

        val rpcName = rpc.name.decapitalized()
        val rpcRequestType = rpc.requestType!!.asClassName(schema)
        val rpcReturnType = rpc.responseType!!.asClassName(schema)

        val code = when {
            rpc.isFireAndForget -> CodeBlock.of(
                FIRE_AND_FORGET_CODE,
                Types.ClientMetadata,
                serviceName,
                rpcName,
                Types.ExtraMetadata,
                rpcRequestType,
                Types.Options,
            )
            rpc.isMetadataPush -> CodeBlock.of(
                METADATA_PUSH_CODE,
                Types.ClientMetadata,
                serviceName,
                rpcName,
                Types.ExtraMetadata,
                Types.Options,
            )
            else -> CodeBlock.of(
                format = BASIC_REQUEST_CODE,
                args = arrayOf(
                    when {
                        rpc.isRequestResponse -> "requestResponse"
                        rpc.isRequestStream -> "requestStream"
                        rpc.isRequestChannel -> "requestChannel"
                        else -> error("Unsupported type.")
                    },
                    Types.ClientMetadata,
                    serviceName,
                    rpcName,
                    Types.ExtraMetadata,
                    if (rpc.requestStreaming) "messages" else "message",
                    rpcRequestType,
                    rpcReturnType,
                    Types.Options,
                )
            )
        }

        return FunSpec.builder(rpc.name.decapitalized())
            .deprecated(rpc.options.isDeprecated)
            .apply {
                if (rpc.requestStreaming) {
                    addParameter(
                        name = "messages",
                        type = Types.Flow(rpcRequestType)
                    )
                } else {
                    addParameter(
                        name = "message",
                        type = rpcRequestType,
                    )
                }
            }
            .addParameter(
                ParameteRRpcec.builder("extra", MAP.parameterizedBy(STRING, BYTE_ARRAY))
                    .defaultValue("emptyMap()")
                    .build()
            )
            .apply {
                if (!rpc.requestStreaming && !rpc.responseStreaming) {
                    addModifiers(KModifier.SUSPEND)
                }
            }
            .addCode(code)
            .returns(rpcReturnType.let { if (rpc.responseStreaming) Types.Flow(it) else it })
            .build()
    }
}

private val BASIC_REQUEST_CODE = """
        return handler.%1L(
                %2T(
                    serviceName = %3S,
                    procedureName = %4S,
                    extra = %5T(extra),
                ),
                data = %6L,
                options = rpcsOptions[%4S] ?: %9T.EMPTY,
                serializationStrategy = %7T.serializer(),
                deserializationStrategy = %8T.serializer(),
            )
        """.trimIndent()

private val FIRE_AND_FORGET_CODE = """
        return handler.fireAndForget(
                %1T(
                    serviceName = %2S,
                    procedureName = %3S,
                    extra = %4T(extra),
                ),
                data = message,
                options = rpcsOptions[%3S] ?: %5T.EMPTY,
                serializationStrategy = %6T.serializer(),
            )
        """.trimIndent()

private val METADATA_PUSH_CODE = """
        return handler.metadataPush(
                %1T(
                    serviceName = %2S,
                    procedureName = %3S,
                    extra = %4T(extra),
                ),
                options = rpcsOptions[%3S] ?: %5T.EMPTY,
            )
        """.trimIndent()
