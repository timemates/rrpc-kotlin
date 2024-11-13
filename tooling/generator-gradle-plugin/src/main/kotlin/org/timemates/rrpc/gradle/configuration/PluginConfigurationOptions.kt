package org.timemates.rrpc.gradle.configuration

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.mapProperty
import org.timemates.rrpc.gradle.RRpcGradlePluginDsl

@RRpcGradlePluginDsl
public class PluginConfigurationOptions(objects: ObjectFactory) {
    internal val map: MapProperty<String, Any?> = objects.mapProperty<String, Any?>()

    public operator fun set(key: String, value: Any) {
        map.put(key, value)
    }

    public operator fun get(key: String): Provider<Any?> {
        return map.getting(key)
    }

    public object Kotlin {
        public const val OUTPUT: String = "kotlin_output"
        public const val SERVER_GENERATION: String = "kotlin_server_generation"
        public const val CLIENT_GENERATION: String = "kotlin_client_generation"
        public const val TYPE_GENERATION: String = "kotlin_server_generation"
        public const val METADATA_GENERATION: String = "kotlin_metadata_generation"
        public const val METADATA_SCOPE_NAME: String = "kotlin_metadata_scope_name"
    }
}