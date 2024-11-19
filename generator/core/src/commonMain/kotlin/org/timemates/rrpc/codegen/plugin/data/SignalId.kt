package org.timemates.rrpc.codegen.plugin.data

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
public value class SignalId(public val id: String)