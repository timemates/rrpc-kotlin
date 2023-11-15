package com.y9vad9.rsproto.server.instances

import com.y9vad9.rsproto.server.annotations.ExperimentalInstancesApi

/**
 * A contract for classes that can provide instances based on a specified key.
 */
@ExperimentalInstancesApi
public interface ProvidableInstance {
    public val key: Key<*>

    @ExperimentalInstancesApi
    public interface Key<TInstance : ProvidableInstance>
}