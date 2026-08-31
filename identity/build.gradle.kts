plugins {
    `java-library`
}

val mockitoAgent: Configuration by configurations.creating

dependencies {
    api(project(":domain-user")) // AuthService.register() returns User
    api(project(":shared"))      // CustomUserDetails implements AuditablePrincipal
    api(project(":shared-security"))

    api(platform(libs.spring.boot.dependencies))
    annotationProcessor(platform(libs.spring.boot.dependencies))

    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation(libs.springdoc.openapi)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation("org.mockito:mockito-subclass")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation(testFixtures(project(":shared-security")))
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test") // @AutoConfigureMockMvc

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