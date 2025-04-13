import org.gradle.internal.os.OperatingSystem

plugins {
    id(libs.plugins.conventions.jvm.library.get().pluginId)
    application
    alias(libs.plugins.graalvm.native)
    alias(libs.plugins.shadowJar)
}

group = "app.timemate.rrpc"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

kotlin {
    explicitApi()
}

application {
    mainClass.set("app.timemate.rrpc.generator.kotlin.MainKt")
}

val mainClassPath = "app.timemate.rrpc.generator.kotlin.MainKt"

// --------------------------------------
// Dependencies
// --------------------------------------
dependencies {
    // -- Project --
    implementation(libs.timemate.rrpc.pluginApi)

    // -- Coroutines --
    implementation(libs.kotlinx.coroutines)

    // -- SquareUp --
    implementation(libs.squareup.kotlinpoet)
}

// --------------------------------------
// GraalVM Native Configuration
// --------------------------------------
var osClassifier: String
var fileExtension: String

when {
    OperatingSystem.current().isLinux -> {
        osClassifier = "linux-x86_64"
        fileExtension = "" // No extension for Linux
    }
    OperatingSystem.current().isWindows -> {
        osClassifier = "windows-x86_64"
        fileExtension = ".exe" // Windows needs .exe
    }
    OperatingSystem.current().isMacOsX -> {
        osClassifier = "macos-aarch64"
        fileExtension = "" // No extension for macOS
    }
    else -> throw GradleException("Unsupported OS: ${OperatingSystem.current()}")
}

graalvmNative {
    binaries.named("main") {
        mainClass = mainClassPath
        useFatJar = true
        buildArgs.addAll(
            "--initialize-at-build-time=kotlin.DeprecationLevel",
            "-H:ReflectionConfigurationFiles=${project.layout.projectDirectory.dir("src/main/resources/META-INF/native-image/reflect-config.json")}",
            "-H:ResourceConfigurationFiles=${project.layout.projectDirectory.dir("src/main/resources/META-INF/native-image/resource-config.json")}",
            "-H:Name=rrpc-kotlin-gen-$osClassifier"
        )
    }
}

// --------------------------------------
// ShadowJar Configuration
// --------------------------------------
tasks.shadowJar {
    archiveClassifier.set("")
}

// --------------------------------------
// Artifact Publishing
// --------------------------------------
artifacts {
    add("archives", tasks.shadowJar)
}

configurations {
    named("runtimeElements") {
        outgoing.artifacts.clear()
        outgoing.artifact(tasks.shadowJar)
    }
    named("apiElements") {
        outgoing.artifacts.clear()
        outgoing.artifact(tasks.shadowJar)
    }
}

// --------------------------------------
// Maven Publishing
// --------------------------------------
mavenPublishing {
    coordinates(
        groupId = "app.timemate.rrpc",
        artifactId = "kotlin-generator",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing
    )

    pom {
        name.set("RRpc Kotlin Code Generator")
        description.set("Code-generation library for RRpc servers and clients.")
    }
}

tasks.matching { it.name.startsWith("publish") }.configureEach {
    dependsOn(tasks.shadowJar)
}