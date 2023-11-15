plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    commonMainImplementation(libs.rsocket.server)
    commonMainImplementation(libs.kotlinx.serialization.proto)
    commonMainImplementation(libs.ktor.server.websockets)

    commonMainImplementation(libs.ktor.server.core)
}