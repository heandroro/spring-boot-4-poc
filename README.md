# Spring Boot 4 PoC

Scaffolded Spring Boot 4 project using Java 25, MapStruct, Instancio, JUnit 6 and MongoDB.

## 🎯 Key Points

### Architecture & Structure
- **DDD-inspired layout**: `domain`, `application`, `infrastructure`, `web` layers
- **No Lombok**: Uses Java 25 native `record` types for immutability
- **MapStruct**: Type-safe DTO ↔ Entity mapping
- **Gradle Kotlin DSL**: Type-safe build configuration with IDE support

### Phase 6: Infrastructure & OpenAPI
**Latest updates** (Fase 6 Infrastructure PR #7):
- ✅ **SpringDoc 3.0.0**: Full Spring Boot 4 / Spring Framework 7 support
- ✅ **Swagger UI & OpenAPI**: Fully functional at `/swagger-ui/index.html` and `/v3/api-docs`
- ✅ **Agent Architecture**: Specialized Copilot agents for unit & integration tests
- ✅ **Improved Docs**: Agent best-practices, performance guidance, security patterns

### GitHub Copilot Automation
- **Smart PR Reviews**: Automated code review in Portuguese (pt-BR)
- **Auto-generated PR Descriptions**: Copilot generates detailed PR descriptions automatically
- **Context-Aware**: Reads PR comments to avoid duplicate feedback
- **Selective Review**: Focuses on code (Java, YAML), ignores planning docs
- **Standards Validation**: DDD, security, testing, and MongoDB best practices

📖 **Details:** [.github/agents/README.md](.github/agents/README.md) | [GitHub Strategy](docs/github-approval-strategy.md)

### Testing Stack
- **JUnit 6 (Jupiter)**: Modern testing framework with `@DisplayName`
- **Instancio**: Automatic test data generation
- **Mockito**: Mocking framework for unit tests
- **Testcontainers**: Real MongoDB for integration tests

### OpenAPI / Swagger UI
After starting the app, access the API documentation in the browser:

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

**Note:** SpringDoc 3.0.0 is required for Spring Boot 4 / Spring Framework 7 compatibility. Endpoints are enabled by default; disable in production via `springdoc.api-docs.enabled=false` and `springdoc.swagger-ui.enabled=false`.

If authentication becomes required in the future, ensure `/v3/api-docs/**` and `/swagger-ui/**` remain permitted in security configuration for local access.

### Why Gradle (vs Maven)?
This project uses **Gradle** with **Kotlin DSL** (`build.gradle.kts`) for:
- ✅ **Performance**: 2-10x faster builds with incremental compilation, build cache, and parallel execution
- ✅ **Type-safety**: Compile-time error detection in build scripts (Kotlin DSL vs XML)
- ✅ **IDE Support**: Full autocompletion, navigation, and refactoring in build files
- ✅ **Flexibility**: Programmatic DSL allows custom tasks and conditional logic easily
- ✅ **Conciseness**: Less verbose than Maven's XML (~30-40% fewer lines)

## 🚀 Getting Started

### Prerequisites
- **Java 25+** (LTS: Java 21 recommended for stability)
- **MongoDB 7.0+** (via Docker)
- **Gradle** (wrapper included; no installation needed)

### Quick Start

```bash
# 1. Clone and navigate
git clone https://github.com/heandroro/spring-boot-4-poc.git
cd spring-boot-4-poc

# 2. Make Gradle wrapper executable (macOS/Linux)
chmod +x gradlew

# 3. Start MongoDB (Docker required)
docker-compose up -d

# 4. Run the app (JWT_SECRET required for security)
JWT_SECRET=dev-secret ./gradlew bootRun

# 5. Access the app
# API Docs: http://localhost:8080/swagger-ui/index.html
# OpenAPI JSON: http://localhost:8080/v3/api-docs
# Health: http://localhost:8080/actuator/health
```

### Build and Test

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

### Run Tests

```bash
./gradlew test
```

### Testing Notes
- **Unit Tests**: Run with Mockito and Instancio
- **Integration Tests**: Use Testcontainers with real MongoDB (requires Docker)
- **Conditional Execution**: Docker-based tests only run when `ENABLE_DOCKER_TESTS=true`
- **JUnit Platform**: Project uses JUnit Jupiter (JUnit 5 under the hood)

## 📚 Documentation

This README provides a concise overview. Detailed documentation is in [docs/](docs/):

- **[Architecture](docs/architecture.md)** - DDD layers, design decisions
- **[Agent Best Practices](docs/agent-best-practices.md)** - Copilot agent design, performance, size limits
- **[Java Records Best Practices](docs/java-records-best-practices.md)** - Records, Jackson, validation patterns
- **[Security](docs/security.md)** - JWT, authorization, CORS
- **[API Reference](docs/api.md)** - REST endpoints, contracts
- **[MongoDB](docs/mongodb.md)** - Schema design, indexes, queries
- **[Testing](docs/testing.md)** - Testing strategy, fixtures, coverage
- **[Deployment](docs/deployment.md)** - Environment setup, Docker
- **[Troubleshooting](docs/troubleshooting.md)** - Common issues
- **[GitHub Strategy](docs/github-approval-strategy.md)** - Copilot approval workflow

> **Note:** README.md is kept concise. For detailed information, see the linked documentation files.
