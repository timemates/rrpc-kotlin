@file:Suppress("MemberVisibilityCanBePrivate")
@file:OptIn(ExperimentalSerializationApi::class, ExperimentalSerializationApi::class)

package org.timemates.rsproto.server.descriptors

import io.ktor.utils.io.core.*
import io.rsocket.kotlin.payload.Payload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.protobuf.ProtoBuf

public sealed interface ProcedureDescriptor {
    public val name: String
    public val inputSerializer: DeserializationStrategy<Any>
    public val outputSerializer: SerializationStrategy<Any>

    public data class RequestResponse(
        override val name: String,
        override val inputSerializer: KSerializer<Any>,
        override val outputSerializer: KSerializer<Any>,
        private val procedure: suspend (Any) -> Any,
    ) : ProcedureDescriptor {
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

    public data class RequestStream(
        override val name: String,
        override val inputSerializer: DeserializationStrategy<Any>,
        override val outputSerializer: SerializationStrategy<Any>,
        private val procedure: suspend (Any) -> Flow<Any>,
    ) : ProcedureDescriptor {
        public suspend fun execute(
            protoBuf: ProtoBuf = ProtoBuf,
            request: ByteReadPacket,
        ): Flow<Payload> {
            return procedure(protoBuf.decodeFromByteArray(inputSerializer, request.readBytes()))
                .map { Payload(ByteReadPacket(protoBuf.encodeToByteArray(outputSerializer, it))) }
        }
    }

    public data class RequestChannel(
        override val name: String,
        override val inputSerializer: DeserializationStrategy<Any>,
        override val outputSerializer: SerializationStrategy<Any>,
        private val procedure: suspend (Any, Flow<Any>) -> Flow<Any>,
    ) : ProcedureDescriptor {
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