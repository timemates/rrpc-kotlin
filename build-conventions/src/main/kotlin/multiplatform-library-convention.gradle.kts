import org.jetbrains.kotlin.gradle.dsl.*

plugins {
    id("multiplatform-convention")
}

kotlin {
    explicitApi = ExplicitApiMode.Strict
}