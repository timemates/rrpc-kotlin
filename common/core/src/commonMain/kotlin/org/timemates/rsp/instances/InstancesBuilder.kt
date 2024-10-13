package org.timemates.rrpc.instances

import org.timemates.rrpc.annotations.InternalRRpcrotoAPI

public class InstancesBuilder @InternalRRpcrotoAPI constructor() {
    private val instances: MutableList<ProvidableInstance> = mutableListOf()

    public fun register(instance: ProvidableInstance) {
        instances += instance
    }

    @InternalRRpcrotoAPI
    public fun build(): List<ProvidableInstance> {
        return instances.toList()
    }
}

public fun instances(block: InstancesBuilder.() -> Unit): List<ProvidableInstance> {
    @OptIn(InternalRRpcrotoAPI::class)
    return InstancesBuilder().also(block).build()
}