package org.timemates.rsp.server.module.descriptors

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import org.timemates.rsp.server.OptionsContainer
import org.timemates.rsp.options.Options
import org.timemates.rsp.server.RequestContext
import org.timemates.rsp.server.optionsContainer
import kotlin.reflect.KClass

/**
 * Represents a service descriptor containing the service name and a list of procedure descriptors.
 *
 * @property name The name of the service.
 * @property procedures The list of procedure descriptors associated with the service.
 */
@Suppress("UNCHECKED_CAST")
public class ServiceDescriptor(
    public val name: String,
    public val procedures: List<ProcedureDescriptor<*, *>>,
    options: Options,
) : OptionsContainer by optionsContainer(options) {
    /**
     * A map that associates procedure names with their corresponding descriptors.
     */
    private val proceduresMap = procedures.associateBy {
        it.name to it::class.simpleName!!
    }

    /**
     * Retrieves a specific ProcedureDescriptor from the ServiceDescriptor based on the name and type.
     *
     * @param name The name of the procedure.
     * @param type The class representing the type of the ProcedureDescriptor.
     * @return The ProcedureDescriptor object matching the given name and type, or null if not found.
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T : ProcedureDescriptor<*, *>> procedure(name: String, type: KClass<T>): T? {
        return proceduresMap[name to type.simpleName!!] as? T
    }

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
}

/**
 * Retrieves a specific ProcedureDescriptor from the ServiceDescriptor based on the name.

 * @param name The name of the procedure.
 * @return The ProcedureDescriptor object matching the given name, or null if not found.
 */
public inline fun <reified T : ServiceDescriptor.ProcedureDescriptor<*, *>> ServiceDescriptor.procedure(name: String): T? {
    return procedure(name, T::class)
}