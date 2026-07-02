pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Integrates the official Spring toolchain management inside settings
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "TaskAPI"

include("shared", "domain-user", "domain-task", "service-task")
include("shared")
include("domain-user")
include("domain-task")
include("service-task")
include("identity")