plugins {
    java
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springframework.boot") version "3.5.6" apply false
    id("org.graalvm.buildtools.native") version "0.11.1" apply false
}

repositories { mavenCentral() }

subprojects {
    repositories { mavenCentral() }

    group = "wiki.chiu.megalith"

    // Apply plugins in order
    plugins.apply("java")
    plugins.apply("io.spring.dependency-management")
    plugins.apply("org.springframework.boot")  // Apply to all modules for -parameters and AOT
    plugins.apply("org.graalvm.buildtools.native")

    // Configure jar tasks based on module type
    if (name.startsWith("micro-")) {
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

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.6")
        }
        dependencies {
            dependency("org.redisson:redisson:3.52.0")
            dependency("com.nimbusds:nimbus-jose-jwt:10.5")
            dependency("wiki.chiu.megalith:cache-spring-boot-starter:3.5.7")
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_25
    }
}
