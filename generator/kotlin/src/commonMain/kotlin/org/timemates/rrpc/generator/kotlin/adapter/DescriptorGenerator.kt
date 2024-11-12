package org.timemates.rrpc.generator.kotlin.adapter

import com.squareup.kotlinpoet.*
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.*
import org.timemates.rrpc.common.schema.annotations.NonPlatformSpecificAccess
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.addAllSeparated
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.asClassName
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.qualifiedName

public object DescriptorGenerator {

    public fun generateDescriptor(service: RSService, resolver: RSResolver): PropertySpec {
        val procedureDescriptors = service.rpcs.map { rpc ->
            generateRpcDescriptor(
                rpc,
                rpc.requestType.asClassName(resolver),
                rpc.responseType.asClassName(resolver),
                resolver
            )
        }

        return PropertySpec.builder("descriptor", LibClassNames.ServiceDescriptor)
            .addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", "UNCHECKED_CAST").build())
            .addModifiers(KModifier.FINAL, KModifier.OVERRIDE)
            .initializer(generateServiceDescriptorBlock(service, resolver, procedureDescriptors))
            .build()
    }

    private fun generateServiceDescriptorBlock(
        service: RSService,
        resolver: RSResolver,
        procedureDescriptors: List<CodeBlock>,
    ): CodeBlock {
        return CodeBlock.builder()
            .addStatement("%T(", LibClassNames.ServiceDescriptor)
            .indent()
            .addStatement("name = %S,", service.typeUrl.qualifiedName(resolver))
            .add("procedures = listOf(\n")
            .indent()
            .addAllSeparated(procedureDescriptors, ",\n")
            .unindent()
            .addStatement(")")
            .unindent()
            .addStatement(")")
            .build()
    }

    private fun generateRpcDescriptor(
        rpc: RSRpc,
        receiverType: TypeName,
        returnType: TypeName,
        resolver: RSResolver,
    ): CodeBlock {
        val procedureType = when {
            rpc.isRequestResponse -> LibClassNames.ProcedureDescriptor.requestResponse
            rpc.isRequestStream -> LibClassNames.ProcedureDescriptor.requestStream
            rpc.isRequestChannel -> LibClassNames.ProcedureDescriptor.requestChannel
            else -> error("Should never reach this state.")
        }

        @OptIn(NonPlatformSpecificAccess::class)
        return CodeBlock.builder()
            .addStatement("%T(", procedureType)
            .indent()
            .addStatement("name = %S,", rpc.name)
            .addStatement("inputSerializer = %T.serializer() as %T<Any>,", receiverType, LibClassNames.KSerializer)
            .addStatement("outputSerializer = %T.serializer() as %T<Any>,", returnType, LibClassNames.KSerializer)
            .addStatement("procedure = %L", getProcedure(rpc, resolver))
            .unindent()
            .addStatement(")")
            .build()
    }

    @OptIn(NonPlatformSpecificAccess::class)
    private fun getProcedure(rpc: RSRpc, resolver: RSResolver): CodeBlock {
        val procedureTemplate = when {
            rpc.isRequestResponse -> "{ ${rpc.name}(it as %2T) }"
            rpc.isRequestStream -> "{ ${rpc.name}(it as %2T) }"
            rpc.isRequestChannel -> "{ init, incoming -> ${rpc.name}(init as %2T, incoming as Flow<%2T>) }"
            else -> error("Request Streaming with no Response Streaming is not supported.")
        }

        return CodeBlock.of(procedureTemplate, rpc.requestType.asClassName(resolver))
    }
}
