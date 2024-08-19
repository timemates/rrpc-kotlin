package org.timemates.rsp.client.options

import org.timemates.rsp.options.Options

@JvmInline
public value class RPCsOptions(
    private val map: Map<String, Options>
) {
    public companion object {
        public val EMPTY: RPCsOptions = RPCsOptions(emptyMap())
    }

    public constructor(vararg pairs: Pair<String, Options>) : this(mapOf(*pairs))

    public operator fun get(name: String): Options? = map[name]
}

public fun RPCsOptions.getOrEmpty(name: String): Options = get(name) ?: Options.EMPTY