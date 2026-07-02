plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    api(platform(libs.spring.boot.dependencies))
    annotationProcessor(platform(libs.spring.boot.dependencies))

    // BaseEntity/BaseRecord/audit are JPA types every module needs to see -> api.
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api(variantOf(libs.querydsl.jpa) { classifier("jakarta") })
    annotationProcessor(variantOf(libs.querydsl.apt) { classifier("jakarta") })
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation(libs.mapstruct.core)
    runtimeOnly("com.microsoft.sqlserver:mssql-jdbc")

    // GlobalExceptionHandler needs spring-web (@RestControllerAdvice, ResponseEntity)
    // and spring-security-core (BadCredentialsException). Nothing shared re-exposes
    // these publicly, so implementation is enough.
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.security:spring-security-core")

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // --- testFixtures: consumed by domain-user, domain-task, service-task ---
    testFixturesApi(platform(libs.spring.boot.dependencies))
    testFixturesApi(platform(libs.testcontainers.bom))
    testFixturesApi("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testFixturesApi("org.springframework.boot:spring-boot-starter-test")
    testFixturesApi("org.springframework.boot:spring-boot-starter-validation") // BaseConstraintsTest
    testFixturesApi("org.springframework.boot:spring-boot-testcontainers")
    testFixturesApi("org.testcontainers:mssqlserver")
    testFixturesApi("org.testcontainers:junit-jupiter")
    testFixturesApi(variantOf(libs.querydsl.jpa) { classifier("jakarta") })
    testFixturesCompileOnly(libs.lombok)
    testFixturesAnnotationProcessor(libs.lombok)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}