package org.timemates.rrpc.generator.kotlin.adapter.client

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import org.timemates.rrpc.codegen.typemodel.PoetAnnotations
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.RSService
import org.timemates.rrpc.generator.kotlin.adapter.options.ClientOptionsPropertyGenerator
import org.timemates.rrpc.generator.kotlin.adapter.internal.ImportRequirement

public object ClientServiceGenerator {

    public data class Result(val typeSpec: TypeSpec, val imports: List<ImportRequirement>)

    public fun generateService(
        service: RSService,
        resolver: RSResolver,
    ): Result {
        val className = ClassName("", "${service.name}Client")

        val functions = service.rpcs.map { rpc ->
            ClientRpcGenerator.generateRpc(service.name, rpc, resolver)
        }

        val (optionsProperty, imports) = ClientOptionsPropertyGenerator.generate(
            optionsMap = service.rpcs.associate { it.name to it.options },
            resolver = resolver,
        )

        return Result(
            typeSpec = TypeSpec.classBuilder(className)
                .addAnnotation(
                    PoetAnnotations.OptIn(PoetAnnotations.InternalRRpcAPI)
                )
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("config", LibClassNames.RRpcClientConfig)
                        .build()
                ).superclass(LibClassNames.RRpcClientService)
                .addSuperclassConstructorParameter("config")
                .addFunctions(functions)
                .addProperty(optionsProperty)
                .build(),
            imports = imports,
        )
    }
}