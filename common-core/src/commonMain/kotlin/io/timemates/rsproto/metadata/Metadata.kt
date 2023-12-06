@file:OptIn(ExperimentalSerializationApi::class)

package io.timemates.rsproto.metadata

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * Represents metadata information.
 *
 * @property serviceName The name of the service.
 * @property procedureName The name of the procedure.
 * @property extra Additional key-value pairs of metadata.
 */
@Serializable
public data class Metadata(
    @ProtoNumber(0)
    val serviceName: String = "",
    @ProtoNumber(1)
    val procedureName: String = "",
    @ProtoNumber(2)
    val extra: Map<String, ByteArray> = emptyMap(),
)