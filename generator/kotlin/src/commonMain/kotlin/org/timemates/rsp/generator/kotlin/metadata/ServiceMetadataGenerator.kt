package org.timemates.rsp.generator.kotlin.metadata

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import org.timemates.rrpc.codegen.typemodel.Types
import org.timemates.rrpc.common.metadata.RMService
import org.timemates.rrpc.generator.kotlin.ext.newline

internal object ServiceMetadataGenerator {
    fun generate(service: RMService): CodeBlock {
        return buildCodeBlock {
            addStatement("%T(", Types.RM.Service)
            withIndent {
                addStatement("name = %S,", service.name)
                addStatement("rpcs = listOf(").withIndent {
                    service.rpcs.forEach { rpc ->
                        newline()
                        add(RpcMetadataGenerator.generate(rpc))
                        newline(before = ",")
                    }
                }
                addStatement("),")
                add("options = %P,", OptionsMetadataGenerator.generate(service.options))
                newline()
                addStatement(
                    format = "typeUrl = %T(%S),",
                    Types.RM.Value.TypeUrl,
                    service.typeUrl.value,
                )
            }
            addStatement(")")
        }
    }
}