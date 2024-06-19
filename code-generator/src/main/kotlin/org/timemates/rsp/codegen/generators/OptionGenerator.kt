package org.timemates.rsp.codegen.generators

import com.squareup.kotlinpoet.PropertySpec
import com.squareup.wire.schema.Field
import org.timemates.rsp.codegen.Types

public object OptionGenerator {
    public fun generateOption(field: Field): PropertySpec =
        PropertySpec.builder(field.name, Types.rsprotoOption)
            .addKdoc(field.documentation)
            .initializer("%T(%S, %L)", field.tag)
            .build()
}