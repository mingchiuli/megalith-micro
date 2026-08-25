import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    `java-library`
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

val integrationTest = sourceSets.create("integrationTest")
integrationTest.compileClasspath += sourceSets.main.get().output
integrationTest.runtimeClasspath += sourceSets.main.get().output

configurations[integrationTest.implementationConfigurationName].extendsFrom(
    configurations.testImplementation.get()
)
configurations[integrationTest.runtimeOnlyConfigurationName].extendsFrom(
    configurations.testRuntimeOnly.get()
)

tasks.named<Jar>("jar") {
    archiveClassifier.set("")
}

tasks.named<Javadoc>("javadoc") {
    include(
        "wiki/chiu/micro/cache/annotation/**",
        "wiki/chiu/micro/cache/handler/**",
        "wiki/chiu/micro/cache/key/**",
    )
    exclude("wiki/chiu/micro/cache/handler/impl/**", "wiki/chiu/micro/cache/key/impl/**")
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

val generatedPom = layout.buildDirectory.file("publications/mavenJava/pom-default.xml")
val validateCachePublication = tasks.register("validateCachePublication") {
    group = "verification"
    description = "Checks that the published starter POM exposes all required runtime APIs."
    dependsOn(tasks.named("generatePomFileForMavenJavaPublication"))
    inputs.file(generatedPom)

    doLast {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(generatedPom.get().asFile)
        val dependencies = document.getElementsByTagName("dependency")
        val scopes = buildMap {
            for (index in 0 until dependencies.length) {
                val dependency = dependencies.item(index)
                val children = dependency.childNodes
                var artifactId: String? = null
                var scope = "compile"
                for (childIndex in 0 until children.length) {
                    val child = children.item(childIndex)
                    when (child.nodeName) {
                        "artifactId" -> artifactId = child.textContent
                        "scope" -> scope = child.textContent
                    }
                }
                artifactId?.let { put(it, scope) }
            }
        }
        val requiredCompileDependencies = setOf(
            "spring-boot-starter",
            "spring-boot-starter-jackson",
            "redisson",
            "caffeine",
            "aspectjweaver",
            "micrometer-core",
        )
        requiredCompileDependencies.forEach { artifactId ->
            check(scopes[artifactId] == "compile") {
                "Published dependency $artifactId must have compile scope, but was ${scopes[artifactId] ?: "missing"}."
            }
        }
    }
}

tasks.named("check") {
    dependsOn(validateCachePublication)
}

tasks.register<Test>("integrationTest") {
    group = "verification"
    description = "Runs cache integration tests against Redis and RabbitMQ containers."
    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
}

dependencies {
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-jackson")
    api("org.redisson:redisson:4.7.0")
    api("com.github.ben-manes.caffeine:caffeine")
    api("org.aspectj:aspectjweaver")
    api("io.micrometer:micrometer-core")
    compileOnly("org.springframework.boot:spring-boot-amqp")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-amqp")
    add(
        integrationTest.implementationConfigurationName,
        platform("org.testcontainers:testcontainers-bom:2.0.5"),
    )
    add(
        integrationTest.implementationConfigurationName,
        "org.testcontainers:testcontainers-junit-jupiter",
    )
    add(
        integrationTest.implementationConfigurationName,
        "org.testcontainers:testcontainers-rabbitmq",
    )
}
