package org.timemates.rrpc.codegen.plugin.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoOneOf
import org.timemates.rrpc.common.schema.RSFile

@Serializable
public sealed interface GeneratorSignal : GPSignal {
    /**
     * Command to request a list of all possible options available for the generator.
     */
    public data object FetchOptionsList : GeneratorSignal

    /**
     * Sends the user's schema information to the plugin.
     */
    public data class SendInput(
        @ProtoNumber(1)
        public val files: List<RSFile>,
    ) : GeneratorSignal
}

/**
 * Alias to the [GeneratorMessage.Companion.create].
 */
public fun GeneratorMessage(
    block: GeneratorMessage.Builder.() -> Unit,
): GeneratorMessage = GeneratorMessage.create(block)

/**
 * Represents a message sent from the generator to the plugin, containing an identifier and a specific command.
 *
 * This class is designed to encapsulate a generator signal along with a unique signal ID for tracking.
 * It ensures type safety through a sealed structure of commands, making the message handling robust and extensible.
 *
 * @property id The unique identifier for the signal, used to track and correlate requests and responses.
 * @property signal The actual signal being transmitted, encapsulated as a [GeneratorSignal].
 */
@Serializable
public class GeneratorMessage private constructor(
    @ProtoNumber(1)
    public override val id: SignalId,
    @ProtoOneOf
    private val signalOneOf: SignalOneOf,
) : GPMessage<GeneratorSignal> {
    /**
     * Provides access to the signal being transmitted.
     */
    public override val signal: GeneratorSignal get() = signalOneOf.value

    public companion object {
        /**
         * Factory method to create a [GeneratorMessage] using a builder pattern.
         *
         * @param block A lambda used to configure the builder.
         * @return A fully constructed [GeneratorMessage] instance.
         */
        public fun create(block: Builder.() -> Unit): GeneratorMessage =
            Builder().apply(block).build()
    }

    /**
     * Builder class for constructing a [GeneratorMessage].
     *
     * This builder allows for flexible creation of generator messages by setting required properties step by step.
     * It enforces the presence of an ID and a command, ensuring validity at construction time.
     */
    public class Builder {
        /**
         * The unique identifier for the message.
         */
        public var id: SignalId? = null

        private var signalOneOf: SignalOneOf? = null

        /**
         * The signal associated with the message.
         * Assigning a value automatically maps it to the appropriate sealed subclass of [SignalOneOf].
         */
        public var signal: GeneratorSignal?
            get() = signalOneOf?.value
            set(value) {
                signalOneOf = when (value) {
                    is GeneratorSignal.FetchOptionsList ->
                        SignalOneOf.FetchOptionsListField(value)

                    is GeneratorSignal.SendInput ->
                        SignalOneOf.SendInputField(value)

                    null -> null
                }
            }

        /**
         * Constructs the [GeneratorMessage] instance, ensuring that all required fields are set.
         *
         * @throws IllegalStateException if either `id` or `command` is null.
         */
        internal fun build(): GeneratorMessage = GeneratorMessage(
            id ?: error("GeneratorMessage is required to have an id."),
            signalOneOf ?: error("GeneratorMessage is required to have a command."),
        )
    }
}


@Serializable
private sealed interface SignalOneOf {
    val value: GeneratorSignal

    @JvmInline
    value class FetchOptionsListField(
        @ProtoNumber(2)
        override val value: GeneratorSignal.FetchOptionsList,
    ) : SignalOneOf

    @JvmInline
    value class SendInputField(
        @ProtoNumber(3)
        override val value: GeneratorSignal.SendInput,
    ) : SignalOneOf
}
