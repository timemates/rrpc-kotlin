plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    commonMainApi(libs.kotlinx.serialization.proto)
    commonMainApi(projects.common.core)
}

kotlin {
    js(IR) {
        browser()
        nodejs()
    }
    iosArm64()
    iosX64()
    iosSimulatorArm64()
}

mavenPublishing {
    coordinates(
        groupId = "org.timemates.rrpc",
        artifactId = "common-metadata",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpcroto Common Metadata")
        description.set("Multiplatform Kotlin Mutadata library for RRpcroto servers and clients.")
    }
}