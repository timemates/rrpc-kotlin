package app.timemate.rrpc.generator.kotlin

import okio.buffer
import okio.sink
import okio.source
import app.timemate.rrpc.generator.plugin.api.PluginService

public suspend fun main(args: Array<String>) {
    PluginService.main(
        args = args.asList(),
        input = System.`in`.source().buffer(),
        output = System.out.sink().buffer(),
        service = KotlinPluginService,
    )
}