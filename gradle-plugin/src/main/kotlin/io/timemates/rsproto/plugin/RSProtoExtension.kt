package io.timemates.rsproto.plugin

/**
 * Class representing the extension for generating Protobuf code.
 */
public open class RSProtoExtension {
    /**
     * Contains the path to the folder where the Proto definition files are located.
     */
    public var protoSourcePath: String = "src/main/proto"

    /**
     * Contains the path to the folder where the generated code will be saved.
     */
    public var generationOutputPath: String = "build/generated/proto-generator/src/commonMain"

    /**
     * Represents the flag indicating whether the code generation for the client should be performed.
     */
    public var clientGeneration: Boolean = true
    /**
     * Represents the flag indicating whether the code generation for the server should be performed.
     */
    public var serverGeneration: Boolean = true
}