plugins {
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.sonarqube") version "5.1.0.4882"
    id("checkstyle")
    id("com.github.spotbugs") version "6.0.18"

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

// Integration test source set and task
sourceSets {
    val integrationTest by creating {
        java.srcDir("src/integrationTest/java")
        resources.srcDir("src/integrationTest/resources")
        compileClasspath += sourceSets.main.get().output + configurations.testCompileClasspath.get()
        runtimeClasspath += output + compileClasspath + configurations.testRuntimeClasspath.get()
    }
}

configurations.named("integrationTestImplementation") {
    extendsFrom(configurations.testImplementation.get())
}

configurations.named("integrationTestRuntimeOnly") {
    extendsFrom(configurations.testRuntimeOnly.get())
}

val integrationTest by tasks.registering(Test::class) {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter(tasks.test)
}

tasks.check {
    dependsOn(integrationTest)
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

sonarqube {
    properties {
        property("sonar.host.url", "http://localhost:9000")
        System.getenv("SONAR_TOKEN")?.let { property("sonar.token", it) }
    }
}

// Checkstyle configuration
checkstyle {
    toolVersion = "10.18.1"
}

spotbugs {
    toolVersion = "4.9.8"
    excludeFilter.set(file("config/spotbugs/exclude.xml"))
}

spotbugsPlugins("com.h3xstream.findsecbugs:findsecbugs-plugin:1.14.0")
}

tasks.withType<Checkstyle> {
    configFile = file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
    maxWarnings = 0
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
    ignoreFailures = true // Temporarily ignore failures due to Java 25 compatibility
}

tasks.check {
    dependsOn("checkstyleMain", "checkstyleTest", "checkstyleIntegrationTest", "spotbugsMain", "spotbugsTest")
}
