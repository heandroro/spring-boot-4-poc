# Testing

## Testing Strategy

This project uses a comprehensive testing approach covering unit, integration, and component tests.

- **Framework**: JUnit 6 (Jupiter)
- **Mocking**: Mockito
- **Fixtures**: Instancio (random test data generation)
- **Integration Tests**: Testcontainers (MongoDB, Docker)
- **Coverage**: Aim for minimum 90% (ideal 95%) on domain/application layers

## Unit Tests

Test domain logic without external dependencies.

### Domain Entity Tests

```java
@DisplayName("Order Entity")
class OrderTest {
    
    private Order order;
    private Product product;
    
    @BeforeEach
    void setUp() {
        order = new Order();
        product = new Product("product-1", "Widget", BigDecimal.valueOf(29.99));
    }
    
    @DisplayName("should add item to empty order")
    @Test
    void addItemToEmptyOrder() {
        // When
        order.addItem(product, 2);
        
        // Then
        assertThat(order.items()).hasSize(1);
        assertThat(order.totalAmount()).isEqualTo(BigDecimal.valueOf(59.98));
    }
    
    @DisplayName("should merge items with same product")
    @Test
    void mergeItemsWithSameProduct() {
        // When
        order.addItem(product, 2);
        order.addItem(product, 3);
        
        // Then
        assertThat(order.items()).hasSize(1);
        assertThat(order.items().get(0).quantity()).isEqualTo(5);
    }
    
    @DisplayName("should reject invalid quantity")
    @Test
    void rejectInvalidQuantity() {
        // When/Then
        assertThatThrownBy(() -> order.addItem(product, -1))
            .isInstanceOf(InvalidQuantityException.class);
    }
    
    @DisplayName("should transition to valid state")
    @Test
    void transitionToValidState() {
        // Given
        order.setStatus(OrderStatus.PENDING);
        
        // When
        order.transitionTo(OrderStatus.CONFIRMED);
        
        // Then
        assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
    }
    
    @DisplayName("should reject invalid state transition")
    @Test
    void rejectInvalidStateTransition() {
        // Given
        order.setStatus(OrderStatus.SHIPPED);
        
        // When/Then
        assertThatThrownBy(() -> order.transitionTo(OrderStatus.PENDING))
            .isInstanceOf(InvalidStateTransitionException.class);
    }
}
```

### Use Case Tests

```java
@DisplayName("CreateCustomerUseCase")
class CreateCustomerUseCaseTest {
    
    private CreateCustomerUseCase useCase;
    private CustomerRepository repositoryMock;
    private CustomerMapper mapperMock;
    
    @BeforeEach
    void setUp() {
        repositoryMock = mock(CustomerRepository.class);
        mapperMock = mock(CustomerMapper.class);
        useCase = new CreateCustomerUseCase(repositoryMock, mapperMock);
    }
    
    @DisplayName("should create customer with valid data")
    @Test
    void createCustomerWithValidData() {
        // Given
        var request = new CreateCustomerRequest("John Doe", "john@example.com");
        var customer = Instancio.create(Customer.class);
        var response = Instancio.create(CustomerResponse.class);
        
        when(mapperMock.toDomain(request)).thenReturn(customer);
        when(repositoryMock.save(customer)).thenReturn(customer);
        when(mapperMock.toResponse(customer)).thenReturn(response);
        
        // When
        CustomerResponse result = useCase.execute(request);
        
        // Then
        assertThat(result).isEqualTo(response);
        verify(repositoryMock).save(customer);
        verify(mapperMock).toDomain(request);
        verify(mapperMock).toResponse(customer);
    }
    
    @DisplayName("should reject duplicate email")
    @Test
    void rejectDuplicateEmail() {
        // Given
        var request = new CreateCustomerRequest("John Doe", "john@example.com");
        var customer = Instancio.create(Customer.class);
        
        when(mapperMock.toDomain(request)).thenReturn(customer);
        when(repositoryMock.save(customer))
            .thenThrow(new DuplicateEmailException("john@example.com"));
        
        // When/Then
        assertThatThrownBy(() -> useCase.execute(request))
            .isInstanceOf(DuplicateEmailException.class);
    }
}
```

