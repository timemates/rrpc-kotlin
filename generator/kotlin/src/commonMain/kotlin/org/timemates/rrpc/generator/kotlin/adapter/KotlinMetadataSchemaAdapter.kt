package org.timemates.rrpc.generator.kotlin.adapter

import org.timemates.rrpc.codegen.adapters.SchemaAdapter
import org.timemates.rrpc.codegen.configuration.GenerationOption
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.codegen.exception.GenerationException
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.annotations.NonPlatformSpecificAccess
import org.timemates.rrpc.generator.kotlin.adapter.metadata.CombinedFilesMetadataGenerator
import org.timemates.rrpc.generator.kotlin.options.KotlinPluginOptions
import org.timemates.rrpc.generator.kotlin.options.METADATA_GENERATION
import org.timemates.rrpc.generator.kotlin.options.METADATA_SCOPE_NAME
import org.timemates.rrpc.generator.kotlin.options.MetadataGenerationType

public object KotlinMetadataSchemaAdapter : SchemaAdapter {
    override val options: List<GenerationOption> = listOf(
        GenerationOptions.METADATA_GENERATION,
        GenerationOptions.METADATA_SCOPE_NAME,
    )

    @OptIn(NonPlatformSpecificAccess::class)
    override fun process(
        options: GenerationOptions,
        resolver: RSResolver,
    ): RSResolver = with(KotlinPluginOptions(options)) {
        if (metadataGeneration == MetadataGenerationType.SCOPED && metadataScopeName == null)
            throw GenerationException("Metadata config requires generation to be scoped, but name isn't provided.")

        resolver.resolveAvailableFiles()
            .filterNot { file -> file.packageName.value.startsWith("wire") }
            .toList()
            .let {
                CombinedFilesMetadataGenerator.generate(
                    name = metadataScopeName,
                    scoped = metadataGeneration == MetadataGenerationType.SCOPED,
                    resolver = RSResolver(it)
                )
            }.apply {
                writeTo(output.toNioPath())
            }

        return resolver
    }

}