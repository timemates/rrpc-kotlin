package org.timemates.rsp.codegen.generators.client

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.wire.schema.Rpc
import com.squareup.wire.schema.Schema
import org.timemates.rsp.codegen.ext.decapitalized
import org.timemates.rsp.codegen.ext.*
import org.timemates.rsp.codegen.ext.asClassName
import org.timemates.rsp.codegen.ext.isRequestChannel
import org.timemates.rsp.codegen.ext.isRequestResponse
import org.timemates.rsp.codegen.ext.isRequestStream
import org.timemates.rsp.codegen.typemodel.Types
import kotlin.text.trimIndent

public object ClientRpcGenerator {
    public fun generateRpc(serviceName: String, rpc: Rpc, schema: Schema): FunSpec {
        if (rpc.requestStreaming && !rpc.responseStreaming)
            error("Client-only streaming is not supported.")

        val rpcName = rpc.name.decapitalized()
        val rpcRequestType = rpc.requestType!!.asClassName(schema)
        val rpcReturnType = rpc.responseType!!.asClassName(schema)

        val code = CodeBlock.of(
            format = REQUEST_CODE,
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
            .returns(rpcReturnType.let { if (rpc.responseStreaming) Types.Flow(it) else it })
            .build()
    }
}

private val REQUEST_CODE = """
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
