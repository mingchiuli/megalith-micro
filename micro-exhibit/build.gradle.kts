import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

version = "latest"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(project(":common"))
    implementation("wiki.chiu.megalith:cache-spring-boot-starter")
    implementation("org.redisson:redisson")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.amqp:spring-rabbit-test")
}

tasks.withType<Test> { useJUnitPlatform() }

tasks.named<BootBuildImage>("bootBuildImage") {
    buildpacks =
            listOf(
                    "docker.io/paketobuildpacks/oracle",
                    "urn:cnb:builder:paketo-buildpacks/java-native-image",
                    "gcr.io/paketo-buildpacks/health-checker"
            )
    environment =
            mapOf(
                    "BP_NATIVE_IMAGE_BUILD_ARGUMENTS" to
                            """
				-march=compatibility
				--gc=serial
				-R:MaxHeapSize=256m
				-O3
				-J-XX:MaxRAMPercentage=80.0
				-H:+CompactingOldGen
                -H:+TrackPrimitiveValues
                -H:+UsePredicates
                -XX:+UseCompactObjectHeaders
			""",
                    "BP_HEALTH_CHECKER_ENABLED" to "true"
            )
    docker {
        publish.set(true)
        publishRegistry {
            imageName.set("docker.io/mingchiuli/megalith-micro-exhibit:${version}")
            url.set("https://docker.io")
            val un = System.getenv("DOCKER_USERNAME")
            val pwd = System.getenv("DOCKER_PWD")
            username.set(un)
            password.set(pwd)
        }
    }
}
