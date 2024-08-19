package org.timemates.rsp.server.module.descriptors

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import org.timemates.rsp.options.Options
import org.timemates.rsp.server.OptionsContainer
import org.timemates.rsp.server.RequestContext
import org.timemates.rsp.server.optionsContainer

/**
 * A sealed interface representing a procedure descriptor within a service.
 * Contains common properties for different types of procedures.
 */
public sealed interface ProcedureDescriptor<TInput : Any, TOutput : Any> : OptionsContainer {
    /**
     * The name of the procedure.
     */
    public val name: String

    /**
     * The deserialization strategy for the input of the procedure.
     */
    public val inputSerializer: DeserializationStrategy<TInput>

    /**
     * The serialization strategy for the output of the procedure.
     */
    public val outputSerializer: SerializationStrategy<TOutput>

    /**
     * A data class representing a request-response type procedure descriptor.
     *
     * @param name The name of the procedure.
     * @param inputSerializer The deserialization strategy for the input of the procedure.
     * @param outputSerializer The serialization strategy for the output of the procedure.
     * @param procedure The suspend function representing the procedure logic.
     * @param options The list of options associated with the procedure.
     */
    public class RequestResponse<TInput : Any, TOutput : Any>(
        override val name: String,
        override val inputSerializer: DeserializationStrategy<TInput>,
        override val outputSerializer: SerializationStrategy<TOutput>,
        private val procedure: suspend (RequestContext, TInput) -> TOutput,
        options: Options,
    ) : ProcedureDescriptor<TInput, TOutput>, OptionsContainer by optionsContainer(options) {
        /**
         * Executes the request-response procedure.
         * @return The response payload.
         */
        public suspend fun execute(
            context: RequestContext,
            input: TInput,
        ): TOutput {
            return procedure(context, input)
        }
    }

    /**
     * A data class representing a request-stream type procedure descriptor.
     *
     * @param name The name of the procedure.
     * @param inputSerializer The deserialization strategy for the input of the procedure.
     * @param outputSerializer The serialization strategy for the output of the procedure.
     * @param procedure The suspend function representing the procedure logic.
     * @param options The list of options associated with the procedure.
     */
    public class RequestStream<TInput : Any, TOutput : Any>(
        override val name: String,
        override val inputSerializer: DeserializationStrategy<TInput>,
        override val outputSerializer: SerializationStrategy<TOutput>,
        private val procedure: suspend (RequestContext, TInput) -> Flow<TOutput>,
        options: Options,
    ) : ProcedureDescriptor<TInput, TOutput>, OptionsContainer by optionsContainer(options) {
        /**
         * Executes the request-stream procedure.
         * @return A flow of response payloads.
         */
        public suspend fun execute(
            context: RequestContext,
            value: TInput,
        ): Flow<TOutput> {
            return procedure(context, value)
        }
    }

    /**
     * A data class representing a request-channel type procedure descriptor.
     *
     * @param name The name of the procedure.
     * @param inputSerializer The deserialization strategy for the input of the procedure.
     * @param outputSerializer The serialization strategy for the output of the procedure.
     * @param procedure The suspend function representing the procedure logic.
     * @param options The list of options associated with the procedure.
     */
    public class RequestChannel<TInput : Any, TOutput : Any>(
        override val name: String,
        override val inputSerializer: DeserializationStrategy<TInput>,
        override val outputSerializer: SerializationStrategy<TOutput>,
        private val procedure: suspend (RequestContext, Flow<TInput>) -> Flow<TOutput>,
        options: Options,
    ) : ProcedureDescriptor<TInput, TOutput>, OptionsContainer by optionsContainer(options) {
        /**
         * Executes the request-channel procedure.
         *
         * @param protoBuf The ProtoBuf instance to use for serialization and deserialization.
         * @param init The initial packet containing the serialized input data.
         * @param incoming The flow of incoming request packets.
         * @return A flow of response payloads.
         */
        public suspend fun execute(
            context: RequestContext,
            flow: Flow<TInput>,
        ): Flow<TOutput> {
            return procedure(context, flow)
        }
    }
}