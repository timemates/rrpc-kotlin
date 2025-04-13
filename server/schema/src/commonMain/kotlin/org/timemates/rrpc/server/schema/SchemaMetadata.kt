package app.timemate.rrpc.server.schema

import app.timemate.rrpc.common.schema.RSResolver

public fun SchemaMetadata(resolver: RSResolver): SchemaMetadata = DelegatedSchemaMetadataGroup(resolver)

public interface SchemaMetadata : RSResolver {
    public companion object Global : SchemaMetadata by _resolver {
        /**
         * Register a [SchemaMetadata] to the global scope that is used by default.
         */
        public fun register(group: SchemaMetadata) {
            _resolver = DelegatedSchemaMetadataGroup(RSResolver(this, group))
        }

        /**
         * Register a [RSResolver] to the global scope that is used by default.
         */
        public fun register(resolver: RSResolver): Unit = register(SchemaMetadata(resolver))
    }
}

internal class DelegatedSchemaMetadataGroup(resolver: RSResolver) : SchemaMetadata, RSResolver by _resolver

private var _resolver: SchemaMetadata = DelegatedSchemaMetadataGroup(RSResolver(emptyList()))
