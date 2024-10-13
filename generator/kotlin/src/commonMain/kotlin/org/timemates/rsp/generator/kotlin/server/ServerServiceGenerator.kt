package org.timemates.rrpc.generator.kotlin.server

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import org.timemates.rrpc.common.metadata.RMResolver
import org.timemates.rrpc.codegen.typemodel.Types
import org.timemates.rrpc.common.metadata.RMService
import org.timemates.rrpc.generator.kotlin.annotation.ExperimentalSpecModifierApi
import org.timemates.rrpc.generator.kotlin.modifier.ModifiersRegistry
import org.timemates.rrpc.generator.kotlin.modifier.ServerServiceModifier

public object ServerServiceGenerator {
    @OptIn(ExperimentalSpecModifierApi::class)
    public fun generateService(
        service: RMService,
        resolver: RMResolver,
        modifiersRegistry: ModifiersRegistry,
    ): TypeSpec {
        return TypeSpec.classBuilder(service.name)
            .addModifiers(KModifier.ABSTRACT)
            .superclass(Types.RRpcServerService)
            .addProperty(ServerMetadataGenerator.generateMetadata(service, resolver))
            .addFunctions(service.rpcs.map { ServerRpcGenerator.generateRpc(it, resolver) })
            .build()
            .let { spec -> modifiersRegistry.modified(ServerServiceModifier, spec, service, resolver) }
    }
}