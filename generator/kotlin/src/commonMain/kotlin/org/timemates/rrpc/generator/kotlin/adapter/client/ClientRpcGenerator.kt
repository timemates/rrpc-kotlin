package org.timemates.rrpc.generator.kotlin.adapter.client

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.*
import org.timemates.rrpc.common.schema.value.RMDeclarationUrl
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.asClassName
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.deprecated

public object ClientRpcGenerator {
    public fun generateRpc(serviceName: String, rpc: RSRpc, schema: RSResolver): FunSpec {
        when {
            rpc.requestType.type == RMDeclarationUrl.ACK && rpc.requestType.isStreaming ->
                error("Ack type cannot be used as streaming type.")

            rpc.responseType.type == RMDeclarationUrl.ACK && rpc.responseType.isStreaming ->
                error("Ack type cannot be used as streaming type.")

            rpc.requestType.isStreaming && !rpc.responseType.isStreaming ->
                error("Client-only streaming is not supported.")
        }

        val rpcName = rpc.kotlinName()
        val rpcRequestType = rpc.requestType.type.asClassName(schema)
        val rpcReturnType = rpc.responseType.type.asClassName(schema)

        val code = when {
            rpc.isFireAndForget -> CodeBlock.of(
                FIRE_AND_FORGET_CODE,
                LibClassNames.ClientMetadata,
                serviceName,
                rpcName,
                LibClassNames.ExtraMetadata,
                rpcRequestType,
                LibClassNames.Options,
            )

            rpc.isMetadataPush -> CodeBlock.of(
                METADATA_PUSH_CODE,
                LibClassNames.ClientMetadata,
                serviceName,
                rpcName,
                LibClassNames.ExtraMetadata,
                LibClassNames.Options,
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
                    LibClassNames.ClientMetadata,
                    serviceName,
                    rpcName,
                    LibClassNames.ExtraMetadata,
                    if (rpc.requestType.isStreaming) "messages" else "message",
                    rpcRequestType,
                    rpcReturnType,
                    LibClassNames.Options,
                )
            )
        }

        return FunSpec.builder(rpc.kotlinName())
            .deprecated(rpc.options.isDeprecated)
            .apply {
                if (rpc.requestType.isStreaming) {
                    addParameter(
                        name = "messages",
                        type = LibClassNames.Flow(rpcRequestType)
                    )
                } else {
                    addParameter(
                        name = "message",
                        type = rpcRequestType,
                    )
                }
            }
            .addParameter(
                ParameterSpec.builder("extra", MAP.parameterizedBy(STRING, BYTE_ARRAY))
                    .defaultValue("emptyMap()")
                    .build()
            )
            .apply {
                if (rpc.isRequestResponse || rpc.isFireAndForget || rpc.isMetadataPush) {
                    addModifiers(KModifier.SUSPEND)
                }
            }
            .addCode(code)
            .returns(rpcReturnType.let { if (rpc.responseType.isStreaming) LibClassNames.Flow(it) else it })
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
