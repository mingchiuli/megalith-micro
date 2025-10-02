plugins {
    java
    id("org.springframework.boot") version "3.5.6" apply false
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.11.1" apply false
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
        dependencies {
            dependency("org.redisson:redisson:3.52.0")
            dependency("com.nimbusds:nimbus-jose-jwt:10.5")
            dependency("wiki.chiu.megalith:cache-spring-boot-starter:3.5.7")
        }
    }

    java { sourceCompatibility = JavaVersion.VERSION_25 }
}
