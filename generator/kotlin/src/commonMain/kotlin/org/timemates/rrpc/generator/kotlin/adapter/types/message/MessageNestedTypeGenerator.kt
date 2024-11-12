package org.timemates.rrpc.generator.kotlin.adapter.types.message

import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.RSType
import org.timemates.rrpc.generator.kotlin.adapter.types.TypeGenerator

internal object MessageNestedTypeGenerator {
    fun generateNestedTypes(incoming: RSType.Message, schema: RSResolver): List<TypeGenerator.Result> {
        return incoming.nestedTypes.mapNotNull { TypeGenerator.generateType(it, schema) }
    }
}