package org.timemates.rsproto.codegen.services.server

import com.squareup.kotlinpoet.*
import com.squareup.wire.schema.Rpc
import com.squareup.wire.schema.Schema
import com.squareup.wire.schema.Service
import org.timemates.rsproto.codegen.*
import org.timemates.rsproto.codegen.Types
import org.timemates.rsproto.codegen.isRequestChannel
import org.timemates.rsproto.codegen.isRequestResponse
import org.timemates.rsproto.codegen.isRequestStream
import org.timemates.rsproto.codegen.services.RpcTransformer

internal object ServerServiceTransformer {
    fun transform(incoming: Service, schema: Schema): TypeSpec {
        val procedures = incoming.rpcs.map { RpcTransformer.transform(it, schema) }

        val procedureDescriptors = incoming.rpcs.map { rpc ->
            createDescriptor(rpc, rpc.requestType!!.asClassName(schema), rpc.responseType!!.asClassName(schema))
        }

        return TypeSpec.classBuilder(incoming.name)
            .superclass(Types.rSocketService)
            .addModifiers(KModifier.ABSTRACT)
            .addKdoc(incoming.documentation)
            .addProperty(
                PropertySpec.builder("descriptor", Types.serviceDescriptor)
                    .addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("\"UNCHECKED_CAST\"").build())
                    .addModifiers(KModifier.FINAL, KModifier.OVERRIDE)
                    .initializer(
                        CodeBlock.builder()
                            .addStatement("ServiceDescriptor(")
                            .indent()
                            .addStatement("name = %S,", incoming.type.qualifiedName(schema))
                            .add("procedures = listOf(\n")
                            .indent()
                            .addAllSeparated(procedureDescriptors)
                            .unindent()
                            .addStatement(")")
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
        inputSerializer = %2T.serializer() as %4T<Any>,
        outputSerializer = %3T.serializer() as %4T<Any>,
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
            Types.kserializer
        )
    )

    private fun getProcedure(rpc: Rpc) = when {
        rpc.isRequestResponse -> "{ ${rpc.name}(it as %2T) }"
        rpc.isRequestStream -> "{ ${rpc.name}(it as %2T) }"
        rpc.isRequestChannel -> "{ init, incoming -> ${rpc.name}(init as %2T, incoming·as·Flow<%2T>) }"
        else -> error("Request Streaming with no Response Streaming is not supported.")
    }
}