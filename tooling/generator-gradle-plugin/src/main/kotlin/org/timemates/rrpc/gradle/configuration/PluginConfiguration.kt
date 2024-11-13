package org.timemates.rrpc.gradle.configuration

import org.gradle.api.model.ObjectFactory
import org.timemates.rrpc.gradle.RRpcGradlePluginDsl

@RRpcGradlePluginDsl
public class PluginConfiguration(objects: ObjectFactory) {
    public val options: PluginConfigurationOptions = PluginConfigurationOptions(objects)

    public fun options(block: PluginConfigurationOptions.() -> Unit) {
        options.apply(block)
    }
}