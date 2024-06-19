package org.timemates.rsp.metadata

import org.timemates.rsp.annotations.ExperimentalInstancesApi
import org.timemates.rsp.instances.InstanceContainer
import org.timemates.rsp.instances.ProvidableInstance
import org.timemates.rsp.instances.getInstance

/**
 * Represents extra metadata within incoming request associated with a coroutine context.
 *
 * @property extra The map containing the extra metadata.
 */
@OptIn(ExperimentalInstancesApi::class)
@JvmInline
public value class ExtraMetadata(public val extra: Map<String, ByteArray>) : ProvidableInstance {
    public companion object : ProvidableInstance.Key<ExtraMetadata> {
        public val EMPTY: ExtraMetadata = ExtraMetadata(emptyMap())
    }

    override val key: ProvidableInstance.Key<*>
        get() = Companion
}

@ExperimentalInstancesApi
public val InstanceContainer.extraMetadata: ExtraMetadata
    get() = getInstance(ExtraMetadata) ?: error("Called from the wrong context")