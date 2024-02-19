package org.timemates.rsproto.server.instances

import org.timemates.rsproto.server.RSocketProtoServerBuilder
import org.timemates.rsproto.server.annotations.ExperimentalInstancesApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoBufBuilder

@ExperimentalInstancesApi
@OptIn(ExperimentalSerializationApi::class)
@JvmInline
public value class ProtobufInstance(public val protoBuf: ProtoBuf) : ProvidableInstance {

    public companion object Key : ProvidableInstance.Key<ProtobufInstance>

    override val key: ProvidableInstance.Key<ProtobufInstance>
        get() = Key
}

/**
 * Register the protobuf instance with the RSocketProtoServerBuilder.
 *
 * @param protobuf The ProtoBuf instance to register.
 */
@OptIn(ExperimentalSerializationApi::class)
@ExperimentalInstancesApi
public fun RSocketProtoServerBuilder.InstancesBuilder.protobuf(
    protobuf: ProtoBuf
) {
    register(ProtobufInstance(protobuf))
}

/**
 * Registers a Protobuf instance with the RSocketProtoServerBuilder.
 * This allows the server to handle remote method calls using Protobuf encoding.
 *
 * @param builder A lambda function that configures the Protobuf instance using the [ProtoBufBuilder].
 */
@OptIn(ExperimentalSerializationApi::class)
@ExperimentalInstancesApi
public fun RSocketProtoServerBuilder.InstancesBuilder.protobuf(
    builder: ProtoBufBuilder.() -> Unit
) {
    register(ProtobufInstance(ProtoBuf(builderAction = builder)))
}