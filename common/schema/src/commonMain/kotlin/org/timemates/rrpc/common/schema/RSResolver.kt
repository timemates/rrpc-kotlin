package org.timemates.rrpc.common.schema

import org.timemates.rrpc.common.schema.value.RMDeclarationUrl

public fun RSResolver(
    files: List<RSFile>,
): RSResolver = InMemoryRSResolver(files)

public fun RSResolver(
    vararg resolvers: RSResolver,
): RSResolver = InMemoryRSResolver(resolvers.flatMap { it.resolveAvailableFiles() })

/**
 * Interface for resolving various components (fields, types, files, services) in the RPC metadata model.
 * This provides a lookup mechanism for retrieving metadata elements such as types, services, fields,
 * extensions, and files based on unique identifiers like type URLs or package names.
 */
public interface RSResolver {

    /**
     * Resolves a field within a type by the given [typeMemberUrl].
     *
     * @param typeMemberUrl The URL that identifies the specific field within the type.
     * @return The corresponding [RSField] if found, or `null` if no matching field is found.
     */
    public fun resolveField(typeMemberUrl: RSTypeMemberUrl): RSField?

    /**
     * Resolves a type by the given [url].
     *
     * @param url The unique identifier for the type, typically used in protobuf definitions.
     * @return The corresponding [RSType] if found, or `null` if no matching type is found.
     */
    public fun resolveType(url: RMDeclarationUrl): RSType?

    /**
     * Resolves the file where a type is present.
     *
     * @param url The reference to the type.
     * @return The corresponding [RSFile] where the type is found, or `null` if no matching file is found.
     */
    public fun resolveFileOf(url: RMDeclarationUrl): RSFile?

    /**
     * Resolves a service by the given [url].
     *
     * @param url The unique identifier for the service, typically used in protobuf definitions.
     * @return The corresponding [RSService] if found, or `null` if no matching service is found.
     */
    public fun resolveService(url: RMDeclarationUrl): RSService?

    /**
     * Resolves all available files in the current [RSResolver].
     *
     * @return A sequence of all [RSFile]s available within this resolver.
     */
    public fun resolveAvailableFiles(): Sequence<RSFile>

    /**
     * Resolves all available services in the current [RSResolver].
     *
     * @return A sequence of all [RSService]s available within this resolver.
     */
    public fun resolveAllServices(): Sequence<RSService>

    /**
     * Resolves all available types in the current [RSResolver].
     *
     * @return A sequence of all [RSType]s available within this resolver.
     */
    public fun resolveAllTypes(): Sequence<RSType>
}

private class InMemoryRSResolver(
    private val files: List<RSFile>,
) : RSResolver {
    private val servicesIndex: Map<RMDeclarationUrl, RSService> by lazy {
        files.flatMap { it.services }.associateBy { it.typeUrl }
    }
    private val typesIndex: Map<RMDeclarationUrl, RSType> by lazy {
        files.flatMap { it.allTypes }.associateBy { it.typeUrl }
    }

    private val filesIndex: Map<RMDeclarationUrl, RSFile> by lazy {
        buildMap {
            files.forEach { file ->
                file.allTypes.forEach { type ->
                    put(type.typeUrl, file)
                }
            }
        }
    }
    private val fieldsIndex: Map<RSTypeMemberUrl, RSField> by lazy {
        buildMap {
            typesIndex.values
                .asSequence()
                .filterIsInstance<RSType.Message>()
                .flatMap { it.fields }
                .forEach { field ->
                    put(RSTypeMemberUrl(field.typeUrl, field.name), field)
                }
        }
    }

    override fun resolveField(typeMemberUrl: RSTypeMemberUrl): RSField? {
        return fieldsIndex[typeMemberUrl]
    }

    override fun resolveType(url: RMDeclarationUrl): RSType? {
        return typesIndex[url]
    }

    override fun resolveFileOf(url: RMDeclarationUrl): RSFile? {
        return filesIndex[url]
    }

    override fun resolveService(url: RMDeclarationUrl): RSService? {
        return servicesIndex[url]
    }

    override fun resolveAvailableFiles(): Sequence<RSFile> {
        return files.asSequence()
    }

    override fun resolveAllServices(): Sequence<RSService> {
        return servicesIndex.values.asSequence()
    }

    override fun resolveAllTypes(): Sequence<RSType> {
        return typesIndex.values.asSequence()
    }
}
