package org.timemates.rsp.instances

import org.timemates.rsp.annotations.InternalRSProtoAPI

public class InstancesBuilder @InternalRSProtoAPI constructor() {
    private val instances: MutableList<ProvidableInstance> = mutableListOf()

    public fun register(instance: ProvidableInstance) {
        instances += instance
    }

    @InternalRSProtoAPI
    public fun build(): List<ProvidableInstance> {
        return instances.toList()
    }
}

public fun instances(block: InstancesBuilder.() -> Unit): List<ProvidableInstance> {
    @OptIn(InternalRSProtoAPI::class)
    return InstancesBuilder().also(block).build()
}