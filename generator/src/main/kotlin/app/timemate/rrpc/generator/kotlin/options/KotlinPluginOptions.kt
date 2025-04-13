package app.timemate.rrpc.generator.kotlin.options

import okio.Path
import app.timemate.rrpc.generator.plugin.api.GenerationOptions

@JvmInline
public value class KotlinPluginOptions(private val options: GenerationOptions) {
    public val isClientGenerationEnabled: Boolean get() = options[GenerationOptions.KOTLIN_CLIENT_GENERATION] ?: true
    public val isServerGenerationEnabled: Boolean get() = options[GenerationOptions.KOTLIN_SERVER_GENERATION] ?: true
    public val isTypesGenerationEnabled: Boolean get() = options[GenerationOptions.KOTLIN_TYPE_GENERATION] ?: true
    public val output: Path
        get() = (options[GenerationOptions.GEN_OUTPUT]
            ?: error("Kotlin output folder was not specified."))

    public val metadataGeneration: Boolean
        get() = options[GenerationOptions.METADATA_GENERATION] == true
    public val metadataScopeName: String? get() = options[GenerationOptions.METADATA_SCOPE_NAME]
    public val adaptNames: Boolean
        get() = options[GenerationOptions.ADAPT_NAMES] != false
    public val messageWithDataModifier: Boolean
        get() = options[GenerationOptions.MESSAGE_DATA_MODIFIER] != false
}