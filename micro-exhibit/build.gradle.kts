// Override default heap size for this service (needs more memory)
ext.set("nativeImageHeapSize", "256m")

val integrationTest = sourceSets.create("integrationTest")
integrationTest.compileClasspath += sourceSets.main.get().output
integrationTest.runtimeClasspath += sourceSets.main.get().output

configurations[integrationTest.implementationConfigurationName].extendsFrom(
    configurations.testImplementation.get()
)
configurations[integrationTest.runtimeOnlyConfigurationName].extendsFrom(
    configurations.testRuntimeOnly.get()
)

tasks.register<Test>("integrationTest") {
    group = "verification"
    description = "Runs micro-exhibit integration tests against a Redis container."
    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation(project(":api-user"))
    implementation(project(":api-blog"))
    implementation(project(":api-search"))
    implementation(project(":common-contract"))
    implementation(project(":common-rpc"))
    implementation(project(":common-web"))
    implementation(project(":common-auth-web"))
    implementation(project(":common-observability"))
    implementation(project(":common-messaging"))
    implementation(project(":cache"))
    implementation("io.micrometer:micrometer-core")
    implementation("org.redisson:redisson")
    add(
        integrationTest.implementationConfigurationName,
        platform("org.testcontainers:testcontainers-bom:2.0.5"),
    )
    add(
        integrationTest.implementationConfigurationName,
        "org.testcontainers:testcontainers-junit-jupiter",
    )
}
