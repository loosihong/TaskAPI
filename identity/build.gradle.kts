plugins {
    `java-library`
    `java-test-fixtures`
}

val mockitoAgent: Configuration by configurations.creating

dependencies {
    api(project(":domain-user")) // AuthService.register() returns User
    api(project(":shared"))      // CustomUserDetails implements AuditablePrincipal

    api(platform(libs.spring.boot.dependencies))
    annotationProcessor(platform(libs.spring.boot.dependencies))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    implementation(libs.springdoc.openapi)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation("org.mockito:mockito-subclass")
    testImplementation("org.junit.jupiter:junit-jupiter-params")

    // --- testFixtures: AbstractSecuredIntegrationTest, BaseControllerTest ---
    testFixturesApi(project(":shared"))                 // GlobalExceptionHandler (shared's main)
    testFixturesApi(testFixtures(project(":shared")))   // TestcontainersConfig (shared's testFixtures)
    testFixturesApi(platform(libs.spring.boot.dependencies))
    testFixturesApi("org.springframework.boot:spring-boot-starter-test")
    testFixturesApi("org.springframework.boot:spring-boot-starter-webmvc-test") // @AutoConfigureMockMvc

    testRuntimeOnly(platform(libs.spring.boot.dependencies))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    mockitoAgent(libs.mockito.core) { isTransitive = false }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

tasks.withType<Test> {
    failOnNoDiscoveredTests = false
    useJUnitPlatform()
    systemProperty("java.util.logging.config.file", "src/test/resources/logging.properties")
    doFirst {
        jvmArgs("-javaagent:${mockitoAgent.singleFile.absolutePath}")
    }
}