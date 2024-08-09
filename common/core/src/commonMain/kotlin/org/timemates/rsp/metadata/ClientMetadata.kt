@file:OptIn(ExperimentalSerializationApi::class)

package org.timemates.rsp.metadata

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
public data class ClientMetadata(
    @ProtoNumber(0)
    public val schemaVersion: Int = RSPMetadata.CURRENT_SCHEMA_VERSION,
    @ProtoNumber(1)
    public val serviceName: String = "",
    @ProtoNumber(2)
    public val procedureName: String = "",
    @ProtoNumber(3)
    public override val extra: ExtraMetadata = ExtraMetadata.EMPTY,
) : RSPMetadata {
    override fun extra(extra: ExtraMetadata): ClientMetadata {
        return if (extra == this.extra)
            this
        else copy(extra = extra)
    }
}