plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    // Deliberately NO project() dependency, in either direction. Infrastructure
    // wiring only: no domain types, no foreign contract, no security knowledge.
    // Split from :shared for the same reason as :shared-security - so domain
    // modules and service-identity don't carry Lettuce. Keeping it free of
    // :shared-security leaves the JWT-denylist direction open later.
    api(platform(libs.spring.boot.dependencies))
    api("org.springframework.boot:spring-boot-starter-data-redis")

    testFixturesApi(platform(libs.spring.boot.dependencies))
    testFixturesApi(platform(libs.testcontainers.bom))
    testFixturesApi("org.testcontainers:testcontainers")
    testFixturesApi("org.springframework:spring-test")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

tasks.withType<Test> {
    failOnNoDiscoveredTests = false
    useJUnitPlatform()
}