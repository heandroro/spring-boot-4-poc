# Integration Tests

This directory contains integration tests that verify system behavior with real external dependencies (MongoDB, etc.) using Testcontainers.

## Overview

Unlike unit tests that use mocks, integration tests:
- Start real services in Docker containers
- Verify actual persistence and serialization
- Test database queries against real databases
- Validate system behavior end-to-end

## Running Integration Tests

### Prerequisites

1. **Docker**: Must be installed and running
   ```bash
   docker --version
   docker ps  # Should succeed
   ```

2. **Environment Variable**: Set `ENABLE_DOCKER_TESTS=true`
   ```bash
   export ENABLE_DOCKER_TESTS=true
   ```

### Execute Tests

```bash
# Run all integration tests
ENABLE_DOCKER_TESTS=true ./gradlew integrationTest

# Run specific integration test
ENABLE_DOCKER_TESTS=true ./gradlew integrationTest --tests "*MongoCustomerRepositoryIntegrationTest"

# Run with verbose output
ENABLE_DOCKER_TESTS=true ./gradlew integrationTest --info
```

## Test Structure

### MongoCustomerRepositoryIntegrationTest

Comprehensive integration tests for MongoDB repository:

**Value Object Persistence**:
- ✅ Money serialization/deserialization
- ✅ Email serialization/deserialization
- ✅ Address serialization/deserialization

**MongoDB Operations**:
- ✅ Save and retrieve customers
- ✅ Custom query `findHighCreditUtilization`
- ✅ Find by email (unique index)
- ✅ Find by status (indexed queries)
- ✅ Count by status
- ✅ Delete operations

**Business Logic**:
- ✅ Credit operations persistence
- ✅ Domain event publishing
- ✅ State change persistence

## Why These Tests Are Important

The original `MongoCustomerRepositoryTest` was converted to unit tests with mocks (PR #16). While unit tests verify delegation logic, they cannot validate:

1. **Actual MongoDB Queries**: Real queries may behave differently than mocked responses
2. **Serialization/Deserialization**: Value Objects (Money, Email, Address) must serialize correctly to MongoDB
3. **Index Usage**: MongoDB indexes must be correctly defined and used
4. **Data Integrity**: State changes must persist correctly across save/retrieve cycles
5. **Query Performance**: Complex queries like `findHighCreditUtilization` need real data to test

## Performance Notes

- Integration tests are slower than unit tests (15-30s for container startup)
- Tests share a single MongoDB container instance per test class
- Container is automatically stopped after tests complete
- First run may be slower due to Docker image download

## CI/CD Integration

In CI environments without Docker or where Docker is restricted:
- Tests are automatically skipped (disabled by default)
- No failures occur when `ENABLE_DOCKER_TESTS` is not set
- This allows CI pipelines to run unit tests quickly

For full CI/CD validation with integration tests:
```yaml
# .github/workflows/ci.yml example
- name: Run Integration Tests
  env:
    ENABLE_DOCKER_TESTS: true
  run: ./gradlew integrationTest
```

## Troubleshooting

### Tests Timeout
If tests timeout waiting for MongoDB:
- Check Docker is running: `docker ps`
- Check Docker has sufficient resources (2GB+ RAM recommended)
- Increase timeout in test (currently set to 5 minutes)

### Container Won't Start
- Check Docker daemon is running
- Verify port 27017 is not already in use
- Try pulling image manually: `docker pull mongo:7.0`

### Tests Skipped
- Verify `ENABLE_DOCKER_TESTS=true` is set
- Check environment variable is exported: `echo $ENABLE_DOCKER_TESTS`

## References

- [Testing Strategy](../docs/testing.md)
- [MongoDB Configuration](../docs/mongodb.md)
- [Testcontainers Documentation](https://testcontainers.com/)
- [Spring Boot Test Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
