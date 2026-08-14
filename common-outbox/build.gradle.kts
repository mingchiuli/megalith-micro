plugins {
    `java-library`
}

dependencies {
    api(project(":common-contract"))
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.redisson:redisson")
    implementation("io.micrometer:micrometer-core")
    implementation("tools.jackson.core:jackson-databind")
}
