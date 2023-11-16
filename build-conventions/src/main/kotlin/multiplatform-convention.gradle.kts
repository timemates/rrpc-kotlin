import org.jetbrains.kotlin.gradle.dsl.*

plugins {
    kotlin("multiplatform")
}

kotlin {
    js(IR) {
        browser()
        nodejs()
    }
    iosArm64()
    iosX64()
    iosSimulatorArm64()
    jvm {
        jvmToolchain(11)
    }
}