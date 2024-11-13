package org.timemates.rrpc.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import org.timemates.rrpc.gradle.configuration.PluginConfiguration

/**
 * Class representing the extension for configuring and generating Protobuf code in the RRPC Gradle plugin.
 * This extension allows specifying various properties related to the target source set, input directories,
 * and additional options for code generation, making it suitable for projects with custom directory structures
 * and specific Protobuf generation requirements.
 */
@RRpcGradlePluginDsl
public open class RRpcExtension(objects: ObjectFactory) {

    /**
     * Specifies the target source set for code generation.
     * Useful for projects that do not follow the default `commonMain` / `main` source set structure.
     *
     * @property targetSourceSet Name of the custom source set to use as the target, or `null` if the default should be used.
     */
    public val targetSourceSet: Property<String?> = objects.property<String?>()
        .convention(null)

    /**
     * Specifies a list of folders containing `.proto` files to be included in the generation process.
     * These directories should include all necessary Protobuf definitions required for code generation.
     *
     * @property protosInput List of directory paths for `.proto` files.
     */
    public val protosInput: ListProperty<String> = objects.listProperty(String::class.java)
        .convention(mutableListOf())

    /**
     * Additional options for controlling the Protobuf code generation process, including custom generation rules.
     *
     * @property protoOptions Instance of [ProtoOptions] for specifying extra configuration settings.
     */
    public val protoOptions: ProtoOptions = ProtoOptions(objects)

    /**
     * Configures additional settings for the [protoOptions], allowing further customization of the Protobuf generation options.
     *
     * @param block A lambda for applying custom configurations to [protoOptions].
     */
    @RRpcGradlePluginDsl
    public fun protoOptions(block: ProtoOptions.() -> Unit) {
        protoOptions.apply(block)
    }

    /**
     * Holds a set of configurations for each supported language or specific generation setup.
     * New configurations can be added dynamically by calling `kotlin { ... }` or defining other language configurations.
     *
     * @property pluginConfigurations Container of [PluginConfiguration] objects for each language or target type.
     */
    public val pluginConfigurations: NamedDomainObjectContainer<PluginConfiguration> =
        objects.domainObjectContainer(PluginConfiguration::class.java)

    /**
     * A nested class representing options that apply to Protobuf generation.
     * Allows setting specific preferences, such as enabling or disabling package cycle checks.
     */
    @RRpcGradlePluginDsl
    public class ProtoOptions(objects: ObjectFactory) {
        /**
         * Determines whether package cycle checks are permitted during generation.
         * If `false`, package cycles will raise an error, while `true` will allow them.
         *
         * @property permitPackageCycles A boolean property controlling package cycle permissions.
         */
        public val permitPackageCycles: Property<Boolean> = objects.property<Boolean>()
            .convention(false)
    }

    /**
     * Configures a `kotlin` target for generation, allowing language-specific options to be set.
     * This function applies the given configuration block to a Kotlin-specific [PluginConfiguration] instance.
     *
     * @param block A lambda for applying configurations specific to Kotlin code generation.
     * @see org.timemates.rrpc.gradle.configuration.PluginConfigurationOptions.Kotlin
     */
    public fun kotlin(block: PluginConfiguration.() -> Unit) {
        pluginConfigurations.maybeCreate("kotlin").apply(block)
    }
}
