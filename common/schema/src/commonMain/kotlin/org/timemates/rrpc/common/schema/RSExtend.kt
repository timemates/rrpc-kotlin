package org.timemates.rrpc.common.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.timemates.rrpc.common.schema.value.RMDeclarationUrl

@Serializable
public data class RSExtend(
    @ProtoNumber(1)
    public val typeUrl: RMDeclarationUrl,
    @ProtoNumber(2)
    public val name: String,
    @ProtoNumber(3)
    public val fields: List<RSField>,
    @ProtoNumber(4)
    override val documentation: String?,
) : Documentable, RSNode