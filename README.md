# Spring Boot 4 PoC

Scaffolded Spring Boot 4 project using Java 25, MapStruct, Instancio, JUnit 6 and MongoDB.

## 🎯 Key Points

### Architecture & Structure
- **DDD-inspired layout**: `domain`, `application`, `infrastructure`, `web` layers
- **No Lombok**: Uses Java 25 native `record` types for immutability
- **MapStruct**: Type-safe DTO ↔ Entity mapping
- **Gradle Kotlin DSL**: Type-safe build configuration with IDE support

### Testing Stack
- **JUnit 6 (Jupiter)**: Modern testing framework with `@DisplayName`
- **Instancio**: Automatic test data generation
- **Mockito**: Mocking framework for unit tests
- **Testcontainers**: Real MongoDB for integration tests

### Why Gradle Kotlin DSL?
This project uses `build.gradle.kts` (Kotlin DSL) instead of `build.gradle` (Groovy DSL) for:
- ✅ **Type-safety**: Compile-time error detection in build scripts
- ✅ **IDE Support**: Full autocompletion, navigation, and refactoring
- ✅ **Consistency**: Same language paradigm as Java/Kotlin code
- ✅ **Maintainability**: Easier to catch configuration errors early

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
- The project uses the JUnit Jupiter platform. If you require specific “JUnit 6” coordinates, please update the dependency accordingly; public releases currently use the JUnit 5 (Jupiter) line.
