package org.timemates.rrpc.common.metadata

import kotlinx.serialization.Serializable
import org.timemates.rrpc.common.metadata.value.RMTypeUrl

@Serializable
public class RMTypeMemberUrl(
    public val typeUrl: RMTypeUrl,
    public val memberName: String,
) {
    override fun toString(): String {
        return "$typeUrl#$memberName"
    }
}