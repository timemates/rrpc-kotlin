package org.timemates.rsp.options

import kotlin.jvm.JvmInline

@JvmInline
public value class Options(private val map: Map<Int, Any>) {
    public companion object {
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
}