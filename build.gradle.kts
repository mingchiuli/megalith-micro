import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.graalvm.buildtools.gradle.dsl.GraalVMExtension
import org.hibernate.orm.tooling.gradle.HibernateOrmSpec

plugins {
    // Only declare plugin versions, don't apply to root project
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.springframework.boot") version "4.0.2" apply false
    id("org.graalvm.buildtools.native") version "0.11.4" apply false
    id("org.hibernate.orm") version "7.2.4.Final" apply false
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

        if (name == "micro-user" || name == "micro-blog") {
            plugins.apply("org.hibernate.orm")

            configure<HibernateOrmSpec> {
                enhancement {
                    enableAssociationManagement = true
                }
            }
        }

        // Configure test tasks
        tasks.withType<Test> {
            useJUnitPlatform()
        }

        // Configure GraalVM Native Image compilation (仅用于本地测试)
        configure<GraalVMExtension> {
            binaries {
                named("main") {
                    // 本地测试专用的编译参数
                    val localHeapSize = project.findProperty("localNativeHeapSize") as String? ?: "256m"

                    buildArgs.addAll(
                        "-march=compatibility",
                        "--gc=serial",
                        "-R:MaxHeapSize=$localHeapSize",
                    )

                    // 启用快速构建（测试环境）
                    quickBuild.set(true)
                    verbose.set(false)
                }
            }
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
    }

    configure<DependencyManagementExtension> {
        dependencies {
            dependency("org.redisson:redisson:4.2.0")
            dependency("com.nimbusds:nimbus-jose-jwt:10.7")
            dependency("wiki.chiu.megalith:cache-spring-boot-starter:4.0.3")
        }
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_25
    }
}
