dependencies {
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
    implementation("io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:2.25.0-alpha")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-http-client")

    compileOnly("org.springframework:spring-webmvc")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("tools.jackson.core:jackson-databind")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
}
