dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.redisson:redisson")
    implementation("com.nimbusds:nimbus-jose-jwt")
    implementation(project(":common"))
    implementation("wiki.chiu.megalith:cache-spring-boot-starter")
}