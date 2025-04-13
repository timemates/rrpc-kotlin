package app.timemate.rrpc.generator

import app.timemate.rrpc.generator.plugin.api.result.ProcessResult

public interface Processor<T, R> {
    public suspend fun GeneratorContext.process(data: T): ProcessResult<R>

    public suspend fun process(data: T, context: GeneratorContext): ProcessResult<R> = with(context) {
        return process(data = data)
    }
}