plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
}

kotlin {
    explicitApi()
}

group = "app.timemate.rrpc"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    // -- Project --
    commonMainImplementation(libs.rrpc)
    commonMainImplementation(projects.generator.core)

    // -- Kotlinx Serialization --
    commonMainImplementation(libs.kotlinx.serialization.proto)
}


mavenPublishing {
    coordinates(
        groupId = "app.timemate.rrpc",
        artifactId = "dynamic-serialization",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpc Kotlin Dynamic Serialization Generator")
        description.set("Code-generation library for generating dynamic KSerializers in Runtime based on common-schema types.")
    }
}