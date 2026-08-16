pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Integrates the official Spring toolchain management inside settings
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "TaskAPI"

include("shared")
include("domain-user")
include("domain-task")
include("identity")
include("integration-hackerrank")
include("service-task")