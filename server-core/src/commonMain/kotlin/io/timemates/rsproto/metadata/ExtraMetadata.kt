package io.timemates.rsproto.metadata

import kotlin.coroutines.CoroutineContext

/**
 * Represents extra metadata within incoming request associated with a coroutine context.
 *
 * @property extra The map containing the extra metadata.
 */
@JvmInline
public value class ExtraMetadata(public val extra: Map<String, ByteArray>) : CoroutineContext.Element {
    public companion object Key : CoroutineContext.Key<ExtraMetadata>

    override val key: CoroutineContext.Key<*>
        get() = Key
}