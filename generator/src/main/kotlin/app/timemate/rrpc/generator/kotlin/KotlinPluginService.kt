package app.timemate.rrpc.generator.kotlin

import app.timemate.rrpc.generator.GeneratorContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import app.timemate.rrpc.generator.plugin.api.GenerationOptions
import app.timemate.rrpc.generator.plugin.api.logger.RLogger
import app.timemate.rrpc.generator.plugin.api.GenerationPluginService
import app.timemate.rrpc.generator.plugin.api.communication.OptionDescriptor
import app.timemate.rrpc.generator.plugin.api.communication.toOptionDescriptor
import app.timemate.rrpc.proto.schema.RSFile
import app.timemate.rrpc.generator.plugin.api.RSResolver
import app.timemate.rrpc.generator.kotlin.options.*
import app.timemate.rrpc.generator.kotlin.processors.FileProcessor
import app.timemate.rrpc.generator.kotlin.processors.metadata.CompoundFilesMetadataProcessor
import app.timemate.rrpc.generator.plugin.api.result.ProcessingError
import app.timemate.rrpc.generator.plugin.api.result.flatten
import app.timemate.rrpc.generator.plugin.api.result.getOrElse
import app.timemate.rrpc.generator.plugin.api.result.map
import app.timemate.rrpc.generator.plugin.api.result.onFailure
import app.timemate.rrpc.generator.plugin.api.result.onSuccess
import app.timemate.rrpc.proto.schema.RSMessage
import app.timemate.rrpc.proto.schema.RSTypeMemberUrl
import kotlin.math.log
import kotlin.sequences.forEach

public object KotlinPluginService : GenerationPluginService {
    override val options: List<OptionDescriptor> = listOf(
        GenerationOptions.KOTLIN_CLIENT_GENERATION,
        GenerationOptions.KOTLIN_SERVER_GENERATION,
        GenerationOptions.KOTLIN_TYPE_GENERATION,
        GenerationOptions.METADATA_GENERATION,
        GenerationOptions.METADATA_SCOPE_NAME,
        GenerationOptions.METADATA_GENERATION,
        GenerationOptions.METADATA_SCOPE_NAME,
        GenerationOptions.ADAPT_NAMES,
        GenerationOptions.MESSAGE_DATA_MODIFIER,
    ).map { it.toOptionDescriptor() }

    override val name: String = "rrpc-kotlin-gen"

    override suspend fun generateCode(
        options: GenerationOptions,
        files: List<RSFile>,
        logger: RLogger,
    ) {
        try {
            withContext(Dispatchers.IO) {
                val options = KotlinPluginOptions(options)

                FileSystem.SYSTEM.deleteRecursively(options.output)
                FileSystem.SYSTEM.createDirectories(options.output)

                if (options.isServerGenerationEnabled)
                    logger.lifecycle("Configured to generate server stubs.")
                else logger.lifecycle("Configured not to generate server stubs.")

                if (options.isClientGenerationEnabled)
                    logger.lifecycle("Configured to generate client-specific code.")
                else logger.lifecycle("Configured not to generate client-specific code.")

                if (!options.isTypesGenerationEnabled)
                    logger.lifecycle("Configured not to generate proto types.")

                val resolver = RSResolver(files)


                val context = GeneratorContext(
                    options,
                    emptyMap(),
                    logger,
                    resolver,
                )

                val unpermittedGoogleProtoFiles = listOf("any", "duration", "empty", "struct", "timestamp", "wrappers")

                resolver.resolveAvailableFiles().toList().filterNot {
                    it.name in unpermittedGoogleProtoFiles && it.packageName?.value == "google.protobuf"
                }.map { file ->
                    FileProcessor.process(file, context)
                }.flatten().getOrElse {
                    printPrettyErrors(logger, it.errors)
                    return@withContext
                }.filter {
                    it.funSpecs.isNotEmpty() || it.members.isNotEmpty() || it.propertySpecs.isNotEmpty()
                }.forEach { spec ->
                    logger.debug("Writing file: ${spec.relativePath}")
                    spec.writeTo(directory = options.output.toNioPath())
                }

                if (options.metadataGeneration) {
                    logger.lifecycle("Configured to generate metadata code.")
                    CompoundFilesMetadataProcessor.process(
                        resolver.resolveAvailableFiles().toList(),
                        context,
                    ).onSuccess { files ->
                        files.forEach {
                            logger.debug("Writing file: ${it.relativePath}")
                            it.writeTo(options.output.toNioPath())
                        }
                    }.onFailure {
                        printPrettyErrors(logger, it.errors)
                    }
                } else {
                    logger.lifecycle("Configured not to generate metadata code.")
                }
            }
        } catch (e: Exception) {
            logger.error(e.stackTraceToString())
        }
    }

    private suspend fun printPrettyErrors(
        logger: RLogger,
        list: List<ProcessingError>
    ) {
        if (list.isEmpty()) return

        val errorMessage = buildString {
            appendLine("Kotlin code generation failed with the following errors:")
            appendLine()

            list.forEachIndexed { index, error ->
                appendLine("Error #${index + 1}")
                appendLine("-".repeat(60))
                appendLine(error.message.trim())
                appendLine()
            }

            appendLine("-".repeat(60))
            appendLine("Total errors: ${list.size}")
        }

        logger.error(errorMessage)
    }

    override val description: String = """
        Kotlin Code Generator for rRPC: supports base client/server generation 
        as well as schema metadata generation.
        """.trimIndent()
}