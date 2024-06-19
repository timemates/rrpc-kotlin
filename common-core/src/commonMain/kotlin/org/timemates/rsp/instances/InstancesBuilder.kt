package org.timemates.rsp.instances

import org.timemates.rsp.annotations.ExperimentalInstancesApi

@ExperimentalInstancesApi
public class InstancesBuilder internal constructor() {
    private val instances: MutableList<ProvidableInstance> = mutableListOf()

    public fun register(instance: ProvidableInstance) {
        instances += instance
    }

    internal fun build(): List<ProvidableInstance> {
        return instances.toList()
    }
}

@ExperimentalInstancesApi
public fun instances(block: InstancesBuilder.() -> Unit): List<ProvidableInstance> {
    return InstancesBuilder().also(block).build()
}