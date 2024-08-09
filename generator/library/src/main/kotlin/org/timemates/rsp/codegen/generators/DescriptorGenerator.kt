package org.timemates.rsp.codegen.generators

import com.squareup.kotlinpoet.*
import com.squareup.wire.schema.Rpc
import com.squareup.wire.schema.Schema
import com.squareup.wire.schema.Service
import org.timemates.rsp.codegen.ext.*
import org.timemates.rsp.codegen.ext.asClassName
import org.timemates.rsp.codegen.ext.isRequestResponse
import org.timemates.rsp.codegen.ext.isRequestStream
import org.timemates.rsp.codegen.ext.qualifiedName
import org.timemates.rsp.codegen.typemodel.Types

public object DescriptorGenerator {

    public fun generateDescriptor(service: Service, schema: Schema): PropertySpec {
        val procedureDescriptors = service.rpcs.map { rpc ->
            generateRpcDescriptor(rpc, rpc.requestType!!.asClassName(schema), rpc.responseType!!.asClassName(schema), schema)
        }

        return PropertySpec.builder("descriptor", Types.ServiceDescriptor)
            .addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", "UNCHECKED_CAST").build())
            .addModifiers(KModifier.FINAL, KModifier.OVERRIDE)
            .initializer(generateServiceDescriptorBlock(service, schema, procedureDescriptors))
            .build()
    }

    private fun generateServiceDescriptorBlock(
        service: Service,
        schema: Schema,
        procedureDescriptors: List<CodeBlock>
    ): CodeBlock {
        return CodeBlock.builder()
            .addStatement("%T(", Types.ServiceDescriptor)
            .indent()
            .addStatement("name = %S,", service.type.qualifiedName(schema))
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
        rpc: Rpc,
        receiverType: TypeName,
        returnType: TypeName,
        schema: Schema,
    ): CodeBlock {
        val procedureType = when {
            rpc.isRequestResponse -> Types.ProcedureDescriptor.requestResponse
            rpc.isRequestStream -> Types.ProcedureDescriptor.requestStream
            rpc.isRequestChannel -> Types.ProcedureDescriptor.requestChannel
            else -> error("Should never reach this state.")
        }

        return CodeBlock.builder()
            .addStatement("%T(", procedureType)
            .indent()
            .addStatement("name = %S,", rpc.name)
            .addStatement("inputSerializer = %T.serializer() as %T<Any>,", receiverType, Types.KSerializer)
            .addStatement("outputSerializer = %T.serializer() as %T<Any>,", returnType, Types.KSerializer)
            .addStatement("procedure = %L", getProcedure(rpc, schema))
            .unindent()
            .addStatement(")")
            .build()
    }

    private fun getProcedure(rpc: Rpc, schema: Schema): CodeBlock {
        val procedureTemplate = when {
            rpc.isRequestResponse -> "{ ${rpc.name}(it as %2T) }"
            rpc.isRequestStream -> "{ ${rpc.name}(it as %2T) }"
            rpc.isRequestChannel -> "{ init, incoming -> ${rpc.name}(init as %2T, incoming as Flow<%2T>) }"
            else -> error("Request Streaming with no Response Streaming is not supported.")
        }

        return CodeBlock.of(procedureTemplate, rpc.requestType!!.asClassName(schema))
    }
}
