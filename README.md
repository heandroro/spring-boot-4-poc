# Spring Boot 4 PoC

Scaffolded Spring Boot 4 project using Java 25, MapStruct, Instancio, JUnit 5 (Jupiter) and MongoDB.

## 🎯 Key Points

### Architecture & Structure
- **DDD-inspired layout**: `domain`, `application`, `infrastructure`, `web` layers
- **No Lombok**: Uses Java 25 native `record` types for immutability
- **MapStruct**: Type-safe DTO ↔ Entity mapping
- **Gradle Kotlin DSL**: Type-safe build configuration with IDE support

### GitHub Copilot Automation
- **Smart PR Reviews**: Automated code review in Portuguese (pt-BR)
- **Auto-generated PR Descriptions**: Copilot generates detailed PR descriptions automatically
- **Context-Aware**: Reads PR comments to avoid duplicate feedback
- **Selective Review**: Focuses on code (Java, YAML), ignores planning docs
- **Standards Validation**: DDD, security, testing, and MongoDB best practices

📖 **Details:** [.github/agents/README.md](.github/agents/README.md) | [GitHub Strategy](docs/github-approval-strategy.md)

### Testing Stack
- **JUnit 5 (Jupiter)**: Modern testing framework with `@DisplayName`
- **Instancio**: Automatic test data generation
- **Mockito**: Mocking framework for unit tests
- **Testcontainers**: Real MongoDB for integration tests

### Why Gradle (vs Maven)?
This project uses **Gradle** with **Kotlin DSL** (`build.gradle.kts`) for:
- ✅ **Performance**: 2-10x faster builds with incremental compilation, build cache, and parallel execution
- ✅ **Type-safety**: Compile-time error detection in build scripts (Kotlin DSL vs XML)
- ✅ **IDE Support**: Full autocompletion, navigation, and refactoring in build files
- ✅ **Flexibility**: Programmatic DSL allows custom tasks and conditional logic easily
- ✅ **Conciseness**: Less verbose than Maven's XML (~30-40% fewer lines)

Build and run

```bash
# make the wrapper executable (required on macOS / Linux)
chmod +x gradlew

./gradlew clean build
./gradlew bootRun
```

Docker-based tests (Testcontainers)

- Repository integration test (`CustomerRepositoryTest`) uses Testcontainers (MongoDB). It is disabled by default and only runs when the environment variable `ENABLE_DOCKER_TESTS=true` is set.
- To run it, ensure Docker is running and execute:

```bash
ENABLE_DOCKER_TESTS=true ./gradlew test
```

If Docker is not available, regular unit tests still run and the build remains green.

Run tests

```bash
./gradlew test
```

Notes
- Repository integration tests use Testcontainers and require Docker available on the machine. If you prefer an embedded Mongo solution, adjust tests accordingly.
 - To avoid failures when Docker is unavailable, the Testcontainers-based test is guarded by `@EnabledIfEnvironmentVariable` and only runs when `ENABLE_DOCKER_TESTS=true`.

JUnit platform
- The project uses the JUnit Jupiter platform. If you require specific "JUnit 6" coordinates, please update the dependency accordingly; public releases currently use the JUnit 5 (Jupiter) line.

## 📚 Documentation

This README provides a concise overview. Detailed documentation is in [docs/](docs/):

- **[Architecture](docs/architecture.md)** - DDD layers, design decisions
- **[Java Records Best Practices](docs/java-records-best-practices.md)** - Records, Jackson, validation patterns
- **[Security](docs/security.md)** - JWT, authorization, CORS
- **[API Reference](docs/api.md)** - REST endpoints, contracts
- **[MongoDB](docs/mongodb.md)** - Schema design, indexes, queries
- **[Testing](docs/testing.md)** - Testing strategy, fixtures, coverage
- **[Deployment](docs/deployment.md)** - Environment setup, Docker
- **[Troubleshooting](docs/troubleshooting.md)** - Common issues
- **[GitHub Strategy](docs/github-approval-strategy.md)** - Copilot approval workflow

### README Maintenance
Copilot can automatically update this README on each significant commit. See [.github/README-UPDATE-INSTRUCTIONS.md](.github/README-UPDATE-INSTRUCTIONS.md) for automation workflow.

> **Note:** README.md is kept concise. For detailed information, see the linked documentation files.
