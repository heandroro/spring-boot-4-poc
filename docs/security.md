# Security

## Authentication & Authorization

### JWT-based Authentication
The application uses JWT (JSON Web Tokens) for stateless authentication.

#### Token Structure
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Tokens contain:
- `sub`: User identifier (email or username)
- `roles`: Assigned roles
- `iat`: Issue timestamp
- `exp`: Expiration timestamp (typically 1 hour)
- Custom claims as needed

#### Token Generation
Tokens are generated during login and returned to the client:

```java
@PostMapping("/authenticate")
public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
    // Validate credentials
    User user = authenticateUser(request.email(), request.password());
    
    // Generate JWT token
    String token = jwtProvider.generateToken(user);
    
    return ResponseEntity.ok(new AuthResponse(token, user.roles()));
}
```

#### Token Validation
Tokens are validated on protected endpoints via `JwtAuthenticationFilter`:

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain chain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token != null && jwtProvider.validateToken(token)) {
                String userId = jwtProvider.getUserIdFromToken(token);
                Set<String> roles = jwtProvider.getRolesFromToken(token);
                
                var authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, mapRoles(roles));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException e) {
            logger.warn("JWT validation failed: {}", e.getMessage());
        }
        
        chain.doFilter(request, response);
    }
}
```

### Role-Based Access Control

Use `@PreAuthorize` annotation on controller methods:

```java
@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    
    // Anyone authenticated can view customers
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<CustomerResponse>> listAll() {
        // ...
    }
    
    // Only ADMIN can create customers
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> create(@RequestBody CreateCustomerRequest request) {
        // ...
    }
    
    // Only ADMIN can delete
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        // ...
    }
}
```

## CORS (Cross-Origin Resource Sharing)

Configure CORS to allow frontend applications:

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("https://example.com", "http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }
}
```

## Password Security

### Hashing & Storage
Never store plaintext passwords. Use `bcrypt` via Spring Security:

```java
// ✅ CORRECT: Hash passwords
@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    
    public UserService(PasswordEncoder passwordEncoder, UserRepository repository) {
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
    }
    
    public User createUser(String email, String plainPassword) {
        String hashedPassword = passwordEncoder.encode(plainPassword);
        return repository.save(new User(email, hashedPassword));
    }
}

// ❌ INCORRECT: Plaintext password
public User createUser(String email, String password) {
    return repository.save(new User(email, password));  // Security risk!
}
```

Configure `PasswordEncoder` bean:

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);  // Cost factor 12
    }
}
```

## Secrets Management

### Environment Variables
Store sensitive data in environment variables, not code:

```yaml
# ❌ INCORRECT: In application.yml
jwt:
  secret: super-secret-key-hardcoded
  
# ✅ CORRECT: Use environment variable
jwt:
  secret: ${JWT_SECRET}
```

Set via environment:
```bash
export JWT_SECRET="your-production-secret-here"
export MONGODB_URI="mongodb+srv://user:pass@cluster.mongodb.net/dbname"
export DATABASE_PASSWORD="${DATABASE_PASSWORD}"
```

For Docker:
```bash
docker run -e JWT_SECRET=secret -e MONGODB_URI=mongodb://... app
```

### Sensitive Data in Logs
Never log passwords, tokens, or credentials:

```java
// ❌ INCORRECT: Logs sensitive data
logger.info("User login: email={}, password={}", email, password);

// ✅ CORRECT: Only log non-sensitive info
logger.info("User login attempt: email={}", email);

// ✅ CORRECT: Use masks for tokens
String maskedToken = token.substring(0, 10) + "...";
logger.debug("Token received: {}", maskedToken);
```

## Input Validation

### Bean Validation
Apply constraints to DTOs and entities:

```java
public record CreateCustomerRequest(
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    String name,
    
    @NotBlank
    @Email(message = "Invalid email format")
    String email,
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", 
             message = "Invalid phone number")
    String phone
) {}

