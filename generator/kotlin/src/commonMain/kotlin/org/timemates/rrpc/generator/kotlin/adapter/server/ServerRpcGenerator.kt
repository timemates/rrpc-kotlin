package org.timemates.rrpc.generator.kotlin.adapter.server

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.*
import org.timemates.rrpc.common.schema.value.RMDeclarationUrl
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.asClassName
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.deprecated

public object ServerRpcGenerator {
    public fun generateRpc(
        rpc: RSRpc,
        resolver: RSResolver,
    ): FunSpec {
        val (requestType, returnType) = getRpcType(rpc, resolver)

        return FunSpec.builder(rpc.kotlinName())
            .addKdoc(rpc.documentation?.replace("%", "%%").orEmpty())
            .addModifiers(KModifier.ABSTRACT)
            .deprecated(rpc.options.isDeprecated)
            .addParameter("context", LibClassNames.RequestContext)
            .apply {
                if (rpc.isRequestResponse)
                    addModifiers(KModifier.SUSPEND)

                if (rpc.requestType.type != RMDeclarationUrl.ACK)
                    addParameter(
                        "request",
                        requestType,
                    )

                if (rpc.responseType.type != RMDeclarationUrl.ACK)
                    returns(returnType)
            }
            .build()
    }

    private fun getRpcType(rpc: RSRpc, schema: RSResolver): Pair<TypeName, TypeName> {
       return rpc.requestType.asClassName(schema) to rpc.responseType.asClassName(schema)
    }
}