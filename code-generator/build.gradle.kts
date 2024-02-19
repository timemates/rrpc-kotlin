plugins {
    id(libs.plugins.conventions.jvm.library.get().pluginId)
}

kotlin {
    explicitApi()
}

group = "org.timemates.rsproto"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    implementation(libs.squareup.wire.schema)
    implementation(libs.squareup.kotlinpoet)
    implementation(libs.squareup.okio)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.squareup.okio.fakeFs)
}

mavenPublishing {
    coordinates(
        groupId = "org.timemates.rsproto",
        artifactId = "code-generator",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RSProto Client Core")
        description.set("Code-generation library for RSProto servers and clients.")
    }
}