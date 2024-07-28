package org.timemates.rsp.codegen.generators.client

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.wire.schema.Options
import com.squareup.wire.schema.Rpc
import com.squareup.wire.schema.Schema
import org.timemates.rsp.codegen.*
import org.timemates.rsp.codegen.decapitalize
import kotlin.text.trimIndent

public object ClientRpcGenerator {
    public fun generateRpc(serviceName: String, rpc: Rpc, schema: Schema): FunSpec {
        if (rpc.requestStreaming && !rpc.responseStreaming)
            error("Client-only streaming is not supported.")

        val rpcName = rpc.name.decapitalize()
        val rpcRequestType = rpc.requestType!!.asClassName(schema)
        val rpcReturnType = rpc.responseType!!.asClassName(schema)

        println(rpc.options.map)
        println(rpc.options.get(Options.deprecated))

        rpc.options.map.entries.joinToString { it.value!!::class.qualifiedName!! }
            .let { println(it) }

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
                if (rpc.requestStreaming) "messages" else "message",
                rpcRequestType,
                rpcReturnType,
            )
        )

        return FunSpec.builder(rpc.name.decapitalize())
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
                extra = extra,
            ),
            data = %5S,
            options = rpcsOptions[%4S],
            serializationStrategy = %6T.serializer(),
            deserializationStrategy = %7T.serializer(),
        )
        """.trimIndent()
