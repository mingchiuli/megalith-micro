plugins {
    `java-library`
}

dependencies {
    api(project(":common-web"))
    implementation(project(":auth-api"))
    implementation(project(":common-rpc"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
}
