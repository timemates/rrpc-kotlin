package org.timemates.rrpc.common.metadata.value

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
public value class RMPackageName(
    public val value: String,
)