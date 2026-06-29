plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.jvm)
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

repositories {
    mavenCentral()
}

// Custom configuration to resolve the Mockito agent jar path for tests dynamically
val mockitoAgent: Configuration by configurations.creating

dependencies {
    // --- Native Gradle BOM Platforms ---
    implementation(platform(libs.spring.boot.dependencies))
    annotationProcessor(platform(libs.spring.boot.dependencies))
    testImplementation(platform(libs.testcontainers.bom))

    // --- Web, Security & Validation (Versions managed by Spring BOM) ---
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // --- Data JPA & Driver (Versions managed by Spring BOM) ---
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.microsoft.sqlserver:mssql-jdbc")

    // --- QueryDSL (Uses Catalog + Classifier via variantOf) ---
    implementation(variantOf(libs.querydsl.jpa) { classifier("jakarta") })

    // --- JWT (Catalog) ---
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    // --- API Documentation (Catalog) ---
    implementation(libs.springdoc.openapi)

    // --- MapStruct & Lombok (Catalog) ---
    implementation(libs.mapstruct.core)
    compileOnly(libs.lombok)

    // --- Kotlin Runtime ---
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // --- Annotation Processors (Catalog) ---
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.lombok.mapstruct.binding)
    annotationProcessor(libs.mapstruct.processor)
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor(variantOf(libs.querydsl.apt) { classifier("jakarta") })
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // --- Testing (Versions managed by Spring & Testcontainers BOMs) ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:mssqlserver")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.mockito:mockito-subclass")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Dynamic Mockito Core agent attachment (Catalog)
    mockitoAgent(libs.mockito.core) { isTransitive = false }
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("java.util.logging.config.file", "src/test/resources/logging.properties")

    doFirst {
        jvmArgs("-javaagent:${mockitoAgent.singleFile.absolutePath}")
    }
}