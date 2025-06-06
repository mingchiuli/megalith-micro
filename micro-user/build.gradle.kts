import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

version = "latest"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.security:spring-security-crypto")
    implementation(project(":common"))
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.amqp:spring-rabbit-test")
}

tasks.withType<Test> { useJUnitPlatform() }

tasks.named<BootBuildImage>("bootBuildImage") {
    buildpacks =
            listOf(
                    "docker.io/paketobuildpacks/oracle",
                    "urn:cnb:builder:paketo-buildpacks/java-native-image",
                    "docker.io/paketobuildpacks/health-checker"
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
			""",
                    "BP_HEALTH_CHECKER_ENABLED" to "true",
            )
    docker {
        publish.set(true)
        publishRegistry {
            imageName.set("docker.io/mingchiuli/megalith-micro-user:${version}")
            url.set("https://docker.io")
            val un = System.getenv("DOCKER_USERNAME")
            val pwd = System.getenv("DOCKER_PWD")
            username.set(un)
            password.set(pwd)
        }
    }
}
