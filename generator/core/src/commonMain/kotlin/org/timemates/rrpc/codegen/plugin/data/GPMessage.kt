package org.timemates.rrpc.codegen.plugin.data

public sealed interface GPMessage<TSignal : GPSignal> {
    public val id: SignalId
    public val signal: TSignal
}