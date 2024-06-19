package org.timemates.rsp.codegen.generators.client

import com.squareup.kotlinpoet.*
import com.squareup.wire.schema.Schema
import com.squareup.wire.schema.Service
import org.timemates.rsp.codegen.Types

public object ClientServiceGenerator {
    public fun generateService(service: Service, schema: Schema): TypeSpec {
        val className = ClassName("", service.name)

        val functions = service.rpcs.map { rpc ->
            ClientRpcGenerator.generateRpc(service.name, rpc, schema)
        }

        return TypeSpec.classBuilder(className)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("rsocket", Types.rsocket)
                    .addParameter("interceptors", Types.interceptors)
                    .build()
            )
            .superclass(Types.rspClientService)
            .addSuperclassConstructorParameter("rsocket")
            .addSuperclassConstructorParameter("interceptors")
            .addType(ClientServiceBuilderGenerator.generateBuilder(className))
            .addFunctions(functions)
            .build()
    }
}