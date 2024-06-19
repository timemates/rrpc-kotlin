package org.timemates.rsp.codegen.generators

import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.Extend
import com.squareup.wire.schema.Options
import org.timemates.rsp.codegen.GenerationException

public object ExtendGenerator {
    public fun generateExtend(extend: Extend): TypeSpec {
        return when(extend.type) {
            Options.METHOD_OPTIONS,
            Options.ENUM_OPTIONS,
            Options.ENUM_VALUE_OPTIONS,
            Options.FILE_OPTIONS,
            Options.FIELD_OPTIONS,
            Options.MESSAGE_OPTIONS,
            Options.ONEOF_OPTIONS,
            Options.SERVICE_OPTIONS -> TypeSpec.objectBuilder(extend.name)
                .addKdoc(extend.documentation)
                .addProperties(extend.fields.map(OptionGenerator::generateOption))
                .build()
            else -> throw GenerationException("Extending messages are not supported.")
        }
    }
}