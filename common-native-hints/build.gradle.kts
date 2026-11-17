import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar
import org.gradle.external.javadoc.StandardJavadocDocletOptions

plugins {
    `java-library`
    `maven-publish`
    signing
}

val cacheVersion = providers.gradleProperty("cacheVersion")
version = cacheVersion.getOrElse("0.0.0-SNAPSHOT")

dependencies {
    api("org.springframework:spring-core")
    testImplementation("io.netty:netty-buffer")
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.named<Jar>("jar") {
    archiveClassifier.set("")
}

tasks.named<Javadoc>("javadoc") {
    // The registrar is discovered through aot.factories, not exported as a Java API.
    (options as StandardJavadocDocletOptions).addStringOption("-show-packages", "all")
}

val validateNativeHintsReleaseVersion = tasks.register("validateNativeHintsReleaseVersion") {
    doLast {
        val stableVersion = Regex("""(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)""")
        check(cacheVersion.isPresent && stableVersion.matches(cacheVersion.get())) {
            "A Maven Central release requires -PcacheVersion=<major>.<minor>.<patch>."
        }
    }
}

tasks.matching { it.name.endsWith("ToCentralStagingRepository") }.configureEach {
    dependsOn(validateNativeHintsReleaseVersion)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "megalith-native-hints"
            versionMapping {
                allVariants { fromResolutionResult() }
            }
            pom {
                name.set("Megalith Native Hints")
                description.set("Shared third-party GraalVM runtime hints for Megalith")
                url.set("https://github.com/mingchiuli/megalith-micro/")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("mingchiuli")
                    }
                }
                scm {
                    url.set("https://github.com/mingchiuli/megalith-micro/")
                    connection.set("scm:git:https://github.com/mingchiuli/megalith-micro.git")
                    developerConnection.set("scm:git:ssh://git@github.com/mingchiuli/megalith-micro.git")
                }
            }
        }
    }
    repositories {
        maven {
            name = "centralStaging"
            url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
            credentials {
                username = providers.gradleProperty("mavenCentralUsername").orNull
                password = providers.gradleProperty("mavenCentralPassword").orNull
            }
        }
    }
}

signing {
    val signingKey = providers.gradleProperty("signingInMemoryKey").orNull
    val signingPassword = providers.gradleProperty("signingInMemoryKeyPassword").orNull
    if (signingKey != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
    sign(publishing.publications["mavenJava"])
}
