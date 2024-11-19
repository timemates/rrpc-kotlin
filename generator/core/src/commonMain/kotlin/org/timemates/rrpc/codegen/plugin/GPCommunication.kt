package org.timemates.rrpc.codegen.plugin

import kotlinx.serialization.KSerializer
import kotlinx.serialization.protobuf.ProtoBuf
import okio.BufferedSink
import okio.BufferedSource
import okio.EOFException
import org.timemates.rrpc.codegen.plugin.data.*

public typealias PluginCommunication = GPCommunication<GeneratorMessage, PluginMessage>
public typealias GeneratorCommunication = GPCommunication<PluginMessage, GeneratorMessage>

/**
 * Defines an API for plugin-to-generator communication, providing mechanisms to send signals,
 * handle responses, and process incoming signals asynchronously.
 *
 * The implementation is not thread-safe.
 */
public sealed interface GPCommunication<TInput : GPMessage<*>, TOutput : GPMessage<*>> : AutoCloseable {

    /**
     * Sends a signal to the generator and suspends until a response of the expected type is received.
     *
     * @param message The outgoing message to send.
     * @return The response signal matching the expected type.
     * @throws CommunicationException If communication fails or the response type is mismatched.
     */
    public suspend fun send(message: TOutput)

    /**
     * Provides an iterator for processing an incoming message asynchronously.
     *
     * The iterator can be used in a suspending loop to process each signal sequentially.
     */
    public val incoming: GPMessageIterator<TInput>
}

/**
 * High-level handler for processing and responding to incoming generator signals.
 *
 * @param block A handler function that takes a `GeneratorSignal` and returns a list of `PluginSignal` to respond with.
 * @throws CommunicationException If communication errors occur during processing.
 */
@Suppress("UNCHECKED_CAST")
public suspend inline fun <TInput : GPSignal, TOutput : GPSignal, TInputMessage : GPMessage<TInput>, TOutputMessage : GPMessage<TOutput>> GPCommunication<TInputMessage, TOutputMessage>.receive(
    block: (TInput) -> List<TOutput>,
) {
    while (incoming.hasNext()) {
        val message = incoming.next()
        val signal = message.signal

        val isPluginSide = signal is PluginSignal

        val responses = block(signal)

        for (response in responses) {
            send(
                if (isPluginSide) {
                    PluginMessage.create {
                        id = message.id
                        this.signal = response as PluginSignal?
                    }
                } else {
                    GeneratorMessage.create {
                        id = message.id
                        this.signal = response as GeneratorSignal?
                    }
                } as TOutputMessage
            )
        }
    }
}

public fun PluginCommunication(
    input: BufferedSource,
    output: BufferedSink,
): PluginCommunication =
    GPCommunicationImpl(input, output, GeneratorMessage.serializer(), PluginMessage.serializer())

public fun GeneratorCommunication(
    input: BufferedSource,
    output: BufferedSink,
): GeneratorCommunication =
    GPCommunicationImpl(input, output, PluginMessage.serializer(), GeneratorMessage.serializer())

/**
 * Concrete implementation of [GPCommunication], providing mechanisms for
 * sending messages to the generator and processing incoming messages asynchronously.
 *
 * @property input The source for reading incoming generator messages.
 * @property output The sink for writing outgoing plugin messages.
 * @param coroutineContext The coroutine context used for internal operations.
 */
/**
 * Concrete implementation of [GPCommunication], providing mechanisms for
 * sending messages to the generator and processing incoming messages asynchronously.
 *
 * @property input The source for reading incoming generator messages.
 * @property output The sink for writing outgoing plugin messages.
 * @param coroutineContext The coroutine context used for internal operations.
 */
private class GPCommunicationImpl<TInput : GPMessage<*>, TOutput : GPMessage<*>>(
    private val input: BufferedSource,
    private val output: BufferedSink,
    private val inputSerializer: KSerializer<TInput>,
    private val outputSerializer: KSerializer<TOutput>,
) : GPCommunication<TInput, TOutput> {

    override val incoming: GPMessageIterator<TInput> = object : GPMessageIterator<TInput> {
        private var nextMessage: TInput? = null
        private var isClosed = false

        override suspend fun hasNext(): Boolean {
            if (!input.isOpen) return false
            // Keep waiting for the next message until the source is closed.
            if (isClosed) return false

            nextMessage = try {
                readMessage()
            } catch (e: Exception) {
                if (e is EOFException) {
                    isClosed = true
                    return false // End of stream
                }
                null
            }

            return nextMessage != null
        }

        override suspend fun next(): TInput {
            return nextMessage ?: throw NoSuchElementException("No message available")
        }
    }

    override suspend fun send(message: TOutput) {
        val bytes = ProtoBuf.encodeToByteArray(outputSerializer, message)
        output.writeInt(bytes.size)
        output.write(bytes)
        output.flush()
    }

    private fun readMessage(): TInput {
        val size = input.readInt()
        val bytes = input.readByteArray(size.toLong())
        return ProtoBuf.decodeFromByteArray(inputSerializer, bytes)
    }

    private fun BufferedSink.writeInt(value: Int) {
        writeByte((value shr 24) and 0xFF)
        writeByte((value shr 16) and 0xFF)
        writeByte((value shr 8) and 0xFF)
        writeByte(value and 0xFF)
    }

    private fun BufferedSource.readInt(): Int {
        return (readByte().toInt() shl 24) or
            (readByte().toInt() shl 16) or
            (readByte().toInt() shl 8) or
            readByte().toInt()
    }

    override fun close() {
        input.close()
        output.close()
    }
}