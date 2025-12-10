# Troubleshooting

## Common Issues & Solutions

### MongoDB Connection Issues

#### Problem: `MongoException: failed to authenticate`

**Cause**: Invalid MongoDB URI or authentication credentials.

**Solutions**:

1. **Verify MongoDB URI**:
   ```yaml
   # application.yml
   spring:
     data:
       mongodb:
         uri: mongodb://localhost:27017/spring-boot-poc
   ```

2. **Check credentials** (for MongoDB Atlas):
   ```bash
   export MONGODB_URI="mongodb+srv://user:password@cluster.mongodb.net/spring-boot-poc?retryWrites=true&w=majority"
   ```

3. **Verify MongoDB is running**:
   ```bash
   # Docker Compose
   docker-compose up -d mongodb
   docker ps | grep mongodb
   
   # Or local MongoDB
   mongosh mongodb://localhost:27017
   ```

#### Problem: `com.mongodb.MongoTimeoutException: Timed out`

**Cause**: MongoDB server unreachable or slow.

**Solutions**:

1. **Check network connectivity**:
   ```bash
   ping mongodb.example.com
   nc -zv mongodb.example.com 27017
   ```

2. **Increase timeout**:
   ```yaml
   spring:
     data:
       mongodb:
         uri: mongodb://localhost:27017/spring-boot-poc?connectTimeoutMS=5000&socketTimeoutMS=5000
   ```

3. **Check firewall rules**:
   ```bash
   # Allow MongoDB port
   ufw allow 27017
   ```

#### Problem: `MongoException: duplicate key error`

**Cause**: Unique index constraint violated (e.g., duplicate email).

**Solution**:

```java
// Handle in service
try {
    customer = repository.save(customer);
} catch (MongoException e) {
    if (e.getMessage().contains("duplicate")) {
        throw new DuplicateEmailException(customer.email());
    }
    throw e;
}
```

Or drop and recreate indexes:
```bash
mongosh
use spring-boot-poc
db.customers.dropIndex("email_1")
db.customers.createIndex({ "email": 1 }, { unique: true })
```

### Application Startup Issues

#### Problem: `Failed to bind to port 8080`

**Cause**: Port already in use.

**Solutions**:

1. **Find process using port**:
   ```bash
   lsof -i :8080
   # or on Windows
   netstat -ano | findstr :8080
   ```

2. **Kill process** (macOS/Linux):
   ```bash
   kill -9 <PID>
   ```

3. **Use different port**:
   ```bash
   ./gradlew bootRun --args='--server.port=8081'
   ```

#### Problem: `No main manifest attribute`

**Cause**: JAR missing boot manifest.

**Solution**:

```bash
# Rebuild with bootJar
./gradlew clean build

# Run with proper artifact
java -jar build/libs/spring-boot-4-poc-*.jar
```

### JWT & Security Issues

#### Problem: `JWT validation failed: Token was either expired or invalid`

**Cause**: Token expired or invalid secret.

**Solutions**:

1. **Check token expiration**:
   ```bash
   # Decode JWT (online at jwt.io or use jwk-set-rsa)
   curl -X POST http://localhost:8080/api/authenticate \
     -H "Content-Type: application/json" \
     -d '{"email":"user@example.com","password":"password"}'
   ```

2. **Verify JWT secret**:
   ```bash
   export JWT_SECRET="your-secret-key"
   ```

3. **Increase token expiration**:
   ```yaml
   jwt:
     expiration: 86400  # 24 hours
   ```

#### Problem: `403 Forbidden: Insufficient permissions`

**Cause**: User lacks required role.

**Solutions**:

1. **Check user roles** (in service):
   ```java
   @GetMapping
   @PreAuthorize("hasRole('ADMIN')")
   public List<User> getUsers() { ... }
   ```

2. **Verify role assignment**:
   ```bash
   # Check user in database
   db.users.findOne({ email: "user@example.com" })
   ```

3. **Add role to user**:
   ```javascript
   // MongoDB
   db.users.updateOne(
     { email: "user@example.com" },
     { $set: { roles: ["ADMIN", "USER"] } }
   )
   ```

### Docker & Container Issues

#### Problem: `docker: command not found`

**Cause**: Docker not installed or not in PATH.

**Solution**:

1. **Install Docker**:
   ```bash
   # macOS
   brew install docker
   
   # Ubuntu
   sudo apt-get install docker.io
   ```

2. **Verify installation**:
   ```bash
   docker --version
   docker ps
   ```

#### Problem: `Container exits immediately`

**Cause**: Application crashes or misconfiguration.

**Solution**:

1. **Check logs**:
   ```bash
   docker logs <container-id>
   docker logs -f <container-name>
   ```

2. **Run interactively**:
   ```bash
   docker run -it spring-boot-poc:latest /bin/sh
   ```

3. **Verify environment variables**:
   ```bash
   docker run -e MONGODB_URI=mongodb://mongo:27017 \
     -e JWT_SECRET=secret \
     spring-boot-poc:latest
   ```

