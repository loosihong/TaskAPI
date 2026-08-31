plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    api(platform(libs.spring.boot.dependencies))
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api(variantOf(libs.querydsl.jpa) { classifier("jakarta") })

    annotationProcessor(platform(libs.spring.boot.dependencies))
    annotationProcessor(variantOf(libs.querydsl.apt) { classifier("jakarta") })
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    annotationProcessor(libs.lombok)

    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-security")
    implementation(libs.mapstruct.core)
    implementation(libs.datasource.proxy)
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.security:spring-security-core")

    runtimeOnly("com.microsoft.sqlserver:mssql-jdbc")

    compileOnly(libs.lombok)


    testImplementation(platform(libs.spring.boot.dependencies))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-params")

    testRuntimeOnly(platform(libs.spring.boot.dependencies))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

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