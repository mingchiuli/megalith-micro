
version = "1.0"

sourceSets {
    create("rabbitmqSupport") {
        java {
            srcDirs("/src/rabbitmq/java")
            resources.srcDir("/src/rabbitmq/resources")
        }
    }
}

java {
    registerFeature("rabbitmqSupport") {
        usingSourceSet(sourceSets["rabbitmqSupport"])
        capability("$group", "cache-rabbit-support", "$version")
    }
}

dependencies {
    implementation("org.redisson:redisson:${ext.get("redisson.version")}")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.aspectj:aspectjweaver")
    sourceSets.named("rabbitmqSupport") {
        implementation("org.springframework.amqp:spring-rabbit")
    }
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    compileOnly("org.springframework:spring-context")
    compileOnly("org.springframework:spring-core")
    compileOnly("org.springframework:spring-web")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
}