plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    commonMainImplementation(libs.kotlinx.serialization.proto)
    commonMainImplementation(libs.rsocket.core)
}

group = "org.timemates.rsp"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

kotlin {
    js(IR) {
        browser()
        nodejs()
    }
    iosArm64()
    iosX64()
    iosSimulatorArm64()
}