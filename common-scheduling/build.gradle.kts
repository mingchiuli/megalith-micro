plugins {
    `java-library`
}

dependencies {
    api("org.redisson:redisson")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework:spring-context")
}
