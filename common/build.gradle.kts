dependencies {
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("org.springframework.boot:spring-boot-micrometer-tracing-brave")
    implementation("io.zipkin.brave:brave-instrumentation-spring-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    compileOnly("org.springframework:spring-webmvc")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("tools.jackson.core:jackson-databind")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
}
