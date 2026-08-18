plugins {
    java
    alias(libs.plugins.spring.boot)
}

val mockitoAgent: Configuration by configurations.creating

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

dependencies {
    implementation(project(":domain-task"))
    implementation(project(":domain-user"))
    implementation(project(":shared"))
    implementation(project(":identity"))
    implementation(project(":integration-hackerrank"))
    // JPA, the MSSQL driver, QueryDSL's runtime types, and Spring Security all
    // arrive transitively through the modules above - not redeclared here.

    implementation(platform(libs.spring.boot.dependencies))
    annotationProcessor(platform(libs.spring.boot.dependencies))
    testImplementation(platform(libs.testcontainers.bom))

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-batch-jdbc")

    implementation(libs.springdoc.openapi)
    implementation(libs.jobrunr.spring)
    implementation(libs.mapstruct.core)
    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)
    annotationProcessor(libs.lombok.mapstruct.binding)
    annotationProcessor(libs.mapstruct.processor)
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation(testFixtures(project(":shared")))
    testImplementation(testFixtures(project(":identity")))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-batch-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:mssqlserver")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.mockito:mockito-subclass")
    testImplementation("org.junit.jupiter:junit-jupiter-params")


    testRuntimeOnly(platform(libs.spring.boot.dependencies))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    mockitoAgent(libs.mockito.core) { isTransitive = false }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("java.util.logging.config.file", "src/test/resources/logging.properties")
    doFirst {
        jvmArgs("-javaagent:${mockitoAgent.singleFile.absolutePath}")
    }
}