
group = "org.chiu"
version = "latest"


group = "wiki.chiu"
version = "1.0"

java {
    registerFeature("rabbitmqSupport") {
        usingSourceSet(sourceSets["main"])
        capability("$group", "cache-rabbit-support", "$version")
    }
}

dependencies {
    implementation("org.redisson:redisson")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.apache.tomcat.embed:tomcat-embed-core")
    implementation("org.aspectj:aspectjweaver")
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-core")
    implementation("org.springframework:spring-web")
    "rabbitmqSupportImplementation"("org.springframework.amqp:spring-rabbit")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
}