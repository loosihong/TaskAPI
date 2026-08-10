plugins {
    alias(libs.plugins.spotbugs) apply false
    alias(libs.plugins.spring.boot) apply false
}

// Type-safe `libs.*` accessors aren't resolvable inside subprojects {} lambdas -
// grab the catalog explicitly here and reference that instead.
val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
val spotbugsPluginId = libsCatalog.findPlugin("spotbugs").get().get().pluginId

allprojects {
    group = "com.example"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "checkstyle")
    apply(plugin = "pmd")
    apply(plugin = spotbugsPluginId)

    configure<CheckstyleExtension> {
        toolVersion = "10.17.0"
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        maxWarnings = 0
        isIgnoreFailures = false
    }

    configure<PmdExtension> {
        toolVersion = "7.3.0"
        ruleSetFiles = files(rootProject.file("config/pmd/ruleset.xml"))
        ruleSets = emptyList() // disable PMD's bundled default rulesets; use only ours
        isConsoleOutput = true
        isIgnoreFailures = false
    }

    afterEvaluate {
        tasks.findByName("checkstyleTest")?.enabled = false
        tasks.findByName("pmdTest")?.enabled = false
        tasks.findByName("checkstyleTestFixtures")?.enabled = false
        tasks.findByName("pmdTestFixtures")?.enabled = false
        tasks.findByName("spotbugsTestFixtures")?.enabled = false

        val toolchainService = project.extensions.getByType<JavaToolchainService>()

        tasks.withType<Checkstyle>().configureEach {
            javaLauncher.set(toolchainService.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(21))
            })
        }

        tasks.withType<Pmd>().configureEach {
            javaLauncher.set(toolchainService.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(21))
            })
        }
    }

    configure<com.github.spotbugs.snom.SpotBugsExtension> {
        toolVersion.set("4.10.2")
        effort.set(com.github.spotbugs.snom.Effort.MAX)
        reportLevel.set(com.github.spotbugs.snom.Confidence.MEDIUM)
        excludeFilter.set(rootProject.file("config/spotbugs/exclude.xml"))
        ignoreFailures.set(false)
    }

    tasks.withType<Checkstyle>().configureEach {
        // Skip QueryDSL Q-classes, MapStruct impls, and other annotation-processor output
        exclude("**/generated/**")
        reports {
            xml.required.set(false)
            html.required.set(true)
        }
    }

    tasks.withType<Pmd>().configureEach {
        exclude("**/generated/**")
        reports {
            xml.required.set(false)
            html.required.set(true)
        }
    }

    tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
        reports.create("html") {
            required.set(true)
        }
    }

    tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun>().configureEach {
        val envFile = rootProject.file(".env.dev")
        if (envFile.exists()) {
            envFile.readLines()
                .filter { it.isNotBlank() && !it.trimStart().startsWith("#") && it.contains("=") }
                .forEach { line ->
                    val (key, value) = line.split("=", limit = 2)
                    environment(key.trim(), value.trim())
                }
        }
    }
}