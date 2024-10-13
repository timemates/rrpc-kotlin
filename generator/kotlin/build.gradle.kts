plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
}

kotlin {
    explicitApi()
}

group = "org.timemates.rrpc"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    commonMainImplementation(libs.squareup.kotlinpoet)
    commonMainImplementation(libs.squareup.okio)

    commonMainImplementation(projects.common.core)
    commonMainImplementation(projects.generator.core)
}

mavenPublishing {
    coordinates(
        groupId = "org.timemates.rrpc",
        artifactId = "kotlin-generator",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpcroto Kotlin Code Generator")
        description.set("Code-generation library for RRpcroto servers and clients.")
    }
}