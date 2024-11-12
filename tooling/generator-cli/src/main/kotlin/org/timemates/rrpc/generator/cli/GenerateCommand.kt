package org.timemates.rrpc.generator.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import okio.FileSystem
import org.timemates.rrpc.codegen.CodeGenerator
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.codegen.configuration.OptionTypeKind
import org.timemates.rrpc.codegen.configuration.RepeatableGenerationOption
import org.timemates.rrpc.codegen.configuration.SingleGenerationOption
import org.timemates.rrpc.generator.kotlin.adapter.KotlinSchemaAdapter

class GenerateCommand : CliktCommand("rrgcli") {
    // simplified for now as we don't support plugins just yet.
    private val adaptersOptions = (CodeGenerator.baseOptions + KotlinSchemaAdapter.options)
        .map { option ->
            option to option(
                names = arrayOf("--${option.name}"),
                envvar = option.name.uppercase(),
                help = option.description.orEmpty(),
            ).applyTypeDescriptor(option.valueKind).also { registerOption(it) }
        }

    override fun run() {
        val genOptions = GenerationOptions.create {
            adaptersOptions.forEach { (definition, raw) ->
                val value = when (raw) {
                    is OptionWithValues<*, *, *> -> raw.value?.toString()
                    else -> null
                }

                when (definition) {
                    is RepeatableGenerationOption<*> -> {
                        @Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
                        val definition = definition as RepeatableGenerationOption<Any>
                        append(definition, value ?: return@forEach)
                    }
                    is SingleGenerationOption<*> -> {
                        @Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
                        val definition = definition as SingleGenerationOption<Any>
                        set(definition, value ?: return@forEach)
                    }
                }
            }
        }
        CodeGenerator(FileSystem.SYSTEM).generate(genOptions, adapters = listOf(KotlinSchemaAdapter))
    }
}

private fun RawOption.applyTypeDescriptor(typeKind: OptionTypeKind): Option {
    return when (typeKind) {
        OptionTypeKind.Boolean -> boolean()
        is OptionTypeKind.Choice -> choice(*typeKind.variants.toTypedArray())
        OptionTypeKind.Number.Int -> int()
        OptionTypeKind.Number.Long -> long()
        OptionTypeKind.Number.Float -> float()
        OptionTypeKind.Number.Double -> double()
        OptionTypeKind.Path -> path()
        OptionTypeKind.Text -> this
    }
}