### Build Issues

#### Problem: `Error: JAVA_HOME is not set`

**Cause**: Java not installed or JAVA_HOME not configured.

**Solutions**:

1. **Install Java 25+**:
   ```bash
   # macOS
   brew install openjdk
   
   # Ubuntu
   sudo apt-get install openjdk-25-jdk
   ```

2. **Set JAVA_HOME**:
   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home)  # macOS
   export JAVA_HOME=/usr/lib/jvm/java-25-openjdk  # Ubuntu
   ```

3. **Verify**:
   ```bash
   java -version
   ```

#### Problem: `Gradle build fails: Compilation error`

**Cause**: Syntax errors or missing dependencies.

**Solutions**:

1. **Clean and rebuild**:
   ```bash
   ./gradlew clean build --refresh-dependencies
   ```

2. **Check for syntax errors**:
   ```bash
   # Rebuild shows errors
   ./gradlew build --info
   ```

3. **Update Gradle wrapper**:
   ```bash
   ./gradlew wrapper --gradle-version=8.10
   ```

### Performance Issues

#### Problem: Application slow or hanging

**Cause**: Database queries, connection pool exhaustion, or memory leak.

**Solutions**:

1. **Enable query logging**:
   ```yaml
   logging:
     level:
      org.springframework.data.mongodb: DEBUG
   ```

2. **Monitor heap memory**:
   ```bash
   jps  # List Java processes
   jmap -heap <pid>  # Show heap status
   ```

3. **Increase JVM memory**:
   ```bash
   export JAVA_OPTS="-Xms512m -Xmx1024m"
   ./gradlew bootRun
   ```

4. **Check database indexes**:
   ```javascript
   // MongoDB
   db.customers.getIndexes()
   db.orders.getIndexes()
   ```

### Test Issues

#### Problem: `Docker tests disabled - ENABLE_DOCKER_TESTS not set`

**Solution**:

```bash
# Run with Docker tests
ENABLE_DOCKER_TESTS=true ./gradlew test

# Or set in environment
export ENABLE_DOCKER_TESTS=true
./gradlew test
```

#### Problem: `Testcontainers timeout`

**Cause**: Docker slow or network issues.

**Solution**:

1. **Increase timeout**:
   ```bash
   export TESTCONTAINERS_PULL_PAUSE_BETWEEN_PULLS=1000
   ENABLE_DOCKER_TESTS=true ./gradlew test
   ```

2. **Use local MongoDB instead**:
   ```bash
   docker-compose up -d mongodb
   # Run tests normally (without ENABLE_DOCKER_TESTS)
   ./gradlew test
   ```

### API Issues

#### Problem: `400 Bad Request: Validation Failed`

**Cause**: Invalid request payload.

**Solution**: Check request body against endpoint schema:

```bash
# Example
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "+1234567890"
  }'
```

#### Problem: `500 Internal Server Error`

**Cause**: Unhandled exception in application.

**Solutions**:

1. **Check application logs**:
   ```bash
   tail -f logs/spring-boot-poc.log
   ```

2. **Enable debug logging**:
   ```yaml
   logging:
     level:
       com.example.poc: DEBUG
   ```

3. **Check stack trace** in response or logs

### CORS Issues

#### Problem: `Access to XMLHttpRequest blocked by CORS policy`

**Cause**: Frontend origin not allowed.

**Solution**:

```yaml
# application.yml
cors:
  allowed-origins: https://example.com,https://app.example.com
  allowed-methods: GET,POST,PUT,DELETE
  allowed-headers: '*'
  allow-credentials: true
```

## Debugging Tips

### Enable Debug Mode
```bash
./gradlew bootRun --debug
# Listen on port 5005 with debugger
```

### View Application Properties
```bash
# Print all resolved properties
./gradlew bootRun --info | grep spring.data.mongodb
```

### Database Inspection
```bash
# Connect to MongoDB
mongosh mongodb://localhost:27017

# Check collections
show collections

# Query data
db.customers.find()
db.orders.find()

# Check indexes
db.customers.getIndexes()
```

### Request/Response Logging
```yaml
# application.yml
logging:
  level:
    org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping: TRACE
    org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor: DEBUG
```

## Getting Help

1. **Check logs** first: `./gradlew bootRun 2>&1 | tail -100`
2. **Review error messages** carefully
3. **Search GitHub issues**: https://github.com/heandroro/spring-boot-4-poc/issues
4. **Check documentation**: See [docs/](.) directory
5. **Create issue** with:
   - Error message (full stack trace)
   - Steps to reproduce
   - Environment info (Java version, MongoDB version, OS)
   - Logs from application startup

## See Also

- [Architecture](architecture.md) - Design decisions
- [Deployment](deployment.md) - Production setup
- [MongoDB Documentation](https://docs.mongodb.com/manual/troubleshooting/)
- [Spring Boot Troubleshooting](https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc)
- [Docker Documentation](https://docs.docker.com/config/containers/logging/)
