package org.timemates.rsp.pipeline

import org.timemates.rsp.annotations.ExperimentalInstancesApi
import org.timemates.rsp.annotations.ExperimentalRSProtoAPI
import org.timemates.rsp.options.Options
import org.timemates.rsp.instances.InstanceContainer
import org.timemates.rsp.instances.ProvidableInstance

/**
 * Represents the state of a pipeline during request or response processing.
 * Instances of this class hold information about options, metadata, data, and instances related to the pipeline.
 * This class is part of an experimental API and may undergo changes in future versions.
 *
 * @param options The options associated with the pipeline state.
 * @param metadata The metadata information associated with the pipeline state.
 * @param data The data associated with the pipeline state (if present, otherwise shows.
 * @param instances The map of providable instances associated with the pipeline state.
 */
@OptIn(ExperimentalInstancesApi::class)
public class PipelineContext<TMetadata, TData> @OptIn(ExperimentalInstancesApi::class) constructor(
    public val metadata: TMetadata,
    @property:ExperimentalRSProtoAPI
    public val data: Result<TData>,
    public val options: Options,
    override val instances: Map<ProvidableInstance.Key<*>, ProvidableInstance>,
) : InstanceContainer, ProvidableInstance {
    override val key: ProvidableInstance.Key<*>
        get() = Key

    public companion object Key : ProvidableInstance.Key<PipelineContext<*, *>>

    public fun copy(
        data: Result<TData> = this.data,
        options: Options = this.options,
        instances: Map<ProvidableInstance.Key<*>, ProvidableInstance> = this.instances,
        extraMetadata: Map<String, ByteArray>? = null,
    ) {
        return PipelineContext(
            metadata, data, options, instances,
        )
    }
}
