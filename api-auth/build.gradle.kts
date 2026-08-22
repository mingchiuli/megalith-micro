plugins {
    `java-library`
}

dependencies {
    api(project(":common-contract"))
    api("org.springframework:spring-web")
    testRuntimeOnly("tools.jackson.core:jackson-databind")
}
