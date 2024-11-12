package org.timemates.rrpc.common.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.protobuf.ProtoNumber

@SerialName("ONE_OF")
public data class RSOneOf(
    @ProtoNumber(1)
    public val name: String,
    @ProtoNumber(2)
    public val fields: List<RSField>,
    @ProtoNumber(3)
    val documentation: String?,
    @ProtoNumber(4)
    val options: RSOptions,
)