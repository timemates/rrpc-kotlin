package org.timemates.rsp.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.property

/**
 * Class representing the extension for generating Protobuf code.
 */
public open class RSProtoExtension(objects: ObjectFactory) {
    /**
     * If you have custom project structure that does not have commonMain / main sourceSets,
     * you should specify your main source set in the [targetSourceSet].
     */
    public val targetSourceSet: Property<String?> = objects.property<String?>()
        .convention(null)

    /**
     * Contains the path to the folder where the Proto definition files are located.
     */
    public val protoSourcePath: Property<String> =
        objects.property<String>().convention("src/main/proto")

    /**
     * Contains the path to the folder where the generated code will be saved.
     */
    public val generationOutputPath: Property<String> =
        objects.property<String>().convention("generated/proto-generator/src/commonMain")

    /**
     * Represents the flag indicating whether the code generation for the client should be performed.
     */
    public val clientGeneration: Property<Boolean> = objects.property<Boolean>().convention(true)
    /**
     * Represents the flag indicating whether the code generation for the server should be performed.
     */
    public val serverGeneration: Property<Boolean> = objects.property<Boolean>().convention(true)
}