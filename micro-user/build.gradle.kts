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
    implementation(project(":common-contract"))
    implementation(project(":common-rpc"))
    implementation(project(":common-web"))
    implementation(project(":common-observability"))
    implementation(project(":common-export"))
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
}
