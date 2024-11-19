package org.timemates.rrpc.codegen.adapters

import org.timemates.rrpc.codegen.configuration.GenerationOption
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.common.schema.RSResolver

public interface SchemaAdapter {
    /**
     * List of available options for schema adapter with description. For CLI and Gradle plugin.
     */
    public val options: List<GenerationOption>

    /**
     * This method is used for generating the code usually, but can be used for other
     * purposes, for an example, – logging.
     *
     * @return RMResolver that might be the same as [resolver] or new in cases
     * when you want to modify incoming data to the following adapters.
     */
    public fun process(
        options: GenerationOptions,
        resolver: RSResolver,
    )
}