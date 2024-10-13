package org.timemates.rrpc.generator.kotlin.metadata

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.withIndent
import org.timemates.rrpc.codegen.typemodel.Types
import org.timemates.rrpc.common.metadata.RMFile
import org.timemates.rrpc.common.metadata.annotations.NonPlatformSpecificAccess
import org.timemates.rrpc.generator.kotlin.ext.newline
import org.timemates.rsp.generator.kotlin.metadata.ExtendMetadataGenerator
import org.timemates.rsp.generator.kotlin.metadata.OptionsMetadataGenerator
import org.timemates.rsp.generator.kotlin.metadata.ServiceMetadataGenerator

internal object FileMetadataGenerator {
    fun generate(file: RMFile): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("%T(", Types.RM.File)
            indent()
            addStatement("name = %S,", file.name)
            @OptIn(NonPlatformSpecificAccess::class)
            addStatement("packageName = %T(%S),", Types.RM.Value.PackageName, file.packageName)
            addStatement("options = %P,", OptionsMetadataGenerator.generate(file.options))
            addStatement("services = listOf(")
            withIndent {
                file.services.forEach { service ->
                    newline()
                    add(ServiceMetadataGenerator.generate(service))
                }
            }
            addStatement("),")
            addStatement("extends = listOf(")
            withIndent {
                file.extends.forEach { extend ->
                    newline()
                    add(ExtendMetadataGenerator.generate(extend))
                    add(",")
                }
            }
            addStatement("),")
        }.build()
    }
}