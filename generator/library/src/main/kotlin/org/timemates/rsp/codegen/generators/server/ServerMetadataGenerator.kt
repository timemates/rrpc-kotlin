package org.timemates.rsp.codegen.generators.server

import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.wire.schema.Options
import com.squareup.wire.schema.Schema
import com.squareup.wire.schema.Service
import org.timemates.rsp.codegen.ext.*
import org.timemates.rsp.codegen.ext.asClassName
import org.timemates.rsp.codegen.ext.decapitalized
import org.timemates.rsp.codegen.ext.isRequestResponse
import org.timemates.rsp.codegen.ext.newline
import org.timemates.rsp.codegen.generators.options.RawOptionsCodeGeneration
import org.timemates.rsp.codegen.typemodel.Types

public object ServerMetadataGenerator {
    public fun generateMetadata(
        service: Service,
        schema: Schema,
    ): PropertySpec {
        return PropertySpec.builder(
            "descriptor", Types.ServiceDescriptor,
        ).initializer(
            buildCodeBlock {
                add("%T(", Types.ServiceDescriptor)
                newline()
                indent()
                add("name = %S,", service.name)
                newline()
                add("procedures = listOf(")
                indent()

                service.rpcs.forEach { rpc ->
                    val requestType = rpc.requestType!!.asClassName(schema)
                    val responseType = rpc.responseType!!.asClassName(schema)

                    val type = when {
                        rpc.isRequestResponse -> Types.ProcedureDescriptor.requestResponse
                        rpc.isRequestStream -> Types.ProcedureDescriptor.requestStream
                        rpc.isRequestChannel -> Types.ProcedureDescriptor.requestChannel
                        else -> error("Unsupported type of request for ${service.name}#${rpc.name}")
                    }

                    newline()
                    add("%T(", type)
                    newline()
                    indent()
                    add("name = %S", rpc.name)
                    newline(",")
                    add("inputSerializer = %T.serializer()", requestType)
                    newline(",")
                    add("outputSerializer = %T.serializer()", responseType)
                    newline(",")
                    add("procedure = { context, data -> %L(context, data) }", rpc.name.decapitalized())
                    newline(",")
                    add("options = %L", RawOptionsCodeGeneration.generate(rpc.options, schema, Options.METHOD_OPTIONS))
                    newline(before = ",")
                    unindent()
                    add("),")
                }
                unindent()
                newline()
                add("),")
                newline()
                add("options = %L", RawOptionsCodeGeneration.generate(service.options, schema, Options.SERVICE_OPTIONS))
                newline()
                unindent()
                add(")")
            }
        ).build()
    }
}