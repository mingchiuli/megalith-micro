plugins {
	java
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

ext {
  versions = [
    springboot = "3.3.2",
    redisson = "3.35.0",
		caffeine = "3.1.8"
  ]
}