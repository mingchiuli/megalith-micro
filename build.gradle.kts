plugins {
	java
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.6"
	id("org.graalvm.buildtools.native") version "0.10.3"
}

repositories {
	mavenCentral()
}

subprojects {

	repositories {
		mavenCentral()
	}
	group = "chiu.wiki.megalith"

	plugins.apply("org.springframework.boot")
	plugins.apply("io.spring.dependency-management")
	plugins.apply("org.graalvm.buildtools.native")
	plugins.apply("java")

	java {
		sourceCompatibility = JavaVersion.VERSION_23
	}

	dependencies {
		ext {
			set("redisson.version", "3.37.0")
			set("jwt.version", "9.41.2")
		}
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
	implementation(project(":common"))
	implementation(project(":cache"))
}
