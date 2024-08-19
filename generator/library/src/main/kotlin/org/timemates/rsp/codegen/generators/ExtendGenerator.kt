package org.timemates.rsp.codegen.generators

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.wire.schema.Extend
import com.squareup.wire.schema.Options
import com.squareup.wire.schema.ProtoType
import com.squareup.wire.schema.Schema
import org.timemates.rsp.codegen.exception.GenerationException
import org.timemates.rsp.codegen.typemodel.Types

public object ExtendGenerator {
    public fun generateExtend(extend: Extend, schema: Schema): List<PropertySpec> {
        return when(extend.type) {
            // we don't support for now options generation for anything except methods, files and services.
            Options.FIELD_OPTIONS,
            Options.MESSAGE_OPTIONS,
            Options.ONEOF_OPTIONS,
            Options.ENUM_OPTIONS,
            Options.ENUM_VALUE_OPTIONS -> emptyList()

            Options.METHOD_OPTIONS,
            Options.FILE_OPTIONS,
            Options.SERVICE_OPTIONS,
                -> extend.fields.mapNotNull {
                OptionGenerator.generateOption(
                    field = it,
                    type = getClassNameFromExtendType(extend.type ?: return@mapNotNull null),
                    schema = schema,
                )
            }
            else -> throw GenerationException("Extending messages are not supported.")
        }
    }

    private fun getClassNameFromExtendType(type: ProtoType): ClassName {
        return when (type) {
            Options.METHOD_OPTIONS -> Types.Option.RPC
            Options.SERVICE_OPTIONS -> Types.Option.Service
            Options.FILE_OPTIONS -> Types.Option.File
            else -> throw GenerationException("Should not reach this state.")
        }
    }
}