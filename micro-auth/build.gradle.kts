dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.brave:brave-instrumentation-spring-web")
    implementation("org.redisson:redisson")
    implementation("com.nimbusds:nimbus-jose-jwt")
    implementation(project(":common"))
    implementation("wiki.chiu.megalith:cache-spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

