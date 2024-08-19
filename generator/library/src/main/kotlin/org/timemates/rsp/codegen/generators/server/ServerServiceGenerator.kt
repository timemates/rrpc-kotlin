package org.timemates.rsp.codegen.generators.server

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.Schema
import com.squareup.wire.schema.Service
import org.timemates.rsp.codegen.typemodel.Types

public object ServerServiceGenerator {
    public fun generateService(service: Service, schema: Schema): TypeSpec {
        return TypeSpec.classBuilder(service.name)
            .addModifiers(KModifier.ABSTRACT)
            .superclass(Types.RSPServerService)
            .addProperty(ServerMetadataGenerator.generateMetadata(service, schema))
            .addFunctions(service.rpcs.map { ServerRpcGenerator.generateRpc(it, schema) })
            .build()
    }
}