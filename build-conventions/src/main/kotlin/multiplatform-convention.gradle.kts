import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    jvmToolchain(11)

    js(IR) {
        browser()
        nodejs()
    }

    if (HostManager.hostIsMac) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
        macosX64()
        macosArm64()
    }

    if (HostManager.hostIsLinux) {
        linuxX64()
    }

    if (HostManager.hostIsMingw) {
        mingwX64()
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
    }
}
