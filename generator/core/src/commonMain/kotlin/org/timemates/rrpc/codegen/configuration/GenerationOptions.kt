package org.timemates.rrpc.codegen.configuration

import okio.Path
import okio.Path.Companion.toPath

/**
 * A set of options for code generation, represented as a map of option names to values.
 *
 * `GenerationOptions` provides a structured way to retrieve configuration values for code generation,
 * where options are defined as instances of `GenerationOption`. Each option, such as
 * [PERMIT_PACKAGE_CYCLES], is associated with a key and an expected type.
 *
 * @property map A map containing option names as keys and corresponding values.
 * @constructor Creates a new instance with the specified options map.
 *
 * @see GenerationOption
 */
@JvmInline
public value class GenerationOptions private constructor(private val map: Map<String, Any>) {
    /**
     * A collection of predefined options for code generation.
     */
    public companion object {
        public fun create(block: Builder.() -> Unit): GenerationOptions {
            return Builder().apply(block).build()
        }

        public val PROTOS_INPUT: RepeatableGenerationOption<Path> = GenerationOption.repeatable(
            name = "protos_input",
            description = "Folder with .proto files to be used for generation (repeatable).",
            valueKind = OptionTypeKind.Text,
            constructor = { it.toPath() },
        )

        public val PERMIT_PACKAGE_CYCLES: SingleGenerationOption<Boolean> = GenerationOption.single(
            name = "permit_package_cycles",
            description = "Indicates whether package cycles should be ignored while parsing .proto files.",
            valueKind = OptionTypeKind.Boolean,
            constructor = { it.toBooleanStrictOrNull() ?: false },
        )
    }

    /**
     * Retrieves the value for a given [option], casting it to the expected type.
     *
     * @param option The [SingleGenerationOption] key whose value is requested.
     * @return The value of the option, or `null` if not found.
     */
    @Suppress("UNCHECKED_CAST")
    public operator fun <T> get(option: SingleGenerationOption<T>): T? = map[option.name] as? T

    /**
     * Retrieves the values for a given [option], casting it to the expected type.
     *
     * @param option The [RepeatableGenerationOption] key whose value is requested.
     * @return The value of the option, or `null` if not found.
     */
    @Suppress("UNCHECKED_CAST")
    public operator fun <T> get(option: RepeatableGenerationOption<T>): List<T>? = map[option.name] as? List<T>

    public class Builder {
        private val map: MutableMap<String, Any> = mutableMapOf()

        public operator fun <T : Any> set(option: SingleGenerationOption<T>, value: String) {
            @Suppress("NAME_SHADOWING")
            val value = option.valueFactory(value)

            map[option.name] = value
        }

        public fun <T : Any> append(option: RepeatableGenerationOption<T>, value: String) {
            @Suppress("NAME_SHADOWING")
            val value = option.valueFactory(value)

            if (map.containsKey(option.name)) {
                @Suppress("UNCHECKED_CAST")
                map[option.name] = (map[option.name] as? List<T>)?.plus(value) ?: listOf(value)
            } else {
                map[option.name] = listOf(value)
            }
        }

        public fun build(): GenerationOptions {
            return GenerationOptions(map.toMap())
        }
    }
}

public val GenerationOptions.protoInputs: List<Path> get() =
    this[GenerationOptions.PROTOS_INPUT].orEmpty()
public val GenerationOptions.isPackageCyclesPermitted: Boolean get() = this[GenerationOptions.PERMIT_PACKAGE_CYCLES] ?: false

/**
 * Represents an option for code generation, identified by its unique name.
 *
 * @param T The type of the value associated with this option.
 * @property name The unique identifier for the option.
 * @property description Descriptive information about option, value constraints, and so on.
 */
public sealed interface GenerationOption {
    public val name: String
    public val description: String?
    public val valueKind: OptionTypeKind

    public companion object {
        public fun <T> single(
            name: String,
            description: String,
            valueKind: OptionTypeKind,
            constructor: (String) -> T,
        ): SingleGenerationOption<T> {
            return SingleGenerationOption(name, description, valueKind, constructor)
        }

        public fun <T> repeatable(
            name: String,
            description: String,
            valueKind: OptionTypeKind,
            constructor: (String) -> T,
        ): RepeatableGenerationOption<T> {
            return RepeatableGenerationOption(name, description, valueKind, constructor)
        }
    }
}

public sealed interface OptionTypeKind {
    public data object Text : OptionTypeKind
    public data object Boolean : OptionTypeKind
    public sealed interface Number : OptionTypeKind {
        public data object Int : Number
        public data object Long : Number
        public data object Float : Number
        public data object Double : Number
    }
    public data object Path : OptionTypeKind
    public data class Choice(public val variants: List<String>) : OptionTypeKind
}

@Suppress("unused")
public data class SingleGenerationOption<T>(
    public override val name: String,
    public override val description: String? = null,
    override val valueKind: OptionTypeKind,
    public val valueFactory: (String) -> T,
) : GenerationOption

@Suppress("unused")
public data class RepeatableGenerationOption<T>(
    public override val name: String,
    public override val description: String? = null,
    override val valueKind: OptionTypeKind,
    public val valueFactory: (String) -> T,
) : GenerationOption