@Document(collection = "customers")
public record Customer(
    @Id String id,
    
    @NotBlank
    @Indexed(unique = true)
    String email,
    
    @NotBlank
    String name,
    
    LocalDateTime createdAt
) {}
```

### Controller Validation
Trigger validation on request binding:

```java
@PostMapping
public ResponseEntity<CustomerResponse> create(
    @Valid @RequestBody CreateCustomerRequest request) {  // @Valid triggers validation
    // If validation fails, returns 400 Bad Request with error details
    return ResponseEntity.status(201).body(customerService.create(request));
}
```

## SQL Injection Prevention

### MongoDB (Document-based)
Use parameterized queries via Spring Data:

```java
// ✅ CORRECT: Spring Data prevents injection
@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    Customer findByEmail(String email);
    List<Customer> findByNameContainingIgnoreCase(String name);
}

// ❌ INCORRECT: Raw query string (vulnerable)
@Query("{ 'email': '" + email + "' }")  // String concatenation - UNSAFE
Customer findByRawQuery(String email);
```

Use `@Query` with parameters:

```java
@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    @Query("{ 'email': ?0 }")
    Customer findByEmailSafe(String email);
}
```

## XSS (Cross-Site Scripting) Prevention

### Response DTOs
Ensure DTOs are serialized safely by Jackson:

```java
public record CustomerResponse(
    String id,
    String name,  // Will be automatically escaped by Jackson
    String email
) {}
```

Jackson automatically escapes HTML/JavaScript in JSON responses by default.

### Content Security Policy
Configure CSP headers (if serving HTML/templates):

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.headers()
            .contentSecurityPolicy("default-src 'self'; script-src 'self'");
        return http.build();
    }
}
```

## Error Handling

### No Sensitive Details in Errors
Return generic error messages to clients:

```java
// ✅ CORRECT: Generic message
@ExceptionHandler(EntityNotFoundException.class)
public ResponseEntity<ProblemDetail> handleNotFound(EntityNotFoundException ex) {
    var problemDetail = ProblemDetail.forStatus(404);
    problemDetail.setTitle("Resource Not Found");
    problemDetail.setDetail("The requested resource does not exist");
    
    logger.info("Resource not found: {}", ex.getMessage());  // Log details internally
    
    return ResponseEntity.status(404).body(problemDetail);
}

// ❌ INCORRECT: Exposes internal details
@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, String>> handleError(Exception ex) {
    var error = Map.of(
        "error", ex.getMessage(),  // Exposes stack trace details
        "class", ex.getClass().getName()
    );
    return ResponseEntity.status(500).body(error);
}
```

## HTTPS/TLS

### Production
Always use HTTPS in production:

```yaml
# application.yml
server:
  port: 8443
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

### Development
Use self-signed certificate for local testing:

```bash
keytool -genkeypair -alias tomcat -keyalg RSA -keysize 2048 \
  -keystore keystore.p12 -validity 365 -storepass password \
  -storetype PKCS12
```

## Security Headers

Configure security headers for all responses:

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .headers()
                .contentSecurityPolicy("default-src 'self'")
                .and()
                .xssProtection()
                .and()
                .cacheControl()
                .and()
                .frameOptions().deny();
        
        return http.build();
    }
}
```

## Audit Logging

Log security-relevant events:

```java
@Service
public class UserService {
    
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");
    
    public User authenticate(String email, String password) {
        try {
            User user = validateCredentials(email, password);
            AUDIT_LOG.info("User authenticated: email={}, timestamp={}", email, LocalDateTime.now());
            return user;
        } catch (InvalidCredentialsException e) {
            AUDIT_LOG.warn("Failed authentication attempt: email={}, timestamp={}", 
                          email, LocalDateTime.now());
            throw e;
        }
    }
}
```

## See Also

- [Authentication & Authorization Best Practices](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
