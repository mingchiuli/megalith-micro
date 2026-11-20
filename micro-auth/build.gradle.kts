dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-rabbitmq")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.redisson:redisson")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation(project(":api-auth"))
    implementation(project(":api-user"))
    implementation(project(":common-contract"))
    implementation(project(":common-rpc"))
    implementation(project(":common-web"))
    implementation(project(":common-auth-web"))
    implementation(project(":common-observability"))
    implementation(project(":common-messaging"))
    implementation(project(":cache"))
}

val integrationTest = sourceSets.create("integrationTest")
integrationTest.compileClasspath += sourceSets.main.get().output
integrationTest.runtimeClasspath += sourceSets.main.get().output
configurations[integrationTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[integrationTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())
dependencies {
    add(integrationTest.implementationConfigurationName, "org.testcontainers:testcontainers-junit-jupiter")
}
tasks.register<Test>("integrationTest") {
    group = "verification"
    description = "Verifies atomic login-code operations against Redis."
    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
}
