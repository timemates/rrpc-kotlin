package org.timemates.rrpc.common.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.timemates.rrpc.common.schema.value.RMDeclarationUrl

@Serializable
public data class RSTypeMemberUrl(
    @ProtoNumber(1)
    public val typeUrl: RMDeclarationUrl,
    @ProtoNumber(2)
    public val memberName: String,
) {
    override fun toString(): String {
        return "$typeUrl#$memberName"
    }
}