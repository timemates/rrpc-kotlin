package org.timemates.rsp.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty
import org.timemates.rsp.codegen.configuration.MessageBuilderType

/**
 * Class representing the extension for generating Protobuf code.
 */
@RSPGradlePluginDsl
public open class RSProtoExtension(objects: ObjectFactory) {
    /**
     * If you have custom project structure that does not have commonMain / main sourceSets,
     * you should specify your main source set in the [targetSourceSet].
     */
    public val targetSourceSet: Property<String?> = objects.property<String?>()
        .convention(null)

    /**
     * Settings for specifying the type of generation (Client / Server)
     */
    public val profile: Profile = Profile(objects)

    @RSPGradlePluginDsl
    public fun profile(block: Profile.() -> Unit) {
        profile.apply(block)
    }

    /**
     * Extra options for generation.
     */
    public val options: Options = Options(objects)

    @RSPGradlePluginDsl
    public fun options(block: Options.() -> Unit) {
        options.apply(block)
    }

    /**
     * Settings for specifying generation-related paths.
     */
    public val paths: Paths = Paths(objects)

    @RSPGradlePluginDsl
    public fun paths(block: Paths.() -> Unit) {
        paths.apply(block)
    }

    @RSPGradlePluginDsl
    public class Profile(objects: ObjectFactory) {
        /**
         * Represents the flag indicating whether the code generation for the client should be performed.
         */
        public val client: Property<Boolean> = objects.property<Boolean>().convention(true)
        /**
         * Represents the flag indicating whether the code generation for the server should be performed.
         */
        public val server: Property<Boolean> = objects.property<Boolean>().convention(true)
    }

    @RSPGradlePluginDsl
    public class Options(objects: ObjectFactory) {
        /**
         * Represent the flag indicating what builder types should be generated.
         * @see MessageBuilderType
         */
        public val builderTypes: SetProperty<MessageBuilderType> = objects.setProperty<MessageBuilderType>()
            .convention(setOf(MessageBuilderType.DSL))

        public var builderType: MessageBuilderType
            @Deprecated("Shouldn't be used.", level = DeprecationLevel.HIDDEN)
            get() = error("Shouldn't be read.")
            set(value) = builderTypes.set(setOf(value))

        public val permitPackageCycles: Property<Boolean> = objects.property<Boolean>()
            .convention(false)
    }

    @RSPGradlePluginDsl
    public class Paths(objects: ObjectFactory) {
        /**
         * Contains the path to the folder where the Proto definition files are located.
         */
        public val protoSources: Property<String> =
            objects.property<String>().convention("src/main/proto")

        /**
         * Contains the path to the folder where the generated code will be saved.
         */
        public val generationOutput: Property<String> =
            objects.property<String>().convention("generated/rsp/src/commonMain")
    }
}