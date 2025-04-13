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
)

include(
    ":server:core",
    //":server:schema",
)

include(
    ":client:core",
    //":client:schema",
)

include(":generator")

include(":integration-tests")

//include(
//    ":tooling:rrpc-testing-app",
//)

//include(
//    ":internal:dynamic-serialization"
//)
