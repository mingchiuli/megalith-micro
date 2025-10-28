import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    // Only declare plugin versions, don't apply to root project
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.springframework.boot") version "3.5.7" apply false
    id("org.graalvm.buildtools.native") version "0.11.2" apply false
}

subprojects {
    repositories { mavenCentral() }

    group = "wiki.chiu.megalith"

    // Set default version for all modules (cache module will override this)
    if (name != "cache") {
        version = "latest"
    }

    // Apply plugins to all subprojects
    plugins.apply("java")
    plugins.apply("io.spring.dependency-management")
    plugins.apply("org.springframework.boot")  // Apply to all modules for -parameters and AOT

    // Configure jar tasks based on module type
    if (name.startsWith("micro-")) {
        plugins.apply("org.graalvm.buildtools.native")

        // Microservice modules: generate executable jar
        tasks.named<Jar>("jar") {
            enabled = false
        }

        // Configure test tasks
        tasks.withType<Test> {
            useJUnitPlatform()
        }

        // Configure bootBuildImage for all microservices
        tasks.named<BootBuildImage>("bootBuildImage") {
            buildpacks = listOf(
                "docker.io/paketobuildpacks/oracle",
                "urn:cnb:builder:paketo-buildpacks/java-native-image",
                "docker.io/paketobuildpacks/health-checker"
            )

            // Default heap size, can be overridden in individual modules
            val heapSize = project.findProperty("nativeImageHeapSize") as String? ?: "128m"

            environment = mapOf(
                "BP_NATIVE_IMAGE_BUILD_ARGUMENTS" to """
                    -march=compatibility
                    --gc=serial
                    -R:MaxHeapSize=$heapSize
                    -O3
                    -J-XX:MaxRAMPercentage=80.0
                    -H:+CompactingOldGen
                    -H:+MLCallCountProfileInference
                    -H:+TrackPrimitiveValues
                    -H:+UsePredicates
                """.trimIndent(),
                "BP_HEALTH_CHECKER_ENABLED" to "true"
            )

            docker {
                publish.set(true)
                publishRegistry {
                    imageName.set("docker.io/mingchiuli/megalith-${project.name}:${version}")
                    url.set("https://docker.io")
                    username.set(System.getenv("DOCKER_USERNAME"))
                    password.set(System.getenv("DOCKER_PWD"))
                }
            }
        }
    } else {
        // Library modules: generate plain jar
        tasks.named<BootJar>("bootJar") {
            enabled = false
        }
        tasks.named<Jar>("jar") {
            enabled = true
        }
    }

    configure<DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.7")
        }
        dependencies {
            dependency("org.redisson:redisson:3.52.0")
            dependency("com.nimbusds:nimbus-jose-jwt:10.5")
            dependency("wiki.chiu.megalith:cache-spring-boot-starter:3.5.7")
        }
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_25
    }
}
