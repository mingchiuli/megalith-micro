plugins {
    `java-library`
}

dependencies {
    api(project(":common-contract"))
    api("org.springframework:spring-context")
    api("org.springframework:spring-web")
}
