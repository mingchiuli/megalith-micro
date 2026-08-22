// Override default heap size for this service (needs more memory)
ext.set("nativeImageHeapSize", "256m")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation(project(":api-user"))
    implementation(project(":api-blog"))
    implementation(project(":api-search"))
    implementation(project(":common-contract"))
    implementation(project(":common-rpc"))
    implementation(project(":common-web"))
    implementation(project(":common-auth-web"))
    implementation(project(":common-observability"))
    implementation(project(":common-messaging"))
    implementation(project(":cache"))
    implementation("org.redisson:redisson")
}
