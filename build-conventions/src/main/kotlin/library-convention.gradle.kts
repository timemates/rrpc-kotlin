plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    pom {
        url.set("https://github.com/timemates/rsproto")
        inceptionYear.set("2023")

        licenses {
            license {
                name.set("The MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("y9vad9")
                name.set("Vadym Yaroshchuk")
                url.set("https://github.com/y9vad9/")
            }
        }

        scm {
            url.set("https://github.com/timemates/rsproto")
            connection.set("scm:git:git://github.com/timemates/rsproto.git")
            developerConnection.set("scm:git:ssh://git@github.com/timemates/rsproto.git")
        }

        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/timemates/rsproto/issues")
        }
    }
}

publishing {
    repositories {
        maven {
            val isDev = version.toString().contains("dev")

            name = if (isDev) "timeMatesDev" else "timeMatesReleases"
            url = if (isDev) uri("https://maven.timemates.org/dev") else uri("https://maven.timemates.org/releases")

            credentials {
                username = System.getenv("REPOSILITE_USER")
                password = System.getenv("REPOSILITE_SECRET")
            }
        }
    }
}