version = "latest"

dependencies {
    compileOnly("org.springframework:spring-web")
    compileOnly("org.springframework:spring-context")
    compileOnly("com.fasterxml.jackson.core:jackson-databind")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    compileOnly("io.micrometer:micrometer-tracing-bridge-brave")
    compileOnly("io.zipkin.brave:brave-instrumentation-spring-web")
}
