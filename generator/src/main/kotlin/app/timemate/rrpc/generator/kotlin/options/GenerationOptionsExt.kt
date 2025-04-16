package app.timemate.rrpc.generator.kotlin.options

import app.timemate.rrpc.generator.plugin.api.GenerationOption
import app.timemate.rrpc.generator.plugin.api.GenerationOptions
import app.timemate.rrpc.generator.plugin.api.OptionTypeKind
import app.timemate.rrpc.generator.plugin.api.SingleGenerationOption

public val GenerationOptions.Companion.KOTLIN_SERVER_GENERATION: SingleGenerationOption<Boolean> by lazy {
    GenerationOption.single(
        name = "server_generation",
        description = "Indicates whether server stubs should be generated for Kotlin. False by default.",
        valueKind = OptionTypeKind.Boolean,
        constructor = { it.toBooleanStrict() }
    )
}

public val GenerationOptions.Companion.KOTLIN_CLIENT_GENERATION: SingleGenerationOption<Boolean> by lazy {
    GenerationOption.single(
        name = "client_generation",
        description = "Indicates whether client stubs should be generated for Kotlin. False by default.",
        valueKind = OptionTypeKind.Boolean,
        constructor = { it.toBooleanStrict() }
    )
}

public val GenerationOptions.Companion.KOTLIN_TYPE_GENERATION: SingleGenerationOption<Boolean> by lazy {
    GenerationOption.single(
        name = "type_generation",
        description = "Indicates whether data types should be generated for Kotlin. False by default.",
        valueKind = OptionTypeKind.Boolean,
        constructor = { it.toBooleanStrict() }
    )
}

public val GenerationOptions.Companion.METADATA_GENERATION: SingleGenerationOption<Boolean> by lazy {
    GenerationOption.single(
        name = "metadata_generation",
        description = "Specifies whether metadata should be generated.",
        valueKind = OptionTypeKind.Boolean,
        constructor = { it.toBooleanStrict() }
    )
}

public val GenerationOptions.Companion.METADATA_SCOPE_NAME: SingleGenerationOption<String> by lazy {
    GenerationOption.single(
        name = "metadata_scope_name",
        description = "Specifies the scope name for metadata generation. If not specified and metadata generation is enabled, it will be global-scoped.",
        valueKind = OptionTypeKind.Text,
        constructor = { it }
    )
}

public val GenerationOptions.Companion.ADAPT_NAMES: SingleGenerationOption<Boolean> by lazy {
    GenerationOption.single(
        name = "adapt_names",
        description = "Specifies whether the kotlin generator should adapt field and other names when generating code. When false, the original name will be retained.",
        valueKind = OptionTypeKind.Boolean,
        constructor = { it.toBooleanStrict() }
    )
}

public val GenerationOptions.Companion.MESSAGE_DATA_MODIFIER: SingleGenerationOption<Boolean> by lazy {
    GenerationOption.single(
        name = "message_data_modifier",
        description = "Specifies whether data modifier should be generated for messages. True by default.",
        valueKind = OptionTypeKind.Boolean,
        constructor = { it.toBooleanStrict() }
    )
}