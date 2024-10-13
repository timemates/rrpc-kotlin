package org.timemates.rrpc.generator.kotlin.types.message

import org.timemates.rrpc.common.metadata.RMResolver
import org.timemates.rrpc.common.metadata.RMType
import org.timemates.rrpc.generator.kotlin.types.TypeGenerator

internal object MessageNestedTypeGenerator {
    fun generateNestedTypes(incoming: RMType.Message, schema: RMResolver): List<TypeGenerator.Result> {
        return incoming.nestedTypes.mapNotNull { TypeGenerator.generateType(it, schema) }
    }
}