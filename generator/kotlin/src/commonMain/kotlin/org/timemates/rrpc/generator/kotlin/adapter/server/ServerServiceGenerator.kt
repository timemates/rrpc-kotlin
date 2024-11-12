package org.timemates.rrpc.generator.kotlin.adapter.server

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.RSService

public object ServerServiceGenerator {
    public fun generateService(
        service: RSService,
        resolver: RSResolver,
    ): TypeSpec {
        return TypeSpec.classBuilder(service.name)
            .addModifiers(KModifier.ABSTRACT)
            .superclass(LibClassNames.RRpcServerService)
            .addProperty(ServerMetadataGenerator.generateMetadata(service, resolver))
            .addFunctions(service.rpcs.map { ServerRpcGenerator.generateRpc(it, resolver) })
            .build()
    }
}