plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.jvm)
}

val mockitoAgent: Configuration by configurations.creating

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

dependencies {
    implementation(project(":domain-task"))
    implementation(project(":domain-user"))
    implementation(project(":shared"))
    // JPA, the MSSQL driver, and QueryDSL's runtime types all arrive transitively
    // through shared / domain-user / domain-task's `api` dependencies above.

    implementation(platform(libs.spring.boot.dependencies))
    annotationProcessor(platform(libs.spring.boot.dependencies))
    testImplementation(platform(libs.testcontainers.bom))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    implementation(libs.springdoc.openapi)

    implementation(libs.mapstruct.core)
    compileOnly(libs.lombok)

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    annotationProcessor(libs.lombok)
    annotationProcessor(libs.lombok.mapstruct.binding)
    annotationProcessor(libs.mapstruct.processor)
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:mssqlserver")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.mockito:mockito-subclass")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation(testFixtures(project(":shared")))
    testImplementation(testFixtures(project(":domain-user")))

    mockitoAgent(libs.mockito.core) { isTransitive = false }
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("java.util.logging.config.file", "src/test/resources/logging.properties")
    doFirst {
        jvmArgs("-javaagent:${mockitoAgent.singleFile.absolutePath}")
    }
}