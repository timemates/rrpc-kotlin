package io.timemates.rsproto.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.property

/**
 * Class representing the extension for generating Protobuf code.
 */
public open class RSProtoExtension(private val objects: ObjectFactory) {
    /**
     * Contains the path to the folder where the Proto definition files are located.
     */
    public var protoSourcePath: Provider<String> =
        objects.property<String>().convention("src/main/proto")

    /**
     * Contains the path to the folder where the generated code will be saved.
     */
    public var generationOutputPath: Provider<String> =
        objects.property<String>().convention("build/generated/proto-generator/src/commonMain")

    /**
     * Represents the flag indicating whether the code generation for the client should be performed.
     */
    public var clientGeneration: Boolean = true
    /**
     * Represents the flag indicating whether the code generation for the server should be performed.
     */
    public var serverGeneration: Boolean = true
}