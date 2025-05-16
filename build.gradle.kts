plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.10.6"
}

repositories { mavenCentral() }

subprojects {
    repositories { mavenCentral() }

    group = "wiki.chiu.megalith"

    plugins.apply("org.springframework.boot")
    plugins.apply("io.spring.dependency-management")
    plugins.apply("org.graalvm.buildtools.native")
    plugins.apply("java")

    dependencyManagement {
        dependencies {
            dependency("com.github.ben-manes.caffeine:caffeine:3.2.0")
            dependency("org.redisson:redisson:3.46.0")
            dependency("com.nimbusds:nimbus-jose-jwt:10.3")
            dependency("wiki.chiu.megalith:cache-spring-boot-starter:3.4.5.2")
        }
    }

    java { sourceCompatibility = JavaVersion.VERSION_24 }
}

dependencies {
    implementation(project(":micro-auth"))
    implementation(project(":micro-blog"))
    implementation(project(":micro-user"))
    implementation(project(":micro-exhibit"))
    implementation(project(":micro-search"))
    implementation(project(":common"))
    implementation(project(":cache"))
}
