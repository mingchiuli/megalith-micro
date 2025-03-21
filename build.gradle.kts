plugins {
    java
    id("org.springframework.boot") version "3.4.3"
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
            dependency("org.redisson:redisson:3.45.1")
            dependency("com.nimbusds:nimbus-jose-jwt:10.0.2")
            dependency("wiki.chiu.megalith:cache-spring-boot-starter:3.4.3.1")
        }
    }

    java { sourceCompatibility = JavaVersion.VERSION_23 }
}

dependencies {
    implementation(project(":micro-auth"))
    implementation(project(":micro-blog"))
    implementation(project(":micro-user"))
    implementation(project(":micro-exhibit"))
    implementation(project(":micro-websocket"))
    implementation(project(":micro-search"))
    implementation(project(":common"))
    implementation(project(":cache"))
}
