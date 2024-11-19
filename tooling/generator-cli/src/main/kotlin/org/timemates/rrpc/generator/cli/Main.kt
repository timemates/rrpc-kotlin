package org.timemates.rrpc.generator.cli

import com.github.ajalt.clikt.command.main
import kotlinx.coroutines.*
import okio.buffer
import okio.sink
import okio.source
import org.timemates.rrpc.codegen.CodeGenerator
import org.timemates.rrpc.codegen.plugin.GeneratorCommunication
import org.timemates.rrpc.codegen.plugin.data.*
import org.timemates.rrpc.generator.kotlin.adapter.KotlinSchemaAdapter
import java.util.*
import kotlin.system.exitProcess

/**
 * List of options for functionality that is builtin in the rrgcli by default,
 * such as Kotlin Code Generation.
 */
private val BUILTIN_OPTIONS = (CodeGenerator.baseOptions + KotlinSchemaAdapter.options).map { it.toOptionDescriptor() }

private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
    println("rrgcli exit with error: ${exception.message}")
    exitProcess(1)
}

/**
 * The `rrgcli` entry.
 *
 * For usage documentation, please refer to the [official documentation](https://rrpc.timemates.org/codegen-cli.html).
 */
fun main(args: Array<String>): Unit = runBlocking(exceptionHandler) {
    // Phase 1: Load in the specified plugins
    // accepts both --plugin=X and --plugin="X"
    // also, may include commands to be run, like "java -jar ..."
    val plugins = args.filter { it.startsWith("--plugin=") }
        .map { it.removePrefix("--plugin=\"") }
        .map {
            val callable = it.substringBeforeLast("\"")
            println("Loading plugin: $callable")

            val process = ProcessBuilder(callable.split(" ") + args)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()

            val communication = GeneratorCommunication(
                input = process.inputStream.source().buffer(),
                output = process.outputStream.sink().buffer(),
            )

            Plugin(callable, communication, process)
        }

    val scope = CoroutineScope(Dispatchers.IO)

    // Phase 2: ask plugin about its options
    val pluginsOptions = plugins.map { plugin ->
        val signalId = SignalId(UUID.randomUUID().toString())

        scope.async {
            launch {
                plugin.communication.send(
                    GeneratorMessage {
                        id = signalId
                        signal = GeneratorSignal.FetchOptionsList
                    }
                )
            }

            while (plugin.communication.incoming.hasNext()) {
                val signal = plugin.communication.incoming.next().signal

                if (signal is PluginSignal.SendOptions) {
                    return@async signal.options
                } else if (signal is PluginSignal.RequestStatusChange.Failed) {
                    error("Plugin '$plugin' returned an error: ${signal.message}")
                }
            }

            error("Plugin '$plugin' did not provide list of options.")
        }
    }.awaitAll().flatten()

    println("Generation is started...")

    // Phase 3: Start generation command with received custom options
    GenerateCommand(plugins, BUILTIN_OPTIONS + pluginsOptions, scope).main(args)

    println("Action is finished.")

    // Phase 4: Finish all the processes and exiting the program
    println("Cleaning up resources...")

    plugins.forEach { plugin ->
        plugin.communication.close()
        plugin.process.destroy()
    }

    exitProcess(0)
}