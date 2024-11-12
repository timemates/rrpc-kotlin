package org.timemates.rrpc.generator.kotlin.options

import okio.Path
import okio.Path.Companion.toPath
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.codegen.exception.GenerationException

@JvmInline
public value class KotlinPluginOptions(private val options: GenerationOptions) {
    public val isClientGenerationEnabled: Boolean get() = options[GenerationOptions.KOTLIN_CLIENT_GENERATION] ?: true
    public val isServerGenerationEnabled: Boolean get() = options[GenerationOptions.KOTLIN_SERVER_GENERATION] ?: true
    public val isTypesGenerationEnabled: Boolean get() = options[GenerationOptions.KOTLIN_TYPE_GENERATION] ?: true
    public val output: Path get() = options[GenerationOptions.KOTLIN_OUTPUT] ?: throw GenerationException("Kotlin output folder was not specified.")

    public val metadataGeneration: MetadataGenerationType get() =
        options[GenerationOptions.METADATA_GENERATION]
            ?: MetadataGenerationType.DISABLED
    public val metadataScopeName: String? get() = options[GenerationOptions.METADATA_SCOPE_NAME]
}