## Integration Tests

Test interactions with MongoDB using Testcontainers.

### Repository Integration Tests

```java
@DisplayName("CustomerRepository Integration Tests")
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ENABLE_DOCKER_TESTS", matches = "true")
class CustomerRepositoryIT {
    
    @Autowired
    private CustomerRepository repository;
    
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));
    
    @DynamicPropertySource
```

Note: the Gradle `integrationTest` task defaults to `ENABLE_DOCKER_TESTS=true` when you run it locally unless you explicitly set the environment variable. This lets you run integration tests with `./gradlew integrationTest` without manually exporting the environment variable. If you need to override, set the variable explicitly:

```bash
# Run integration tests (Docker required)
./gradlew integrationTest

# Explicit override
ENABLE_DOCKER_TESTS=false ./gradlew integrationTest
```
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
    
    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }
    
    @DisplayName("should save and retrieve customer")
    @Test
    void saveAndRetrieveCustomer() {
        // Given
        var customer = Instancio.of(Customer.class)
            .ignore(Select.field(Customer::id))
            .create();
        
        // When
        Customer saved = repository.save(customer);
        Optional<Customer> found = repository.findById(saved.id());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().email()).isEqualTo(customer.email());
    }
    
    @DisplayName("should find customer by email")
    @Test
    void findByEmail() {
        // Given
        var customer = Instancio.create(Customer.class);
        repository.save(customer);
        
        // When
        Optional<Customer> found = repository.findByEmail(customer.email());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(customer);
    }
    
    @DisplayName("should enforce unique email constraint")
    @Test
    void enforceUniqueEmail() {
        // Given
        var customer1 = Instancio.of(Customer.class)
            .set(Select.field(Customer::email), "john@example.com")
            .create();
        var customer2 = Instancio.of(Customer.class)
            .set(Select.field(Customer::email), "john@example.com")
            .create();
        
        repository.save(customer1);
        
        // When/Then
        assertThatThrownBy(() -> repository.save(customer2))
            .isInstanceOf(MongoException.class);
    }
}
```

### Use Case Integration Tests

```java
@DisplayName("CreateCustomerUseCase Integration Tests")
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ENABLE_DOCKER_TESTS", matches = "true")
class CreateCustomerUseCaseIT {
    
    @Autowired
    private CreateCustomerUseCase useCase;
    
    @Autowired
    private CustomerRepository repository;
    
    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", 
            new MongoDBContainer(DockerImageName.parse("mongo:7.0"))::getReplicaSetUrl);
    }
    
    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }
    
    @DisplayName("should create customer end-to-end")
    @Test
    void createCustomerEndToEnd() {
        // Given
        var request = new CreateCustomerRequest("Jane Doe", "jane@example.com");
        
        // When
        CustomerResponse response = useCase.execute(request);
        
        // Then
        assertThat(response.name()).isEqualTo("Jane Doe");
        assertThat(response.email()).isEqualTo("jane@example.com");
        
        // Verify persisted
        Optional<Customer> saved = repository.findByEmail("jane@example.com");
        assertThat(saved).isPresent();
    }
}
```

## Controller Tests

Test REST endpoints with MockMvc.

```java
@DisplayName("CustomerController")
@WebMvcTest(CustomerController.class)
class CustomerControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CreateCustomerUseCase createCustomerUseCase;
    
    @MockBean
    private GetCustomerUseCase getCustomerUseCase;
    
    @DisplayName("should create customer with 201 status")
    @Test
    void createCustomer() throws Exception {
        // Given
        var request = new CreateCustomerRequest("John Doe", "john@example.com");
        var response = Instancio.create(CustomerResponse.class);
        
        when(createCustomerUseCase.execute(request)).thenReturn(response);
        
        // When/Then
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "John Doe",
                        "email": "john@example.com"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(response.id()))
            .andExpect(jsonPath("$.name").value("John Doe"));
    }
    
    @DisplayName("should return 404 for non-existent customer")
    @Test
    void getNotFoundCustomer() throws Exception {
        // Given
        when(getCustomerUseCase.execute("invalid-id"))
            .thenThrow(new CustomerNotFoundException("invalid-id"));
        
        // When/Then
        mockMvc.perform(get("/api/customers/invalid-id")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Customer Not Found"));
    }
}
```

## Test Fixtures with Instancio

Generate random test data:

```java
// Simple fixture
Customer customer = Instancio.create(Customer.class);

