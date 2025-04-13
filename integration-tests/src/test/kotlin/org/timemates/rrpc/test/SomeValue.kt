package org.timemates.rrpc.test

import app.timemate.rrpc.instances.ProvidableInstance

data class SomeValue(
    val value: Int,
) : ProvidableInstance {
    override val key: ProvidableInstance.Key<*> get() = Companion

    companion object : ProvidableInstance.Key<SomeValue>
}