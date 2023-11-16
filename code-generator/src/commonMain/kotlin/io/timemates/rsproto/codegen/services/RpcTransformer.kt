package io.timemates.rsproto.codegen.services

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.wire.schema.Rpc
import io.timemates.rsproto.codegen.Types
import io.timemates.rsproto.codegen.asClassName

internal object RpcTransformer {
    fun transform(rpc: Rpc): FunSpec {
        val (requestType, returnType) = getRpcType(rpc)

        return FunSpec.builder(rpc.name)
            .addKdoc(rpc.documentation)
            .addModifiers(KModifier.ABSTRACT)
            .addParameter(
                ParameterSpec.builder(
                    "request",
                    requestType,
                ).build()
            )
            .returns(returnType)
            .build()
    }

    private fun getRpcType(rpc: Rpc): Pair<TypeName, TypeName> {
        val requestClassName = rpc.requestType!!.asClassName()
        val responseClassName = rpc.requestType!!.asClassName()

        return (if (rpc.requestStreaming) Types.flow(requestClassName) else requestClassName) to
            (if (rpc.responseStreaming) Types.flow(responseClassName) else responseClassName)
    }
}