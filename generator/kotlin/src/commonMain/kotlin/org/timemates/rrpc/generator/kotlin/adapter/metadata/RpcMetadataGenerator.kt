package org.timemates.rrpc.generator.kotlin.adapter.metadata

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.RSRpc
import org.timemates.rrpc.common.schema.annotations.NonPlatformSpecificAccess
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.newline

internal object RpcMetadataGenerator {
    fun generate(rpc: RSRpc, resolver: RSResolver): CodeBlock {
        return buildCodeBlock {
            add("%T(", LibClassNames.RM.Rpc)
            withIndent {
                newline()
                @OptIn(NonPlatformSpecificAccess::class)
                addStatement("name = %S,", rpc.name)
                addStatement(
                    format = "requestType = %1T(isStreaming = %2L, %3T(%4S)),",
                    LibClassNames.RM.StreamableTypeUrl,
                    rpc.requestType.isStreaming,
                    LibClassNames.RM.Value.TypeUrl,
                    rpc.requestType.type.value
                )
                addStatement(
                    format = "responseType = %1T(isStreaming = %2L, %3T(%4S)),",
                    LibClassNames.RM.StreamableTypeUrl,
                    rpc.responseType.isStreaming,
                    LibClassNames.RM.Value.TypeUrl,
                    rpc.responseType.type.value
                )
                addStatement("options = %L,", OptionsMetadataGenerator.generate(rpc.options, resolver))
                addStatement(
                    "documentation = %L,",
                    if (rpc.documentation.isNullOrEmpty()) "null" else "\"${rpc.documentation}\""
                )
            }
            add(")")
        }
    }
}