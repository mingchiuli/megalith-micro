// Override default heap size for this service (needs more memory)
ext.set("nativeImageHeapSize", "256m")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(project(":common"))
    implementation("wiki.chiu.megalith:cache-spring-boot-starter")
    implementation("org.redisson:redisson")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.brave:brave-instrumentation-spring-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.amqp:spring-rabbit-test")
}

