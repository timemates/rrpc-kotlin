package org.timemates.rrpc.server.schema

import org.timemates.rrpc.common.schema.RSResolver

public fun SchemaLookup(resolver: RSResolver): SchemaLookup = DelegatedSchemaLookupGroup(resolver)

public interface SchemaLookup : RSResolver {
    public companion object Global : SchemaLookup by _resolver {
        /**
         * Register a [SchemaLookup] to the global scope that is used by default.
         */
        public fun register(group: SchemaLookup) {
            _resolver = DelegatedSchemaLookupGroup(RSResolver(this, group))
        }

        /**
         * Register a [RSResolver] to the global scope that is used by default.
         */
        public fun register(resolver: RSResolver): Unit = register(SchemaLookup(resolver))
    }
}

internal class DelegatedSchemaLookupGroup(resolver: RSResolver) : SchemaLookup, RSResolver by _resolver

private var _resolver: SchemaLookup = DelegatedSchemaLookupGroup(RSResolver(emptyList()))
