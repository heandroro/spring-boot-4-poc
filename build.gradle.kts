plugins {
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    java
    jacoco
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Using Tomcat (default). Undertow is supported but spring-boot-starter-undertow
    // is not available in Spring Boot 4.0.0's dependency management yet.
    // See: https://spring.io/projects/spring-boot (lists Undertow as supported)
    // TODO: Migrate to Undertow when available in Spring Boot 4.x BOM
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    implementation("org.apache.commons:commons-lang3:3.17.0")

    // OpenAPI/Swagger UI via SpringDoc (exposes /v3/api-docs and /swagger-ui)
    // SpringDoc 3.x is required for Spring Boot 4 / Spring Framework 7
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")

    // Spring Boot test support for @WebMvcTest / MockMvc
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-test")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
    // Include spring-test explicitly for MockMvc
    testImplementation("org.springframework:spring-test")
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.1")
    testImplementation("org.instancio:instancio-junit:5.5.1")
    // JavaFaker for realistic test data (names, addresses, emails)
    // Exclude snakeyaml to avoid version conflicts with Spring Boot's managed version
    testImplementation("com.github.javafaker:javafaker:1.0.2") {
        exclude(group = "org.yaml", module = "snakeyaml")
    }
    testImplementation("org.testcontainers:mongodb:1.20.4")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    // Exclude classes not relevant for coverage (bootstrap/config/etc.)
    classDirectories.setFrom(
        files(classDirectories.files.map { fileTree(it) {
            exclude(
                "**/SpringBoot4PocApplication*",
                "**/infrastructure/config/**",
                "**/web/exception/**",
                "**/infrastructure/event/**"
            )
        } })
    )
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

jacoco {
    toolVersion = "0.8.13"
}
