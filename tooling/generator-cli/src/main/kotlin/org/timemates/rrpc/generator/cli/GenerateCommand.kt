package org.timemates.rrpc.generator.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import okio.FileSystem
import org.timemates.rrpc.codegen.CodeGenerator
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.codegen.configuration.OptionTypeKind
import org.timemates.rrpc.codegen.configuration.RepeatableGenerationOption
import org.timemates.rrpc.codegen.configuration.SingleGenerationOption
import org.timemates.rrpc.codegen.plugin.data.GeneratorMessage
import org.timemates.rrpc.codegen.plugin.data.GeneratorSignal
import org.timemates.rrpc.codegen.plugin.data.OptionDescriptor
import org.timemates.rrpc.codegen.plugin.data.PluginSignal
import org.timemates.rrpc.codegen.plugin.data.SignalId
import org.timemates.rrpc.generator.kotlin.adapter.KotlinSchemaAdapter
import java.util.UUID

class GenerateCommand(
    private val plugins: List<Plugin>,
    options: List<OptionDescriptor>,
    private val scope: CoroutineScope,
) : SuspendingCliktCommand("rrgcli") {
    private val registeredOptions = options
        .map { option ->
            option to option(
                names = arrayOf("--${option.name}"),
                envvar = option.name.uppercase(),
                help = option.description,
            ).applyTypeDescriptor(option.kind).also { registerOption(it) }
        }

    override suspend fun run() {
        val genOptions = GenerationOptions.create {
            registeredOptions.forEach { (definition, raw) ->
                val value = when (raw) {
                    is OptionWithValues<*, *, *> -> raw.value?.toString()
                    else -> null
                }

                when (definition.isRepeatable) {
                    true -> rawAppend(definition.name, value ?: return@forEach)
                    false -> rawSet(definition.name, value ?: return@forEach)
                }
            }
        }

        val resolver = CodeGenerator(FileSystem.SYSTEM)
            .generate(
                options = genOptions,
                adapters = listOf(KotlinSchemaAdapter),
            )

        plugins.map { plugin ->
            scope.launch {
                launch {
                    while (plugin.communication.incoming.hasNext()) {
                        val message = plugin.communication.incoming.next()
                        when (message.signal) {
                            is PluginSignal.RequestStatusChange.Failed -> error("Plugin '${plugin.name}' is failed: ${(message.signal as PluginSignal.RequestStatusChange.Failed).message}")
                            PluginSignal.RequestInput -> { /* ignored: already sent */ }
                            PluginSignal.RequestStatusChange.Accepted -> {
                                echo(
                                    message = "Plugin '${plugin.name}' has received schema and successfully started generation.",
                                    trailingNewline = true,
                                )
                            }
                            is PluginSignal.SendOptions -> { /* ignored: should've received before */ }
                            is PluginSignal.RequestStatusChange.Finished -> return@launch
                        }
                    }
                }

                plugin.communication.send(
                    GeneratorMessage {
                        id = SignalId(UUID.randomUUID().toString())
                        signal = GeneratorSignal.SendInput(resolver.resolveAvailableFiles().toList())
                    }
                )
            }
        }.joinAll()
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