# Architecture

## Overview

This project follows **Domain-Driven Design (DDD)** principles with a layered architecture approach. The structure separates concerns into distinct layers: Domain, Application, Infrastructure, and Web.

```
src/main/java/com/example/poc/
├── domain/                    # Core business logic (entities, aggregates, repositories)
├── application/               # Use cases (orchestration, business rules)
├── infrastructure/            # Technical implementation (MongoDB, security, mapping)
└── web/                       # REST API (controllers, DTOs, exception handling)
```

## Layers

### Domain Layer (`domain/`)
**Responsibility**: Pure business logic, independent of frameworks.

- **Entities**: Core business objects with unique identities
- **Aggregates**: Groups of entities that act as consistency boundaries
- **Value Objects**: Immutable objects representing domain concepts
- **Repositories**: Abstractions for data persistence
- **Domain Services**: Business logic requiring multiple aggregates

**Key Principle**: No external dependencies (frameworks, databases, HTTP).

### Application Layer (`application/`)
**Responsibility**: Orchestrate domain objects and implement use cases.

- **Use Cases**: Implement user interactions (e.g., `CreateCustomerUseCase`)
- **DTOs**: Data Transfer Objects for input/output
- **Application Services**: Coordinate domain services and repositories
- **Mapping**: Convert between domain entities and DTOs

**Key Principle**: Thin layer that wires domain logic without duplicating it.

### Infrastructure Layer (`infrastructure/`)
**Responsibility**: Technical implementation details.

- **MongoDB Integration**: Spring Data repositories, document mapping
- **Security**: JWT, authentication, authorization (Spring Security)
- **Configuration**: Spring Boot configuration, beans
- **Mapping**: Entity/DTO conversion using MapStruct or manual mapping
- **HTTP Clients**: REST client integration

**Key Principle**: Implements interfaces defined in domain/application layers.

### Web Layer (`web/`)
**Responsibility**: HTTP API and user interaction.

- **Controllers**: REST endpoints
- **Request/Response DTOs**: API contracts
- **Global Exception Handler**: Consistent error responses (RFC 7807)
- **Validation**: Bean Validation annotations on DTOs

**Key Principle**: Thin layer that exposes application layer functionality.

## Design Decisions

### 1. Immutable DTOs and Entities
Use Java 25 **Records** instead of Lombok for immutable data structures:

```java
// ✅ CORRECT: Record as DTO
public record CreateCustomerRequest(
    @NotBlank String name,
    @Email String email
) {}

// ✅ CORRECT: Record as Entity
public record Customer(
    @Id String id,
    String name,
    String email,
    LocalDateTime createdAt
) {}
```

### 2. Constructor Injection
Always use constructor injection, never field injection:

```java
// ✅ CORRECT
@Service
public class CreateCustomerUseCase {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;
    
    public CreateCustomerUseCase(CustomerRepository repository, CustomerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
}

// ❌ INCORRECT
@Service
public class CreateCustomerUseCase {
    @Autowired private CustomerRepository repository;  // Field injection
}
```

### 3. Spring Boot RestClient
Use `RestClient` instead of deprecated `RestTemplate`:

```java
// ✅ CORRECT
@Service
public class ExternalApiService {
    private final RestClient restClient;
    
    public ExternalApiService(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://api.example.com").build();
    }
    
    public ExternalDataDto fetchData(String id) {
        return restClient.get()
            .uri("/data/{id}", id)
            .retrieve()
            .body(ExternalDataDto.class);
    }
}
```

### 4. MongoDB Document Design
Use Spring Data MongoDB annotations appropriately:

```java
// ✅ CORRECT
@Document(collection = "customers")
public record Customer(
    @Id String id,
    @Indexed(unique = true) String email,
    String name,
    Address address,  // Embedded value object
    LocalDateTime createdAt
) {}

// Embedded value object
public record Address(
    String street,
    String city,
    String zipCode,
    String country
) {}
```

### 5. Rich Domain Model
Implement behavior in entities (rich model), not services (anemic model):

