package org.timemates.rrpc.generator.kotlin.adapter.metadata

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.RSService
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.newline

internal object ServiceMetadataGenerator {
    fun generate(service: RSService, resolver: RSResolver): CodeBlock {
        return buildCodeBlock {
            addStatement("%T(", LibClassNames.RM.Service)
            withIndent {
                addStatement("name = %S,", service.name)
                add("rpcs = listOf(").withIndent {
                    service.rpcs.forEach { rpc ->
                        newline()
                        add(RpcMetadataGenerator.generate(rpc, resolver))
                        add(",")
                    }
                }
                newline()
                add("),")
                newline()
                add("options = %L,", OptionsMetadataGenerator.generate(service.options, resolver))
                newline()
                add(
                    format = "typeUrl = %T(%S),",
                    LibClassNames.RM.Value.TypeUrl,
                    service.typeUrl.value,
                )
            }
            newline()
            add(")")
        }
    }
}