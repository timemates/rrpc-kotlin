package org.timemates.rsp.pipeline

import org.timemates.rsp.annotations.ExperimentalRSProtoAPI
import org.timemates.rsp.options.Options

public interface PipelineContext2<TMetadata, TData> {
    public val metadata: TMetadata
    @property:ExperimentalRSProtoAPI
    public val data: Result<TData>
    public val options: Options
}