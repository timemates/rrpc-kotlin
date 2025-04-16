enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        mavenLocal()
        maven("https://maven.timemates.org")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven("https://jitpack.io")
        maven("https://maven.y9vad9.com")
        maven("https://maven.timemates.org")
    }
}

rootProject.name = "rrpc"

includeBuild("build-conventions")

include(
    ":common:core",
    ":common:metadata",
    ":common:metadata:communication",
)

include(
    ":server:core",
    ":server:metadata",
)

include(
    ":client:core",
    ":client:metadata",
)

include(":generator")

include(":integration-tests")

//include(
//    ":tooling:rrpc-testing-app",
//)

//include(
//    ":internal:dynamic-serialization"
//)
