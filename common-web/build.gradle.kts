plugins {
    `java-library`
}

dependencies {
    api(project(":common-contract"))
    api(project(":common-rpc"))
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
}
