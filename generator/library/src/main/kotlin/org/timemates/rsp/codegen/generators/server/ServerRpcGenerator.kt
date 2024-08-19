package org.timemates.rsp.codegen.generators.server

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.wire.schema.Rpc
import com.squareup.wire.schema.Schema
import org.timemates.rsp.codegen.ext.*
import org.timemates.rsp.codegen.ext.asClassName
import org.timemates.rsp.codegen.ext.decapitalized
import org.timemates.rsp.codegen.ext.deprecated
import org.timemates.rsp.codegen.ext.isDeprecated
import org.timemates.rsp.codegen.typemodel.Types

public object ServerRpcGenerator {
    public fun generateRpc(
        rpc: Rpc,
        schema: Schema,
    ): FunSpec {
        val (requestType, returnType) = getRpcType(rpc, schema)

        return FunSpec.builder(rpc.name.decapitalized())
            .addKdoc(rpc.documentation.replace("%", "%%"))
            .addModifiers(KModifier.ABSTRACT)
            .apply {
                if (rpc.isRequestResponse)
                    addModifiers(KModifier.SUSPEND)
            }
            .deprecated(rpc.options.isDeprecated)
            .addParameter("context", Types.RequestContext)
            .addParameter(
                "request",
                requestType,
            )
            .returns(returnType)
            .build()
    }

    private fun getRpcType(rpc: Rpc, schema: Schema): Pair<TypeName, TypeName> {
        val requestClassName = rpc.requestType!!.asClassName(schema)
        val responseClassName = rpc.responseType!!.asClassName(schema)

        return (if (rpc.requestStreaming) Types.Flow(requestClassName) else requestClassName) to
                (if (rpc.responseStreaming) Types.Flow(responseClassName) else responseClassName)
    }
}