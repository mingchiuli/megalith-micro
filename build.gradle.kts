val springBootVersion = "3.5.6"
val springDependencyManagementVersion = "1.1.7"
val graalvmVersion = "0.11.1"
val redissonVersion = "3.52.0"
val nimbusJoseJwtVersion = "10.5"
val cacheStarterVersion = "3.5.7"

plugins {
    java
    id("org.springframework.boot") version springBootVersion apply false
    id("io.spring.dependency-management") version springDependencyManagementVersion
    id("org.graalvm.buildtools.native") version graalvmVersion apply false
}

repositories { mavenCentral() }

subprojects {
    repositories { mavenCentral() }

    group = "wiki.chiu.megalith"

    plugins.apply("java")
    plugins.apply("io.spring.dependency-management")

    // Only apply Spring Boot plugin to microservice modules
    if (name.startsWith("micro-")) {
        plugins.apply("org.springframework.boot")
        plugins.apply("org.graalvm.buildtools.native")
    }

    dependencyManagement {
        imports {
            ç("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
        }
        dependencies {
            dependency("org.redisson:redisson:$redissonVersion")
            dependency("com.nimbusds:nimbus-jose-jwt:$nimbusJoseJwtVersion")
            dependency("wiki.chiu.megalith:cache-spring-boot-starter:$cacheStarterVersion")
        }
    }

    java { sourceCompatibility = JavaVersion.VERSION_25 }
}
