package org.timemates.rsp.metadata

public sealed interface RSPMetadata {
    public companion object {
        /**
         * This property implies the version of communication. It has
         * different versioning from RSP as such, only for global changes to the scheme.
         */
        public const val CURRENT_SCHEMA_VERSION: Int = 1
    }

    public val extra: ExtraMetadata

    public fun extra(extra: ExtraMetadata): RSPMetadata
}