import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar

plugins {
    `maven-publish`
    signing
}

val cacheVersion = providers.gradleProperty("cacheVersion")
version = cacheVersion.getOrElse("0.0.0-SNAPSHOT")

val stableVersionPattern = Regex("""(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)""")
val validateCacheReleaseVersion = tasks.register("validateCacheReleaseVersion") {
    group = "publishing"
    description = "Validates the stable SemVer supplied for a Maven Central release."

    doLast {
        check(cacheVersion.isPresent) {
            "A Maven Central release requires -PcacheVersion=<major>.<minor>.<patch>."
        }
        check(stableVersionPattern.matches(cacheVersion.get())) {
            "cacheVersion must be a stable SemVer in <major>.<minor>.<patch> format."
        }
    }
}

tasks.matching {
    it.name.endsWith("ToCentralStagingRepository")
}.configureEach {
    dependsOn(validateCacheReleaseVersion)
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.named<Jar>("jar") {
    archiveClassifier.set("")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = "megalith-cache-spring-boot-starter"
            version = project.version.toString()

            pom {
                name.set("Megalith Cache Spring Boot Starter")
                description.set("Two-level cache and distributed eviction support for Megalith")
                inceptionYear.set("2025")
                url.set("https://github.com/mingchiuli/megalith-micro/")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("chiu")
                        name.set("mingchiuli")
                        url.set("https://github.com/mingchiuli/")
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
            url = uri(
                "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/"
            )
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

dependencies {
    implementation("org.redisson:redisson:4.7.0")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.aspectj:aspectjweaver")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    compileOnly("jakarta.annotation:jakarta.annotation-api")
    compileOnly("tools.jackson.core:jackson-databind")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("org.springframework.boot:spring-boot-amqp")
    testImplementation("tools.jackson.core:jackson-databind")
}
