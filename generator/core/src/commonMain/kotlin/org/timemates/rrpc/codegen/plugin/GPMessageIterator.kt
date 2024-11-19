package org.timemates.rrpc.codegen.plugin

import org.timemates.rrpc.codegen.plugin.data.GPMessage

/**
 * An asynchronous iterator for signals, allowing suspending iteration.
 */
public interface GPMessageIterator<T : GPMessage<*>> {
    /**
     * Checks if there are more signals available.
     */
    public suspend operator fun hasNext(): Boolean

    /**
     * Retrieves the next signal. Should only be called after [hasNext] returns `true`.
     */
    public suspend operator fun next(): T
}