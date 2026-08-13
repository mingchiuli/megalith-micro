plugins {
    `java-library`
}

dependencies {
    api(project(":common-web"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("tools.jackson.core:jackson-databind")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
}
