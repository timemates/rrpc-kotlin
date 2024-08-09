package org.timemates.rsp.codegen.configuration

public enum class MessageBuilderType {
    /**
     * This type of builder implies static function `create` with lambda inside. The generation looks like that:
     * ```kotlin
     * public class Bar(public val x: Int = 0) {
     *  public companion object {
     *      @JvmStatic
     *      public fun create(block: DSLBuilder.() -> Unit): Bar {...}
     *  }
     *
     *  public class DSLBuilder {
     *      @JvmField
     *      public val x: Int = 0
     *
     *      ...
     *  }
     * }
     * ```
     */
    DSL,

    /**
     * This type of builder implies static function with `builder()` that returns classic java-style
     * builder. Here's an example:
     * ```kotlin
     * public class Bar(public val x: Int = 0) {
     *  public companion object {
     *      @JvmStatic
     *      public fun builder(): Builder {...}
     *  }
     *
     *  public class Builder {
     *      public fun x(value: Int): Builder {...}
     *      public fun build(): Bar {...}
     *  }
     * }
     * ```
     */
    CLASSIC,
}