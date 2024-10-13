package org.timemates.rrpc.common.metadata

import kotlinx.serialization.Serializable
import org.timemates.rrpc.common.metadata.value.RMTypeUrl

@Serializable
public class RMField(
    public val tag: Int,
    public val name: String,
    public val options: RMOptions,
    override val documentation: String?,
    public val typeUrl: RMTypeUrl,
    public val isRepeated: Boolean,
    public val isInOneOf: Boolean = false,
    public val isExtension: Boolean = false,
) : Documentable, RMNode

