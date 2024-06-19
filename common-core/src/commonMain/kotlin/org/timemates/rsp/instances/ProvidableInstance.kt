package org.timemates.rsp.instances

import org.timemates.rsp.annotations.ExperimentalInstancesApi

/**
 * A contract for classes that can provide instances based on a specified key.
 */
@ExperimentalInstancesApi
public interface ProvidableInstance {
    public val key: Key<*>

    @ExperimentalInstancesApi
    public interface Key<TInstance : ProvidableInstance>
}