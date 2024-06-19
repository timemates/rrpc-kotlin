package org.timemates.rsp.codegen.generators.client

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec

public object ClientServiceBuilderGenerator {
    private val BUILDER_SUPERCLASS_TYPE = ClassName(
        packageName = "org.timemates.rsp.client",
        "RSPServiceApi"
    )

    public fun generateBuilder(serviceName: ClassName): TypeSpec {
        return TypeSpec.classBuilder(serviceName.simpleName)
            .superclass(BUILDER_SUPERCLASS_TYPE)
            .addSuperclassConstructorParameter("constructor = ::%T", serviceName)
            .build()
    }
}