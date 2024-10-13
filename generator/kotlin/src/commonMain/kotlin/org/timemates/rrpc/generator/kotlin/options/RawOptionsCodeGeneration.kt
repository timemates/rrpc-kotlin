package org.timemates.rrpc.generator.kotlin.options

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import org.timemates.rrpc.codegen.typemodel.Types
import org.timemates.rrpc.common.metadata.RMField
import org.timemates.rrpc.common.metadata.RMOptions
import org.timemates.rrpc.common.metadata.RMResolver
import org.timemates.rrpc.common.metadata.kotlinPackage
import org.timemates.rrpc.common.metadata.value.RMTypeUrl
import org.timemates.rrpc.generator.kotlin.ext.newline
import org.timemates.rrpc.generator.kotlin.typemodel.ImportRequirement

internal object RawOptionsCodeGeneration {
    fun generate(
        options: RMOptions,
        resolver: RMResolver,
        optionsType: RMTypeUrl,
    ): CodeBlock {
        val imports = mutableListOf<ImportRequirement>()

        return buildCodeBlock {
            add("%T(", Types.Options)

            if (options.list.isEmpty()) {
                add("emptyMap())")
                return@buildCodeBlock
            }

            newline()
            indent()

            options.list.forEach { option ->
                val field = resolver.resolveField(option.fieldUrl) as RMField
                val type = field.typeUrl

                resolver.resolveFileOf(type)
                    ?.kotlinPackage()
                    ?.let {
                        imports.add(ImportRequirement(it, listOf(field.name)))
                    }

                add(
                    format = "%T.${option.name} to ",
                    when (optionsType) {
                        RMOptions.METHOD_OPTIONS -> Types.Option.RPC
                        RMOptions.SERVICE_OPTIONS -> Types.Option.Service
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