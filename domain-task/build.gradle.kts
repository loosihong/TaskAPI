plugins {
    `java-library`
}

val mockitoAgent: Configuration by configurations.creating

dependencies {
    api(project(":domain-user")) // TaskAssignee -> User; dashboard query joins QUser
    api(project(":shared"))

    api(platform(libs.spring.boot.dependencies))
    annotationProcessor(platform(libs.spring.boot.dependencies))

    api(variantOf(libs.querydsl.jpa) { classifier("jakarta") })
    annotationProcessor(variantOf(libs.querydsl.apt) { classifier("jakarta") })
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(platform(libs.testcontainers.bom))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.mockito:mockito-subclass")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation(testFixtures(project(":shared")))
    testImplementation(testFixtures(project(":domain-user")))

    mockitoAgent(libs.mockito.core) { isTransitive = false }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("java.util.logging.config.file", "src/test/resources/logging.properties")
    doFirst {
        jvmArgs("-javaagent:${mockitoAgent.singleFile.absolutePath}")
    }
}