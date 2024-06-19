package org.timemates.rsp.codegen.services

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.wire.schema.Rpc
import com.squareup.wire.schema.Schema
import org.timemates.rsp.codegen.Types
import org.timemates.rsp.codegen.asClassName
import org.timemates.rsp.codegen.decapitalize

internal object RpcTransformer {
    fun transform(rpc: Rpc, schema: Schema): FunSpec {
        val (requestType, returnType) = getRpcType(rpc, schema)

        return FunSpec.builder(rpc.name.decapitalize())
            .addKdoc(rpc.documentation)
            .addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)
            .addParameter(
                ParameterSpec.builder(
                    "request",
                    requestType,
                ).build()
            )
            .returns(returnType)
            .build()
    }

    private fun getRpcType(rpc: Rpc, schema: Schema): Pair<TypeName, TypeName> {
        val requestClassName = rpc.requestType!!.asClassName(schema)
        val responseClassName = rpc.responseType!!.asClassName(schema)

        return (if (rpc.requestStreaming) Types.flow(requestClassName) else requestClassName) to
            (if (rpc.responseStreaming) Types.flow(responseClassName) else responseClassName)
    }
}