package org.timemates.rsp.codegen.generators.client

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.Schema
import com.squareup.wire.schema.Service
import org.timemates.rsp.codegen.generators.options.ClientOptionsPropertyGenerator
import org.timemates.rsp.codegen.typemodel.Annotations
import org.timemates.rsp.codegen.typemodel.ImportRequirement
import org.timemates.rsp.codegen.typemodel.Types

public object ClientServiceGenerator {

    public data class Result(val typeSpec: TypeSpec, val imports: List<ImportRequirement>)

    public fun generateService(service: Service, schema: Schema): Result {
        val className = ClassName("", "${service.name}Client")

        val functions = service.rpcs.map { rpc ->
            ClientRpcGenerator.generateRpc(service.name, rpc, schema)
        }

        val (optionsProperty, imports) = ClientOptionsPropertyGenerator.generate(
            optionsMap = service.rpcs.associate { it.name to it.options },
            schema = schema,
        )

        return Result(
            typeSpec = TypeSpec.classBuilder(className)
                .addAnnotation(
                    Annotations.OptIn(Annotations.InternalRSProtoAPI)
                )
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("config", Types.RSPClientConfig)
                    .build()
                ).superclass(Types.RSPClientService)
                .addSuperclassConstructorParameter("config")
            .addFunctions(functions)
                .addProperty(optionsProperty)
                .build(),
            imports = imports,
        )
    }
}