```java
// ✅ CORRECT: Rich entity with behavior
@Document(collection = "orders")
public class Order {
    @Id private String id;
    private String customerId;
    private List<OrderItem> items;
    private OrderStatus status;
    
    public void addItem(Product product, int quantity) {
        // Business rule: validate quantity
        if (quantity <= 0) throw new InvalidQuantityException();
        
        // Add or merge item
        items.stream()
            .filter(item -> item.productId().equals(product.id()))
            .findFirst()
            .ifPresentOrElse(
                item -> item.increaseQuantity(quantity),
                () -> items.add(new OrderItem(product.id(), quantity))
            );
    }
    
    public void removeItem(String productId) {
        items.removeIf(item -> item.productId().equals(productId));
    }
    
    public void transitionTo(OrderStatus newStatus) {
        // Business rule: validate state transitions
        if (!status.canTransitionTo(newStatus)) {
            throw new InvalidStateTransitionException(status, newStatus);
        }
        this.status = newStatus;
    }
}

// ❌ INCORRECT: Anemic entity (data only)
@Document(collection = "orders")
public class Order {
    @Id private String id;
    private String customerId;
    private List<OrderItem> items;
    private OrderStatus status;
    
    // Only getters/setters, no logic!
}

// ❌ INCORRECT: Logic moved to service (anti-pattern)
@Service
public class OrderService {
    public void addItem(Order order, Product product, int quantity) {
        // Business logic scattered in service
        order.getItems().add(new OrderItem(product.id(), quantity));
    }
}
```

### 6. Dependency Injection Limits
Keep use cases focused with limited dependencies (1-3 ideal):

```java
// ✅ CORRECT: 2 dependencies (good)
@Service
public class CreateCustomerUseCase {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;
    
    public CreateCustomerUseCase(CustomerRepository repository, CustomerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    
    public CustomerResponse execute(CreateCustomerRequest request) {
        var customer = mapper.toDomain(request);
        var saved = repository.save(customer);
        return mapper.toResponse(saved);
    }
}

// ❌ INCORRECT: 8 dependencies (refactor needed)
@Service
public class ComplexUseCase {
    private final Repository1 repo1;
    private final Repository2 repo2;
    private final Service1 service1;
    private final Service2 service2;
    private final Service3 service3;
    private final Validator validator;
    private final Mapper mapper;
    private final Logger logger;
    // ...
}
```

See [plan/code-examples.md](../plan/code-examples.md) Section 21 for refactoring strategies when exceeding limits.

## Data Flow

### Create Customer Flow
```
1. HTTP Request → CustomerController
2. Validate CreateCustomerRequest (Bean Validation)
3. CustomerController → CreateCustomerUseCase
4. CreateCustomerUseCase → CustomerMapper (convert DTO to entity)
5. CreateCustomerUseCase → CustomerRepository.save(customer)
6. MongoDB stores document in "customers" collection
7. Repository returns saved Customer entity
8. CreateCustomerUseCase → CustomerMapper (convert entity to response DTO)
9. CustomerController → HTTP Response 201 Created
```

### Query Customer Flow
```
1. HTTP Request → CustomerController.getById(id)
2. CustomerController → GetCustomerUseCase
3. GetCustomerUseCase → CustomerRepository.findById(id)
4. MongoDB query on "customers" collection
5. Repository returns Optional<Customer>
6. UseCase → CustomerMapper (convert to DTO)
7. CustomerController → HTTP Response 200 OK
```

## Dependency Diagram

```
Web Layer (Controllers)
    ↓ (uses)
Application Layer (UseCases, DTOs)
    ↓ (uses)
Domain Layer (Entities, Repositories, Domain Services)
    ↓ (depends on interfaces)
Infrastructure Layer (MongoDB, Security, HTTP)
```

## Best Practices

1. **Domain Logic First**: Start with domain entities and business rules
2. **Test Domains First**: Unit test domain layer without dependencies
3. **Minimal Application Layer**: Keep use cases thin and focused
4. **Infrastructure Abstraction**: Use repository interfaces to isolate from MongoDB
5. **Rich Models**: Encapsulate behavior in entities, not services
6. **Immutability**: Use Records for DTOs and value objects
7. **Validation**: Apply constraints at boundaries (API layer)
8. **Exception Handling**: Custom domain exceptions for business errors

## See Also

- [Security](security.md) - JWT, authorization, CORS
- [Testing](testing.md) - Unit tests, integration tests, fixtures
- [Java Records Best Practices](java-records-best-practices.md) - Records, Jackson, and validation patterns
- [plan/code-examples.md](../plan/code-examples.md) - Comprehensive code examples and patterns
