package org.timemates.rrpc.generator.kotlin.adapter.metadata

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.withIndent
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.RSFile
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.annotations.NonPlatformSpecificAccess
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.newline

internal object FileMetadataGenerator {
    fun generate(file: RSFile, resolver: RSResolver): CodeBlock {
        return CodeBlock.builder().apply {
            addStatement("%T(", LibClassNames.RM.File).withIndent {
                addStatement("name = %S,", file.name)
                @OptIn(NonPlatformSpecificAccess::class)
                addStatement("packageName = %T(%S),", LibClassNames.RM.Value.PackageName, file.packageName.value)
                addStatement("options = %L,", OptionsMetadataGenerator.generate(file.options, resolver))
                if (file.services.isNotEmpty()) {
                    add("services = listOf(")
                    withIndent {
                        file.services.forEach { service ->
                            newline()
                            add(ServiceMetadataGenerator.generate(service, resolver))
                        }
                    }
                    add("),")
                } else {
                    add("services = emptyList(),")
                }
                newline()
                if (file.services.isNotEmpty()) {
                    add("extends = listOf(")
                    withIndent {
                        file.extends.forEach { extend ->
                            newline()
                            add(ExtendMetadataGenerator.generate(extend, resolver))
                            add(",")
                        }
                    }
                    newline()
                    add("),")
                } else {
                    add("extends = emptyList(),")
                }
            }
            newline()
            add(")")
        }.build()
    }
}