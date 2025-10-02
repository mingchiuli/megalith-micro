plugins {
    java
    id("org.springframework.boot") version "3.5.6" apply false
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.11.1" apply false
}

extra["springBootVersion"] = "3.5.6"
extra["redissonVersion"] = "3.52.0"
extra["nimbusJoseJwtVersion"] = "10.5"
extra["cacheStarterVersion"] = "3.5.7"

repositories { mavenCentral() }

subprojects {
    repositories { mavenCentral() }

    group = "wiki.chiu.megalith"

    plugins.apply("java")
    plugins.apply("io.spring.dependency-management")

    // Apply GraalVM plugin to all modules (for AOT processing)
    plugins.apply("org.graalvm.buildtools.native")

    // Only apply Spring Boot plugin to microservice modules
    if (name.startsWith("micro-")) {
        plugins.apply("org.springframework.boot")
    }

    val springBootVersion: String by rootProject.extra
    val redissonVersion: String by rootProject.extra
    val nimbusJoseJwtVersion: String by rootProject.extra
    val cacheStarterVersion: String by rootProject.extra

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
        }
        dependencies {
            dependency("org.redisson:redisson:$redissonVersion")
            dependency("com.nimbusds:nimbus-jose-jwt:$nimbusJoseJwtVersion")
            dependency("wiki.chiu.megalith:cache-spring-boot-starter:$cacheStarterVersion")
        }
    }

    java { sourceCompatibility = JavaVersion.VERSION_25 }
}
