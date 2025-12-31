plugins {
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.sonarqube") version "7.2.1.6560"
    id("checkstyle")
    id("com.github.spotbugs") version "6.0.18"
    java
    jacoco
}

group = "com.example"
version = "0.0.1-SNAPSHOT"


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
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

    // FindSecBugs plugin for SpotBugs
    spotbugsPlugins("com.h3xstream.findsecbugs:findsecbugs-plugin:1.14.0")
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

    // Default to running integration tests with Docker enabled when the environment
    // variable is not explicitly set by the user. If user sets ENABLE_DOCKER_TESTS in
    // their environment, that value is respected.
    if (System.getenv("ENABLE_DOCKER_TESTS") == null) {
        environment("ENABLE_DOCKER_TESTS", "true")
    }
}

// Integration tests are intentionally not part of the default `check` lifecycle
// to avoid running Docker/Testcontainers during quick checks or on PR jobs.
// Run them explicitly using `./gradlew integrationTest` or enable via
// the ENABLE_DOCKER_TESTS environment variable when needed.
// tasks.check {
//     dependsOn(integrationTest)
// }

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
    // Use the same class directories filtering as jacocoTestReport so that
    // exclusions (config, event handlers, etc.) are applied consistently
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

    violationRules {
        rule {
            // Enforce minimum covered ratio for lines and branches
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.90".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}

// Make sure coverage verification runs as part of the check lifecycle
tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

jacoco {
    toolVersion = "0.8.13"
}




sonarqube {
    properties {
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.token", "sqa_949d5b009b059821d1a6dc3d8a46ccaf44faf1ca")
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

// Verify that integration tests follow the naming convention: classes annotated with
// integration-related annotations must end with 'IT'. This helps discoverability and
// aligns with Gradle source set integrationTest separation.
tasks.register("checkIntegrationTestNames") {
    group = "verification"
    description = "Fail if integration tests are annotated but their class name doesn't end with IT"
    doLast {
        val integrationAnnotations = listOf("@SpringBootTest", "@Testcontainers", "@DataMongoTest", "@Container", "@EnabledIfEnvironmentVariable")
        val srcDir = file("src/test/java")
        val violations = mutableListOf<String>()

        if (srcDir.exists()) {
            fileTree(srcDir).matching { include("**/*.java") }.forEach { f ->
                val text = f.readText()
                val hasIntegrationAnnotation = integrationAnnotations.any { text.contains(it) }
                if (hasIntegrationAnnotation) {
                    val classRegex = Regex("class\\s+([A-Za-z_][A-Za-z0-9_]*)")
                    val m = classRegex.find(text)
                    if (m != null) {
                        val className = m.groupValues[1]
                        if (!className.endsWith("IT")) {
                            violations.add("${f.relativeTo(projectDir)}: class '$className' appears to be an integration test but does not end with 'IT'")
                        }
                    }
                }
            }
        }

        if (violations.isNotEmpty()) {
            println("Integration test naming violations found:")
            violations.forEach { println("  - $it") }
            throw GradleException("Integration test naming convention violations detected (see output above)")
        }
    }
}

// Ensure it runs as part of check lifecycle
tasks.named("check").configure {
    dependsOn("checkIntegrationTestNames")
}

// Task to install repository git hooks locally (sets core.hooksPath to .githooks)

tasks.register<org.gradle.api.tasks.Exec>("installGitHooks") {
    group = "development"
    description = "Install git hooks (sets core.hooksPath to .githooks and makes pre-commit executable)"
    // configure the external command on the Exec task itself
    commandLine("git", "config", "core.hooksPath", ".githooks")
    doLast {
        // ensure the pre-commit script is executable
        file(".githooks/pre-commit").setExecutable(true)
        println("Installed git hooks (core.hooksPath set to .githooks)")
    }
}
