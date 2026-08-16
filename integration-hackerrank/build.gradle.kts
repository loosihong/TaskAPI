plugins {
    `java-library`
}

val mockitoAgent: Configuration by configurations.creating

dependencies {
    // No internal project() dependencies. This module is a leaf, like `shared`.
    api(platform(libs.spring.boot.dependencies))
    annotationProcessor(platform(libs.spring.boot.dependencies))

    // Boot 4 modular starters: restclient auto-configures RestClient.Builder,
    // jackson supplies the Jackson 3 databind + converters.
    api("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-jackson")

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(platform(libs.spring.boot.dependencies))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-subclass")
    testImplementation("org.junit.jupiter:junit-jupiter-params")

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