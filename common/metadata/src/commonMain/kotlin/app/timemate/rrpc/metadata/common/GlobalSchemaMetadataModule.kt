package app.timemate.rrpc.metadata.common

import app.timemate.rrpc.proto.schema.RSFile


private val schemaFiles: MutableList<RSFile> = mutableListOf()

/**
 * A default singleton implementation of [SchemaMetadataModule] that maintains a shared list of [RSFile] schema definitions
 * across the application scope.
 *
 * This module is intended to be the global fallback when no scoped or explicitly provided [SchemaMetadataModule] is available.
 * It is backed by an internal mutable list of schema files that can be registered via [register].
 *
 * ## Behavior
 * - Schema files are stored in-memory and are accumulated through successive calls to [register].
 * - This instance delegates all operations to an internal [SchemaMetadataModule] built over a shared list.
 *
 * ## Usage
 * While [GlobalSchemaMetadataModule] can be used directly, **it is strongly recommended** to access it via dependency
 * injection or module provisioning patterns to reduce coupling and improve testability.
 *
 * - If used with a [app.timemate.rrpc.metadata.common.metadataModules], this module is automatically registered.
 * - If used manually, it must be passed explicitly along with other scoped modules to avoid runtime visibility issues.
 *
 * ## Recommendation
 * Do not use this object directly unless absolutely necessary. Instead, prefer to pass it as a [SchemaMetadataModule]
 * through a configurable or providable instance. This approach ensures that future changes to the behavior or lifecycle
 * of this module will require minimal changes across your codebase.
 */
public object GlobalSchemaMetadataModule : SchemaMetadataModule by SchemaMetadataModule(schemaFiles) {

    /**
     * Registers a collection of schema [RSFile]s into the global metadata context.
     *
     * @param files The list of [RSFile] instances to register globally.
     */
    public fun register(files: List<RSFile>) {
        schemaFiles.addAll(files)
    }

    public fun register(module: SchemaMetadataModule) {
        register(module.resolveAllFiles())
    }
}