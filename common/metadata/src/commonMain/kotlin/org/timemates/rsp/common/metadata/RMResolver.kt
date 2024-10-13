package org.timemates.rrpc.common.metadata

import org.timemates.rrpc.common.metadata.value.RMPackageName
import org.timemates.rrpc.common.metadata.value.RMTypeUrl

public fun RMResolver(
    project: List<RMFile>,
): RMResolver = InMemoryRMResolver(project)

public fun RMResolver(
    vararg resolvers: RMResolver,
): RMResolver = CompoundRMResolver(*resolvers)

/**
 * Interface for resolving various components (fields, types, files, services) in the RPC metadata model.
 * This provides a lookup mechanism for retrieving data based on unique identifiers like type URLs or package names.
 */
public interface RMResolver {
    /**
     * Resolves a field within a type by the given [typeMemberUrl].
     */
    public fun resolveField(typeMemberUrl: RMTypeMemberUrl): RMField?

    /**
     * Resolves a type by the given [typeUrl].
     *
     * @param typeUrl The unique identifier for the type, typically used in protobuf definitions.
     * @return The corresponding [RMType] if found, or `null` if no matching type is found.
     */
    public fun resolveType(typeUrl: RMTypeUrl): RMType?

    /**
     * Resolves a file by the given [packageName] and [name].
     *
     * @param packageName The package name where the file is defined.
     * @param name The name of the file to be resolved.
     * @return The corresponding [RMFile] if found, or `null` if no matching file is found.
     */
    public fun resolveFileOf(packageName: RMPackageName, name: String): RMFile?

    /**
     * Resolves file where a type is present.
     *
     * @param typeUrl reference to the type.
     * @return The corresponding [RMFile] if type is found in the one of the files, or
     * `null` if no matching file is found.
     */
    public fun resolveFileOf(typeUrl: RMTypeUrl): RMFile?

    /**
     * Resolves a service by the given [typeUrl].
     *
     * @param typeUrl The unique identifier for the service, typically used in protobuf definitions.
     * @return The corresponding [RMService] if found, or `null` if no matching service is found.
     */
    public fun resolveService(typeUrl: RMTypeUrl): RMService?

    /**
     * Resolves all files available in this particular [RMResolver]
     */
    public fun resolveAvailableFiles(): Sequence<RMFile>

    public fun resolveAllServices(): Sequence<RMService>
    public fun resolveAllTypes(): Sequence<RMType>

    public fun resolveNodesWithOption(optionFieldUrl: RMTypeMemberUrl): Sequence<RMNode>
    public fun resolveServicesWithOption(optionFieldUrl: RMTypeMemberUrl): Sequence<RMService>
    public fun resolveTypesWithOption(optionFieldUrl: RMTypeMemberUrl): Sequence<RMType>
    public fun resolveRpcsWithOption(optionFieldUrl: RMTypeMemberUrl): Sequence<RMRpc>
    public fun resolveFieldsWithOption(optionFieldUrl: RMTypeMemberUrl): Sequence<RMField>
    public fun resolveConstantsWithOption(optionFieldUrl: RMTypeMemberUrl): Sequence<RMEnumConstant>
}

