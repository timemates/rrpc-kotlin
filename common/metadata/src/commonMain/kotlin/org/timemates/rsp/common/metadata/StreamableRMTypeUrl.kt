package org.timemates.rrpc.common.metadata

import kotlinx.serialization.Serializable
import org.timemates.rrpc.common.metadata.value.RMTypeUrl

/**
 * Denotes that type might be of streaming type.
 */
@Serializable
public class StreamableRMTypeUrl(public val isStreaming: Boolean, public val type: RMTypeUrl)