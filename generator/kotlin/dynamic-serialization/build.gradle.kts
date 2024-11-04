plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
}

kotlin {
    explicitApi()
}

group = "org.timemates.rrpc"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    // -- Project --
    commonMainImplementation(projects.common.core)
    commonMainImplementation(projects.generator.core)

    // -- Kotlinx Serialization --
    commonMainImplementation(libs.kotlinx.serialization.proto)
}


mavenPublishing {
    coordinates(
        groupId = "org.timemates.rrpc",
        artifactId = "kotlin-generator",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpc Kotlin Dynamic Serialization Generator")
        description.set("Code-generation library for generating dynamic KSerializers in Runtime based on common-schema types.")
    }
}