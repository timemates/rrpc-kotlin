package org.timemates.rsp.options

/**
 * This class represents [protobuf options](https://protobuf.dev/programming-guides/proto3/#options).
 * @property name the name assigned to the given option.
 * @property tag unique identifier assigned to the given option.
 */
@Suppress("unused")
public data class Option<T>(public val name: String, public val tag: Int) {
    public companion object
}
