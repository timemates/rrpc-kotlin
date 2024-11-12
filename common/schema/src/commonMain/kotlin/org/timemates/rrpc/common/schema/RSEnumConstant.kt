package org.timemates.rrpc.common.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
public data class RSEnumConstant(
    @ProtoNumber(1)
    val name: String,
    @ProtoNumber(2)
    val tag: Int,
    @ProtoNumber(3)
    public val options: RSOptions,
    @ProtoNumber(4)
    override val documentation: String?,
) : RSNode, Documentable