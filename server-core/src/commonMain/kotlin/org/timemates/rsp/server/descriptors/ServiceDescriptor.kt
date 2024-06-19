package org.timemates.rsp.server.descriptors

import io.ktor.utils.io.core.*
import io.rsocket.kotlin.payload.Payload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.protobuf.ProtoBuf
import org.timemates.rsp.metadata.ClientMetadata
import org.timemates.rsp.options.Option
import org.timemates.rsp.pipeline.PipelineContext

/**
 * Represents a service descriptor containing the service name and a list of procedure descriptors.
 *
 * @property name The name of the service.
 * @property procedures The list of procedure descriptors associated with the service.
 */
@OptIn(ExperimentalSerializationApi::class)
public data class ServiceDescriptor(
    val name: String,
    val procedures: List<ProcedureDescriptor<*, *>>
) {
    /**
     * A sealed interface representing a procedure descriptor within a service.
     * Contains common properties for different types of procedures.
     */
    public sealed interface ProcedureDescriptor<TInput : Any, TOutput : Any> {
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
         * The list of options associated with the procedure.
         */
        public val options: List<Option<*>>

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
            private val procedure: suspend (TInput) -> TOutput,
            override val options: List<Option<*>>,
        ) : ProcedureDescriptor<TInput, TOutput> {
            /**
             * Executes the request-response procedure.
             *
             * @param protoBuf The ProtoBuf instance to use for serialization and deserialization.
             * @param request The request packet containing the serialized input data.
             * @return The response payload.
             */
            public suspend fun execute(
                protoBuf: ProtoBuf = ProtoBuf,
                request: ByteReadPacket,
            ): Payload {
                return protoBuf.encodeToByteArray(
                    serializer = outputSerializer,
                    value = procedure(protoBuf.decodeFromByteArray(inputSerializer, request.readBytes())),
                ).let { Payload(ByteReadPacket(it)) }
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
            private val procedure: suspend (Any) -> Flow<Any>,
            override val options: List<Option<*>>,
        ) : ProcedureDescriptor<TInput, TOutput> {
            /**
             * Executes the request-stream procedure.
             *
             * @param protoBuf The ProtoBuf instance to use for serialization and deserialization.
             * @param request The request packet containing the serialized input data.
             * @return A flow of response payloads.
             */
            public suspend fun execute(
                protoBuf: ProtoBuf = ProtoBuf,
                request: ByteReadPacket,
            ): Flow<Payload> {
                return procedure(protoBuf.decodeFromByteArray(inputSerializer, request.readBytes()))
                    .map { Payload(ByteReadPacket(protoBuf.encodeToByteArray(outputSerializer, it))) }
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
            private val procedure: suspend (Any, Flow<Any>) -> Flow<Any>,
            override val options: List<Option<*>>,
        ) : ProcedureDescriptor<TInput, TOutput> {
            /**
             * Executes the request-channel procedure.
             *
             * @param protoBuf The ProtoBuf instance to use for serialization and deserialization.
             * @param init The initial packet containing the serialized input data.
             * @param incoming The flow of incoming request packets.
             * @return A flow of response payloads.
             */
            public suspend fun execute(
                protoBuf: ProtoBuf = ProtoBuf,
                init: ByteReadPacket,
                incoming: Flow<ByteReadPacket>,
            ): Flow<Payload> {
                return procedure(
                    protoBuf.decodeFromByteArray(inputSerializer, init.readBytes()),
                    incoming.map { protoBuf.decodeFromByteArray(inputSerializer, it.readBytes()) }
                ).map { Payload(ByteReadPacket(protoBuf.encodeToByteArray(outputSerializer, it))) }
            }
        }
    }
}
