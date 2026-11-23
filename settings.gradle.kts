pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "megalith-micro"

include(
        "micro-auth",
        "micro-blog",
        "micro-user",
        "micro-exhibit",
        "api-auth",
        "api-user",
        "api-blog",
        "common-arch",
        "common-contract",
        "common-rpc",
        "common-web",
        "common-auth-web",
        "common-observability",
        "common-messaging",
        "common-native-hints",
        "common-scheduling",
        "common-outbox",
        "common-export",
        "cache"
)
