plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    commonMainImplementation(libs.rsocket.client)
    commonMainImplementation(libs.kotlinx.serialization.proto)
}