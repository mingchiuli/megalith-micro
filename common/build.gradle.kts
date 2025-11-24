dependencies {
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("org.springframework.boot:spring-boot-micrometer-tracing-brave")
    implementation("io.zipkin.brave:brave-instrumentation-spring-web")

    compileOnly("org.springframework:spring-webmvc")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("tools.jackson.core:jackson-databind")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    compileOnly("io.micrometer:micrometer-tracing-bridge-brave")
    compileOnly("io.zipkin.brave:brave-instrumentation-spring-web")
}
