plugins {
    `java-library`
    `java-test-fixtures`
}

val mockitoAgent: Configuration by configurations.creating

dependencies {
    // Deliberately NO project() dependency except :shared (for AuditablePrincipal).
    // No domain-user. That is the whole point of this module.
    api(project(":shared"))

    api(platform(libs.spring.boot.dependencies))
    annotationProcessor(platform(libs.spring.boot.dependencies))

    // api: consumers write their own SecurityFilterChain against these types.
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-webmvc")

    api(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    implementation(libs.springdoc.openapi)   // OpenApiSecurityScheme only

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-subclass")
    testImplementation("org.junit.jupiter:junit-jupiter-params")

    // --- testFixtures: BaseControllerTest, JwtTestSupport ---
    testFixturesApi(project(":shared"))
    testFixturesApi(project(":domain-user"))
    testFixturesApi(testFixtures(project(":shared")))
    testFixturesApi(platform(libs.spring.boot.dependencies))
    testFixturesApi("org.springframework.boot:spring-boot-starter-test")
    testFixturesApi("org.springframework.boot:spring-boot-starter-webmvc-test")
    testFixturesApi("org.springframework.security:spring-security-test")

    testRuntimeOnly(platform(libs.spring.boot.dependencies))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    mockitoAgent(libs.mockito.core) { isTransitive = false }
}

tasks.withType<JavaCompile>().configureEach {
    // -parameters needed in java-library modules for SpEL / @Value param resolution
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation", "-parameters"))
}

tasks.withType<Test> {
    failOnNoDiscoveredTests = false
    useJUnitPlatform()
    doFirst {
        jvmArgs("-javaagent:${mockitoAgent.singleFile.absolutePath}")
    }
}