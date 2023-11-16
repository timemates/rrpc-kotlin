package io.timemates.rsproto.codegen.services.server

import com.squareup.kotlinpoet.*
import com.squareup.wire.schema.Rpc
import com.squareup.wire.schema.Service
import com.y9vad9.rsproto.codegen.*
import com.y9vad9.rsproto.codegen.Types
import com.y9vad9.rsproto.codegen.isRequestChannel
import com.y9vad9.rsproto.codegen.isRequestResponse
import com.y9vad9.rsproto.codegen.isRequestStream
import com.y9vad9.rsproto.codegen.services.RpcTransformer

internal object ServerServiceTransformer {
    fun transform(incoming: Service): TypeSpec {
        val procedures = incoming.rpcs.map(RpcTransformer::transform)

        val procedureDescriptors = incoming.rpcs.mapIndexed { index, rpc ->
            createDescriptor(rpc, rpc.requestType!!.asClassName(), rpc.requestType!!.asClassName())
        }

        return TypeSpec.classBuilder(incoming.name)
            .addModifiers(KModifier.ABSTRACT)
            .addKdoc(incoming.documentation)
            .addProperty(
                PropertySpec.builder("descriptor", Types.serviceDescriptor)
                    .addModifiers(KModifier.FINAL, KModifier.OVERRIDE)
                    .initializer(
                        CodeBlock.builder()
                            .addStatement("ServiceDescriptor(")
                            .indent()
                            .addStatement("name = %S,", incoming.name)
                            .add("procedures = ")
                            .addAllSeparated(procedureDescriptors)
                            .unindent()
                            .addStatement(")")
                            .build()
                    )
                    .build()
            )
            .addFunctions(procedures)
            .build()
    }

    private fun createDescriptor(rpc: Rpc, receiverType: TypeName, returnType: TypeName): CodeBlock = CodeBlock.of(
        """
    %1T(
        name = "${rpc.name}",
        inputSerializer = %2T.serializer(),
        outputSerializer = %3T.serializer(),
        procedure = ${getProcedure(rpc)}
    )
    """.trimIndent(),
        args = arrayOf(
            when {
                rpc.isRequestResponse -> Types.procedureDescriptor.requestResponse
                rpc.isRequestStream -> Types.procedureDescriptor.requestStream
                rpc.isRequestChannel -> Types.procedureDescriptor.requestChannel
                else -> error("Should never reach this state.")
            },
            receiverType,
            returnType,
        )
    )

    private fun getProcedure(rpc: Rpc) = when {
        rpc.isRequestResponse -> "{ ${rpc.name}(it as %2T) as %3T }"
        rpc.isRequestStream -> "{ ${rpc.name}(it as %2T) as Flow<%3T> }"
        rpc.isRequestChannel -> "{ init, incoming -> ${rpc.name}(init as %2T, incoming·as·Flow<%2T>)·as·Flow<%3T> }"
        else -> error("Invalid state.")
    }
}