package app.timemate.rrpc.generator.kotlin

import app.timemate.rrpc.generator.GeneratorContext
import app.timemate.rrpc.generator.kotlin.error.KotlinGenerationError
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
import app.timemate.rrpc.generator.plugin.api.result.ProcessingError
import app.timemate.rrpc.generator.plugin.api.result.flatten
import app.timemate.rrpc.generator.plugin.api.result.getOrElse

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
    ).map { it.toOptionDescriptor() }

    override val name: String = "rrpc-kotlin-gen"

    override suspend fun generateCode(
        options: GenerationOptions,
        files: List<RSFile>,
        logger: RLogger,
    ): Unit = withContext(Dispatchers.IO) {
        logger.lifecycle("test started")
        val options = KotlinPluginOptions(options)

        FileSystem.SYSTEM.deleteRecursively(options.output)
        FileSystem.SYSTEM.createDirectories(options.output)

        if (options.isServerGenerationEnabled)
            logger.debug("Configured to generate server stubs.")
        else logger.debug("Configured not to generate server stubs.")

        if (options.isClientGenerationEnabled)
            logger.debug("Configured to generate client-specific code.")
        else logger.debug("Configured not to generate client-specific code.")

        if (!options.isTypesGenerationEnabled)
            logger.debug("Configured not to generate proto types.")

        val resolver = RSResolver(files)

        val context = GeneratorContext(
            options,
            emptyMap(),
            logger,
            resolver,
        )

        logger.lifecycle("test 2")
        resolver.resolveAvailableFiles().toList().filterNot {
            it.packageName?.value?.startsWith("google.protobuf") == true ||
                it.packageName?.value?.startsWith("wire") == true
        }.map { file ->
            FileProcessor.process(file, context)
        }.flatten().getOrElse {
            printPrettyErrors(logger, it.errors)
            return@withContext
        }.forEach { spec ->
            logger.debug("Writing file: ${spec.relativePath}")
            spec.writeTo(directory = options.output.toNioPath())
        }
//        if (options.metadataGeneration) {
//            logger.debug("Configured to generate metadata code.")
//            CombinedFilesMetadataGenerator.generate(
//                name = options.metadataScopeName,
//                resolver = resolver,
//            ).writeTo(options.output.toNioPath())
//        } else {
//            logger.debug("Configured not to generate metadata code.")
//        }
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