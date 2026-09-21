// Override default heap size for this service (needs more memory)
ext.set("nativeImageHeapSize", "256m")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.security:spring-security-crypto")
    implementation("io.micrometer:micrometer-core")
    runtimeOnly("com.fasterxml.jackson.core:jackson-databind")
    implementation(project(":api-user"))
    implementation(project(":common-contract"))
    implementation(project(":common-rpc"))
    implementation(project(":common-web"))
    implementation(project(":common-observability"))
    implementation(project(":common-outbox"))
    implementation(project(":common-scheduling"))
    implementation(project(":common-export"))
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
}

val integrationTest = sourceSets.create("integrationTest")
integrationTest.compileClasspath += sourceSets.main.get().output
integrationTest.runtimeClasspath += sourceSets.main.get().output
configurations[integrationTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[integrationTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
    add(integrationTest.implementationConfigurationName, platform("org.testcontainers:testcontainers-bom:2.0.5"))
    add(integrationTest.implementationConfigurationName, "org.testcontainers:testcontainers-junit-jupiter")
}

tasks.register<Test>("integrationTest") {
    group = "verification"
    description = "Verifies outbox index usage and bulk delete flows against an isolated MariaDB instance."
    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
}
