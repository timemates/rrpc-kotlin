plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    commonMainImplementation(libs.kotlinx.serialization.proto)
}

group = "org.timemates.rsproto"
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