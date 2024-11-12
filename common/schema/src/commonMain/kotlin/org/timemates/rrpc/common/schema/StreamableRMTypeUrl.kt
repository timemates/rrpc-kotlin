package org.timemates.rrpc.common.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.timemates.rrpc.common.schema.value.RMDeclarationUrl

/**
 * Denotes that type might be of streaming type.
 */
@Serializable
public class StreamableRMTypeUrl(
    @ProtoNumber(1)
    public val isStreaming: Boolean,
    @ProtoNumber(2)
    public val type: RMDeclarationUrl,
)