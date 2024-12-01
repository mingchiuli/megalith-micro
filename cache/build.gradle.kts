
import com.vanniktech.maven.publish.SonatypeHost

version = "3.3.5-FINAL-Z-1"

plugins {
    id("com.vanniktech.maven.publish") version "0.30.0"
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("$group", "cache-spring-boot-starter", "$version")

    pom {
        name.set("Megalith Cache")
        description.set("A Cache Framework for Megalith")
        inceptionYear.set("2024")
        url.set("https://github.com/mingchiuli/megalith-micro/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("chiu")
                name.set("mingchiuli")
                url.set("https://github.com/mingchiuli/")
            }
        }
        scm {
            url.set("https://github.com/mingchiuli/megalith-micro/")
            connection.set("scm:git:git://github.com/mingchiuli/megalith-micro.git")
            developerConnection.set("scm:git:ssh://git@github.com/mingchiuli/megalith-micro.git")
        }
    }
}

dependencies {
    implementation("org.redisson:redisson:3.39.0")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.aspectj:aspectjweaver")
    compileOnly("org.springframework.amqp:spring-rabbit")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    compileOnly("jakarta.annotation:jakarta.annotation-api")
    compileOnly("org.springframework:spring-context")
    compileOnly("org.springframework:spring-core")
    compileOnly("org.springframework:spring-web")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
}
