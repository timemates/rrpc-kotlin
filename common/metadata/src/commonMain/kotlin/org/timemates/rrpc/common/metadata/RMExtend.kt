package org.timemates.rrpc.common.metadata

import kotlinx.serialization.Serializable
import org.timemates.rrpc.common.metadata.value.RMTypeUrl

@Serializable
public class RMExtend(
    public val typeUrl: RMTypeUrl,
    public val name: String,
    public val fields: List<RMField>,
    override val documentation: String?,
) : Documentable