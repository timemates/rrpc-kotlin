package app.timemate.rrpc.metadata.common

import app.timemate.rrpc.RSProtoType
import app.timemate.rrpc.instances.InstanceContainer
import app.timemate.rrpc.instances.InstancesBuilder
import app.timemate.rrpc.instances.ProvidableInstance
import app.timemate.rrpc.proto.schema.*

/**
 * Creates an in-memory implementation of [SchemaMetadataModule] backed by a static list of [RSFile] instances.
 *
 * @param files The source files used to build metadata resolution.
 * @return A [SchemaMetadataModule] implementation that operates purely in memory.
 */
public fun SchemaMetadataModule(
    files: List<RSFile>
): SchemaMetadataModule = InMemorySchemaMetadataModule(files.asReversed().distinctBy { it.location })

public fun SchemaMetadataModule(vararg files: RSFile): SchemaMetadataModule = SchemaMetadataModule(files.toList())

/**
 * Represents a metadata module capable of resolving proto-like schema structures, including types, enums, services, and files.
 *
 * This interface is intended to abstract over various metadata backends (e.g. in-memory, file-based, remote).
 * It can be provided as a dependency through [ProvidableInstance] and supports composition via the [+] operator.
 */
public interface SchemaMetadataModule : ProvidableInstance {

    /**
     * Resolves all schema files available within this metadata module.
     */
    public fun resolveAllFiles(): List<RSFile>

    /**
     * Resolves all types defined in this module, including messages, extensions, and nested declarations.
     */
    public fun resolveAllTypes(): List<RSType>

    /**
     * Resolves all services declared in this module.
     */
    public fun resolveAllServices(): List<RSService>

    public fun resolveAllExtends(): List<RSExtend>

    /**
     * Resolves a declaration by its fully qualified [url], returning an [RSNode] which may be a message,
     * enum, or extension.
     *
     * @param url The full type URL (e.g., `type.googleapis.com/example.MyType`)
     * @return A matching node if present, or `null` if not found.
     *
     * @warning If multiple nodes share the same URL across different files or modules,
     * the **last one in sequence takes precedence**. This can occur when merging modules.
     */
    public fun resolveDeclaration(url: String): RSNode?

    /**
     * Resolves a specific file based on its location, if present in the module.
     *
     * @param location The location of the element to resolve.
     * @return A matching [RSFile], or `null` if not found.
     */
    public fun resolveFileAt(location: RSElementLocation): RSFile?

    public companion object : ProvidableInstance.Key<SchemaMetadataModule>

    override val key: ProvidableInstance.Key<*>
        get() = Companion
}

/**
 * Resolves a message type from the module using its [url]. Returns `null` if not found or not a message.
 */
public fun SchemaMetadataModule.resolveMessage(url: String): RSMessage? {
    return resolveDeclaration(url) as? RSMessage
}

/**
 * Resolves an enum type from the module using its [url]. Returns `null` if not found or not an enum.
 */
public fun SchemaMetadataModule.resolveEnum(url: String): RSEnum? {
    return resolveDeclaration(url) as? RSEnum
}

public fun SchemaMetadataModule.resolveDefinition(definition: RSProtoType.Definition<*>): RSNode? {
    return resolveDeclaration(definition.url)
}

/**
 * Combines two [SchemaMetadataModule]s into a single module that aggregates their schema files.
 *
 * @receiver The first module.
 * @param other The second module.
 * @return A new [SchemaMetadataModule] containing files from both modules.
 *
 * @warning If declarations with the same URL exist in both modules, the one from [other] will take precedence.
 */
public operator fun SchemaMetadataModule.plus(other: SchemaMetadataModule): SchemaMetadataModule {
    return SchemaMetadataModule(resolveAllFiles() + other.resolveAllFiles())
}

/**
 * Registers multiple global metadata modules into the [InstancesBuilder] context.
 *
 * If used, this effectively merges their contents and makes them available via dependency resolution.
 *
 * @param modules One or more [GlobalSchemaMetadataModule] instances to include.
 */
public fun InstancesBuilder.metadataModules(vararg modules: GlobalSchemaMetadataModule) {
    return register(SchemaMetadataModule(GlobalSchemaMetadataModule.resolveAllFiles() + modules.flatMap { it.resolveAllFiles() }))
}

// must be provided as providable instance
public val InstanceContainer.metadataModule: SchemaMetadataModule? get() = getInstance(SchemaMetadataModule)

/**
 * Default implementation of [SchemaMetadataModule] backed entirely by in-memory data structures.
 *
 * This is used by the global module as well as composed modules created at runtime.
 *
 * @constructor Accepts a list of [RSFile]s as the schema source.
 *
 * @warning In case of duplicate type URLs or file paths, the **last one in the input list wins**.
 * Be careful when merging schema files from different sources to avoid silent overwrites.
 */
internal class InMemorySchemaMetadataModule(
    private val files: List<RSFile>,
) : SchemaMetadataModule {
    private val allTypes by lazy {
        files.flatMap { file -> file.allTypes }
    }
    private val allServices by lazy {
        files.flatMap { file -> file.services }
    }
    private val declarationIndex by lazy {
        buildMap {
            files.forEach { file ->
                file.allTypes.forEach { type ->
                    put(type.typeUrl.value, type)
                }
                file.allEnums.forEach { enum ->
                    put(enum.typeUrl.value, enum)
                }
                file.services.forEach {
                    put(it.typeUrl.value, it)
                }
            }
        }
    }
    private val allExtends by lazy {
        files.flatMap { file -> file.allExtends }
    }


    private val filesIndex by lazy {
        buildMap {
            files.forEach { file ->
                put(file.location.relativePath.value, file)
            }
        }
    }

    override fun resolveAllFiles(): List<RSFile> = files

    override fun resolveAllTypes(): List<RSType> = allTypes.toList()

    override fun resolveAllServices(): List<RSService> = allServices.toList()
    override fun resolveAllExtends(): List<RSExtend> = allExtends.toList()

    override fun resolveDeclaration(url: String): RSNode? {
        return declarationIndex[url]
    }

    override fun resolveFileAt(location: RSElementLocation): RSFile? {
        return filesIndex[location.relativePath.value]
    }
}