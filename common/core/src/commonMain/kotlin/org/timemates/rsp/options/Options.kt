package org.timemates.rsp.options

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

@JvmInline
public value class Options(private val map: Map<Int, Any>) {
    public constructor(vararg pairs: Pair<Int, Any>) : this(mapOf(*pairs))

    public companion object {
        @JvmStatic
        public val EMPTY: Options = Options(emptyMap())
    }

    public operator fun <T> get(option: Option<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return map[option.tag] as? T
    }

    public operator fun plus(other: Options): Options {
        return Options(this.map + other.map)
    }

    public operator fun <T : Any> plus(other: Pair<Option<T>, T>): Options {
        return Options(this.map + (other.first.tag to other.second))
    }

    public operator fun minus(option: Option<*>): Options {
        return Options(map - option.tag)
    }

    public fun asMap(): Map<Int, Any> = map
}

public fun Options.hasOption(option: Option<*>): Boolean = get(option) != null