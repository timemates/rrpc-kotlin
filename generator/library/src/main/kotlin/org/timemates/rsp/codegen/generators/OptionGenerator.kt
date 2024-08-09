package org.timemates.rsp.codegen.generators

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.wire.schema.Field

public object OptionGenerator {
    public fun generateOption(field: Field, type: ClassName): PropertySpec =
        PropertySpec.builder(field.name, type)
            .addKdoc(field.documentation)
            .receiver(type.nestedClass("Companion"))
            .delegate("lazy·{·%T(%S, %L)·}", type, field.name, field.tag)
            .build()
}