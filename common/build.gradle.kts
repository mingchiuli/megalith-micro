
plugins {
    id("java")
}

group = "org.chiu"
version = "latest"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.redisson:redisson:3.37.0")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("org.apache.tomcat.embed:tomcat-embed-core")
    implementation("org.aspectj:aspectjweaver")
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-core")
    implementation("org.springframework:spring-web")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
}
