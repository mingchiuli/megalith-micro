import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

group = "org.chiu"
version = "latest"

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.redisson:redisson-spring-boot-starter:3.35.0")
	implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.amqp:spring-rabbit-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named<BootBuildImage>("bootBuildImage") {
	buildpacks = listOf("docker.io/paketobuildpacks/oracle", "urn:cnb:builder:paketo-buildpacks/java-native-image", "gcr.io/paketo-buildpacks/health-checker")
	environment = mapOf(
		"BP_NATIVE_IMAGE_BUILD_ARGUMENTS" to
			"""
				-march=compatibility
				--gc=serial
				-R:MaxHeapSize=256m
				-O3
			""",
		"BP_HEALTH_CHECKER_ENABLED" to "true"
	)
	docker {
		publish.set(true)
		publishRegistry {
			imageName.set("docker.io/mingchiuli/micro-exhibit:${version}")
			url.set("https://docker.io")
			val un = System.getenv("DOCKER_USERNAME")
			val pwd = System.getenv("DOCKER_PWD")
			username.set(un)
			password.set(pwd)
		}
	}
}