private class InMemoryRMResolver(
    private val files: List<RMFile>,
) : RMResolver {
    override fun resolveField(typeMemberUrl: RMTypeMemberUrl): RMField? {
        TODO()
    }

    override fun resolveType(typeUrl: RMTypeUrl): RMType? {
        TODO()
    }

    override fun resolveFileOf(packageName: RMPackageName, name: String): RMFile? {
        TODO()
    }

    override fun resolveService(typeUrl: RMTypeUrl): RMService? {
        TODO()
    }

    override fun resolveAvailableFiles(): Sequence<RMFile> {
        return files.asSequence()
    }

    override fun resolveAllServices(): Sequence<RMService> {
        return resolveAvailableFiles().flatMap { it.services }
    }

    override fun resolveAllTypes(): Sequence<RMType> {
        return resolveAvailableFiles().flatMap { it.types }
    }

    override fun resolveNodesWithOption(optionFieldUrl: RMTypeMemberUrl): Sequence<RMNode> {
        TODO("Not yet implemented")
    }

    override fun resolveServicesWithOption(optionFieldUrl: RMTypeMemberUrl): Sequence<RMService> {
        TODO("Not yet implemented")
    }

    override fun resolveRpcsWithOption(optionFieldUrl: RMTypeUrl): Sequence<RMRpc> {
        TODO("Not yet implemented")
    }

    override fun resolveFieldsWithOption(optionFieldUrl: RMTypeUrl): Sequence<RMField> {
        TODO("Not yet implemented")
    }

    override fun resolveConstantsWithOption(optionFieldUrl: RMTypeUrl): Sequence<RMEnumConstant> {
        TODO("Not yet implemented")
    }
}

private class CompoundRMResolver(
    private vararg val resolvers: RMResolver,
) : RMResolver {
    override fun resolveField(typeMemberUrl: RMTypeMemberUrl): RMField? {
        return resolvers.firstNotNullOfOrNull { it.resolveField(typeMemberUrl) }
    }

    override fun resolveType(typeUrl: RMTypeUrl): RMType? {
        return resolvers.firstNotNullOfOrNull { it.resolveType(typeUrl) }
    }

    override fun resolveFileOf(packageName: RMPackageName, name: String): RMFile? {
        return resolvers.firstNotNullOfOrNull { it.resolveFileOf(packageName, name) }
    }

    override fun resolveService(typeUrl: RMTypeUrl): RMService? {
        return resolvers.firstNotNullOfOrNull { it.resolveService(typeUrl) }
    }

    override fun resolveAvailableFiles(): Sequence<RMFile> {
        return sequence {
            resolvers.forEach {
                yieldAll(it.resolveAvailableFiles())
            }
        }
    }

    override fun resolveAllServices(): Sequence<RMService> {
        return sequence {
            resolvers.forEach {
                yieldAll(it.resolveAllServices())
            }
        }
    }

    override fun resolveAllTypes(): Sequence<RMType> {
        return sequence {
            resolvers.forEach {
                yieldAll(it.resolveAllTypes())
            }
        }
    }

    override fun resolveNodesWithOption(optionFieldUrl: RMTypeMemberUrl): Sequence<RMNode> {
        return sequenceOf(resolveServicesWithOption(optionFieldUrl), resolveFieldsWithOption(optionFieldUrl), resolve
    }

    override fun resolveServicesWithOption(optionFieldUrl: RMTypeMemberUrl): Sequence<RMService> {
        return resolvers.asSequence().flatMap { resolver ->
            resolver.resolveAllServices().filter { optionFieldUrl in it.options }
        }
    }

    override fun resolveRpcsWithOption(optionFieldUrl: RMTypeMemberUrl): Sequence<RMRpc> {
        return resolvers.asSequence().flatMap { resolver ->
            resolver.resolveAllServices().flatMap { it.rpcs }.filter { rpc ->
                optionFieldUrl in rpc.options
            }
        }
    }

    override fun resolveFieldsWithOption(optionFieldUrl: RMTypeMemberUrl): Sequence<RMField> {
        return resolvers.asSequence().flatMap { resolver ->
            resolver.resolveAllTypes().filterIsInstance<RMType.Message>().flatMap { it.fields }.filter { field ->
                optionFieldUrl in field.options
            }
        }
    }

    override fun resolveConstantsWithOption(optionFieldUrl: RMTypeMemberUrl): Sequence<RMEnumConstant> {
        return resolvers.asSequence().flatMap { resolver ->
            resolver.resolveAllTypes().filterIsInstance<RMType.Enum>().flatMap { it.constants }.filter { constant ->
                optionFieldUrl in constant.options
            }
        }
    }
}