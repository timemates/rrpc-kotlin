package org.timemates.rrpc.generator.kotlin.adapter.options

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.RSOptions
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.kotlinPackage
import org.timemates.rrpc.common.schema.value.RMDeclarationUrl
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.newline
import org.timemates.rrpc.generator.kotlin.adapter.internal.ImportRequirement

internal object RawOptionsCodeGeneration {
    fun generate(
        options: RSOptions,
        resolver: RSResolver,
        optionsType: RMDeclarationUrl,
    ): CodeBlock {
        val imports = mutableListOf<ImportRequirement>()

        return buildCodeBlock {
            add("%T(", LibClassNames.Options)

            if (options.list.isEmpty()) {
                add("emptyMap())")
                return@buildCodeBlock
            }

            newline()
            indent()

            options.list.forEach { option ->
                val field = resolver.resolveField(option.fieldUrl)!!
                val type = field.typeUrl

                resolver.resolveFileOf(type)
                    ?.kotlinPackage()
                    ?.let {
                        imports.add(ImportRequirement(it, listOf(field.name)))
                    }

                add(
                    format = "%T.${option.name} to ",
                    when (optionsType) {
                        RSOptions.METHOD_OPTIONS -> LibClassNames.Option.RPC
                        RSOptions.SERVICE_OPTIONS -> LibClassNames.Option.Service
                        else -> error("Unsupported type of option: ${field.typeUrl}")
                    }
                )
                add(OptionValueGenerator.generate(type, option.value, resolver))
                newline(before = ",")
            }

            unindent()
            add(")")
        }
    }
}