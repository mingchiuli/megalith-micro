plugins {
    `java-library`
}

dependencies {
    api(project(":common-contract"))
    implementation(project(":common-scheduling"))
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("io.micrometer:micrometer-core")
    implementation("tools.jackson.core:jackson-databind")

    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
    testImplementation("org.testcontainers:mariadb:1.21.4")
    testImplementation("org.mariadb.jdbc:mariadb-java-client")
}
