import org.springframework.boot.gradle.tasks.bundling.BootJar
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
