import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.DEFAULT)

    pom {
        url.set("https://github.com/timemates/rrpc-kotlin")
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
            url.set("https://github.com/timemates/rrpc-kotlin")
            connection.set("scm:git:git://github.com/timemates/rrpc-kotlin.git")
            developerConnection.set("scm:git:ssh://git@github.com/timemates/rrpc-kotlin.git")
        }

        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/timemates/rrpc-kotlin/issues")
        }
    }

    signAllPublications()
}