package org.timemates.rsproto.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.property

/**
 * Class representing the extension for generating Protobuf code.
 */
public open class RSProtoExtension(private val objects: ObjectFactory) {
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