package com.y9vad9.rsocket.proto.providable

import com.y9vad9.rsocket.proto.RSocketProtoServerBuilder
import com.y9vad9.rsocket.proto.annotations.ExperimentalInstancesApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf

@ExperimentalInstancesApi
@OptIn(ExperimentalSerializationApi::class)
@JvmInline
public value class ProtobufInstance(public val protoBuf: ProtoBuf) : ProvidableInstance {

    public companion object Key : ProvidableInstance.Key<ProtobufInstance>

    override val key: ProvidableInstance.Key<ProtobufInstance>
        get() = Key
}

@OptIn(ExperimentalSerializationApi::class)
@ExperimentalInstancesApi
public fun RSocketProtoServerBuilder.InstancesBuilder.protobuf(
    protobuf: ProtoBuf
) {
    register(ProtobufInstance(protobuf))
}