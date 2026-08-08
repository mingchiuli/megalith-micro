plugins {
    `java-library`
}

dependencies {
    api(project(":common-contract"))
    implementation("org.springframework.boot:spring-boot-http-client")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("tools.jackson.core:jackson-databind")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
}
