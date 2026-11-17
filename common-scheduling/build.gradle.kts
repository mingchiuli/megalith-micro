plugins {
    `java-library`
}

dependencies {
    runtimeOnly(project(":common-native-hints"))
    api("org.redisson:redisson")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework:spring-context")
}
