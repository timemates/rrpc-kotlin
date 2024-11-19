package org.timemates.rrpc.generator.kotlin.adapter

import okio.FileSystem
import org.timemates.rrpc.codegen.adapters.SchemaAdapter
import org.timemates.rrpc.codegen.configuration.GenerationOption
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.codegen.exception.GenerationException
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.annotations.NonPlatformSpecificAccess
import org.timemates.rrpc.generator.kotlin.options.*

public object KotlinSchemaAdapter : SchemaAdapter {
    override val options: List<GenerationOption> = listOf(
        GenerationOptions.KOTLIN_OUTPUT,
        GenerationOptions.KOTLIN_CLIENT_GENERATION,
        GenerationOptions.KOTLIN_SERVER_GENERATION,
        GenerationOptions.KOTLIN_TYPE_GENERATION,
    ) + KotlinMetadataSchemaAdapter.options

    @OptIn(NonPlatformSpecificAccess::class)
    override fun process(
        options: GenerationOptions,
        resolver: RSResolver,
    ): Unit = with(KotlinPluginOptions(options)) {
        FileSystem.SYSTEM.deleteRecursively(output)

        resolver.resolveAvailableFiles().filterNot {
            it.packageName.value.startsWith("google.protobuf") ||
                it.packageName.value.startsWith("wire")
        }.map { file ->
            FileGenerator.generateFile(
                resolver = resolver,
                file = file,
                clientGeneration = isClientGenerationEnabled,
                serverGeneration = isServerGenerationEnabled,
            )
        }.forEach { spec ->
            spec.writeTo(directory = output.toNioPath())
        }

        if (metadataGeneration)
            KotlinMetadataSchemaAdapter.process(options, resolver)
    }
}