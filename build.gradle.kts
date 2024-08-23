

subprojects {
	plugins.apply("org.springframework.boot:3.3.3")
	plugins.apply("io.spring.dependency-management:1.1.6")
	plugins.apply("org.graalvm.buildtools.native:0.10.2")
	plugins.apply("java")

	java {
		sourceCompatibility = JavaVersion.VERSION_21
	}

	repositories {
		mavenCentral()
	}
}

dependencies {
	implementation(project(":micro-gateway"))
	implementation(project(":micro-auth"))
	implementation(project(":micro-blog"))
	implementation(project(":micro-user"))
	implementation(project(":micro-exhibit"))
	implementation(project(":micro-websocket"))
	implementation(project(":micro-search"))
}
