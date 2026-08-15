plugins {
    `java-library`
}

dependencies {
    api(project(":common-contract"))
    implementation(project(":common-scheduling"))
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("io.micrometer:micrometer-core")
    implementation("tools.jackson.core:jackson-databind")

}
