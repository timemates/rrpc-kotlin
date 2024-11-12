package org.timemates.rrpc.common.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.timemates.rrpc.common.schema.value.RMDeclarationUrl


@Serializable
public class RSService(
    /**
     * Name of the service.
     */
    @ProtoNumber(1)
    public val name: String,

    /**
     * List of RPCs (Remote Procedure Calls) defined in this service.
     */
    @ProtoNumber(2)
    public val rpcs: List<RSRpc>,

    /**
     * Options on service-level.
     */
    @ProtoNumber(3)
    public val options: RSOptions,

    /**
     * String reference representation.
     */
    @ProtoNumber(4)
    public val typeUrl: RMDeclarationUrl,
) : RSNode
