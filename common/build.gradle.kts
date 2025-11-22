dependencies {
    compileOnly("org.springframework:spring-web")
    compileOnly("org.springframework:spring-webmvc")
    compileOnly("org.springframework:spring-context")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("tools.jackson.core:jackson-databind")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    compileOnly("jakarta.annotation:jakarta.annotation-api")
    compileOnly("io.micrometer:micrometer-tracing-bridge-brave")
    compileOnly("io.zipkin.brave:brave-instrumentation-spring-web")
}
