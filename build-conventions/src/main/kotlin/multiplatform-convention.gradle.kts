import org.jetbrains.kotlin.gradle.dsl.*

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    jvmToolchain(11)

    explicitApi = ExplicitApiMode.Strict
}