package com.y9vad9.rsocket.proto.providable

import com.y9vad9.rsocket.proto.annotations.ExperimentalInstancesApi

/**
 * A contract for classes that can provide instances based on a specified key.
 */
@ExperimentalInstancesApi
public interface ProvidableInstance {
    public val key: Key<*>

    @ExperimentalInstancesApi
    public interface Key<TInstance : ProvidableInstance>
}