// With customizations
Customer customCustomer = Instancio.of(Customer.class)
    .set(Select.field(Customer::email), "john@example.com")
    .set(Select.field(Customer::name), "John Doe")
    .ignore(Select.field(Customer::id))  // Will use default/null
    .create();

// Generate multiple with list
List<Customer> customers = Instancio.createList(Customer.class, 10);

// Customize entire list
List<Customer> customList = Instancio.ofList(Customer.class)
    .size(5)
    .map(Select.field(Customer::email), (ignored, customer) -> 
        "user" + customer.id() + "@example.com")
    .create();
```

## Parametrized Tests

Test multiple scenarios:

```java
@DisplayName("Order State Transitions")
class OrderStateTransitionTest {
    
    @ParameterizedTest(name = "from {0} to {1} should {2}")
    @CsvSource({
        "PENDING, CONFIRMED, succeed",
        "CONFIRMED, SHIPPED, succeed",
        "SHIPPED, PENDING, fail",
        "CANCELLED, PENDING, fail",
    })
    @DisplayName("validate state transitions")
    void testStateTransitions(String from, String to, String expected) {
        // Given
        var order = new Order();
        order.setStatus(OrderStatus.valueOf(from));
        
        // When/Then
        if ("succeed".equals(expected)) {
            assertThatCode(() -> order.transitionTo(OrderStatus.valueOf(to)))
                .doesNotThrowAnyException();
        } else {
            assertThatThrownBy(() -> order.transitionTo(OrderStatus.valueOf(to)))
                .isInstanceOf(InvalidStateTransitionException.class);
        }
    }
}
```

## Test Coverage

Run coverage reports:

```bash
./gradlew test --coverage
# Reports in: build/reports/coverage/index.html
```

Target coverage by layer:
- **Domain**: 90%+ (critical business logic)
- **Application**: 85%+ (use cases)
- **Infrastructure**: 70%+ (technical implementation)
- **Web**: minimum 90% (ideal 95%) (controllers)

## Best Practices

1. **Test One Thing**: Each test verifies single behavior
2. **Use Descriptive Names**: `@DisplayName("should add item to empty order")`
3. **Follow AAA Pattern**: Arrange → Act → Assert
4. **Mock External Dependencies**: Database, HTTP, external services
5. **Use Instancio for Fixtures**: Reduces test boilerplate
6. **Test Happy Path First**: Then test error cases
7. **Clean Up Resources**: Reset mocks/repositories in `@BeforeEach`
8. **Test Boundaries**: Validate input, error responses
9. **Use Parameterized Tests**: For multiple scenarios
10. **Avoid Test Interdependencies**: Tests should be independent

## Running Tests

```bash
# All tests
./gradlew test

# With Docker tests
ENABLE_DOCKER_TESTS=true ./gradlew test

# Specific test class
./gradlew test --tests CustomerRepositoryTest

# With coverage
./gradlew test --coverage

# Watch mode (continuous)
./gradlew test --continuous
```

## See Also

- [Architecture](architecture.md) - Testing patterns
- [MongoDB](mongodb.md) - Testcontainers setup
- [JUnit 5 Documentation](https://junit.org/junit5/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Instancio Documentation](https://www.instancio.org/)
