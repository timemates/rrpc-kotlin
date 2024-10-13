package org.timemates.rrpc.server.metadata

import org.timemates.rrpc.common.metadata.RMResolver

public fun MetadataLookup(resolver: RMResolver): MetadataLookup = DelegatedMetadataLookupGroup(resolver)

public interface MetadataLookup : RMResolver {
    public companion object Global : MetadataLookup by _resolver {
        /**
         * Register a [MetadataLookup] to the global scope that is used by default.
         */
        public fun register(group: MetadataLookup) {
            _resolver = DelegatedMetadataLookupGroup(RMResolver(this, group))
        }

        /**
         * Register a [RMResolver] to the global scope that is used by default.
         */
        public fun register(resolver: RMResolver) {
            register(DelegatedMetadataLookupGroup(resolver))
        }
    }
}

internal class DelegatedMetadataLookupGroup(resolver: RMResolver) : MetadataLookup, RMResolver by _resolver

private var _resolver: MetadataLookup = DelegatedMetadataLookupGroup(RMResolver(emptyList()))
