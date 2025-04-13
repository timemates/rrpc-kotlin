package app.timemate.rrpc.generator

import app.timemate.rrpc.generator.kotlin.options.KotlinPluginOptions
import app.timemate.rrpc.generator.plugin.api.logger.RLogger
import app.timemate.rrpc.generator.plugin.api.RSResolver
import kotlin.reflect.KClass

public data class GeneratorContext(
    public val options: KotlinPluginOptions,
    @PublishedApi
    internal val instances: Map<KClass<*>, Any>,
    public val logger: RLogger,
    public val resolver: RSResolver
) {
    public inline fun <reified T> getOrNull(): T? = this.instances[T::class] as T?
    public inline fun <reified T> get(): T = getOrNull<T>() ?: error("Instance is not found: ${T::class.qualifiedName}")

    public fun <T : Any> withInstance(value: T): GeneratorContext = copy(
        instances = instances.plus (value::class to value),
    )
}