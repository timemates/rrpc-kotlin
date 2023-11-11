package com.y9vad9.rsocket.proto.codegen.services

import com.squareup.kotlinpoet.*
import com.squareup.wire.schema.Rpc
import com.squareup.wire.schema.Service
import com.y9vad9.rsocket.proto.codegen.*
import com.y9vad9.rsocket.proto.codegen.Transformer
import com.y9vad9.rsocket.proto.codegen.Types
import com.y9vad9.rsocket.proto.codegen.isRequestResponse
import com.y9vad9.rsocket.proto.codegen.isRequestStream

internal object ServiceTransformer : Transformer<Service, TypeSpec> {
    override fun transform(incoming: Service): TypeSpec {
        val procedures = incoming.rpcs.map(RpcTransformer::transform)

        val procedureDescriptors = incoming.rpcs.mapIndexed { index, rpc ->
            createDescriptor(rpc, procedures[index].parameters.first().type, procedures[index].returnType)
        }

        return TypeSpec.classBuilder(incoming.name)
            .addModifiers(KModifier.ABSTRACT)
            .addKdoc(incoming.documentation)
            .addProperty(
                PropertySpec.builder("descriptor", Types.serviceDescriptor)
                    .addModifiers(KModifier.FINAL, KModifier.OVERRIDE)
                    .initializer(
                        """
                        ServiceDescriptor(
                            name = "${incoming.name}",
                            procedures = listOf(${procedureDescriptors.joinToString(",\n")})
                        )
                        """.trimIndent()
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
        args = arrayOf(Types.procedureDescriptor.requestResponse, receiverType, returnType)
    )

    private fun getProcedure(rpc: Rpc) = when {
        rpc.isRequestResponse -> "{ ${rpc.name}(it as %2T) as %3T }"
        rpc.isRequestStream -> "{ ${rpc.name}(it as %2T) as Flow<%3T> }"
        rpc.isRequestChannel -> "{ init, incoming -> ${rpc.name}(init as %2T, incoming as Flow<%2T>) as Flow<%3T> }"
        else -> error("Invalid state.")
    }
}