package org.timemates.rsp.codegen.generators.types.message

import com.squareup.wire.schema.MessageType
import com.squareup.wire.schema.Schema
import org.timemates.rsp.codegen.generators.types.TypeGenerator

internal object MessageNestedTypeGenerator {
    fun generateNestedTypes(incoming: MessageType, schema: Schema): List<TypeGenerator.Result> {
        return incoming.nestedTypes.map { TypeGenerator.generateType(it, schema) }
    }
}