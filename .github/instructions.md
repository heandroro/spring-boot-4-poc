# GitHub Copilot Instructions

Este arquivo fornece contexto e diretrizes para o GitHub Copilot auxiliar na implementação e revisão de código seguindo os padrões do projeto.

## 🎯 Visão Geral do Projeto

**Tipo**: Backend e-commerce/marketplace REST API  
**Stack**: Spring Boot 4 + Java 25 + MongoDB 7.0  
**Arquitetura**: Domain-Driven Design (DDD) com 4 camadas

## 📐 Arquitetura e Estrutura

### Organização de Pacotes (DDD)
```
src/main/java/com/example/poc/
├── domain/              # Entidades, Value Objects, Interfaces de Repository
├── application/         # Services, Use Cases, Business Logic
├── infrastructure/      # Implementações (MongoDB, Mappers, Configs)
└── web/                # Controllers, DTOs, Exception Handlers
```

### Regras de Dependência
- `domain/` não depende de ninguém (core puro)
- `application/` depende apenas de `domain/`
- `infrastructure/` e `web/` dependem de `domain/` e `application/`
- Controllers nunca chamam Repositories diretamente (sempre via Services)

## 💻 Padrões de Código Java 25

### 1. Records Imutáveis (NÃO usar Lombok)
```java
// ✅ CERTO - Java 25 Record
@Document(collection = "products")
public record Product(
    @Id String id,
    @Indexed String sku,
    String name,
    BigDecimal price,
    Integer stock,
    @CreatedDate LocalDateTime createdAt,
    @LastModifiedDate LocalDateTime updatedAt
) {}

// ✅ CERTO - Value Object
public record Address(
    String street,
    String number,
    String city,
    String state,
    String zipCode
) {
    public Address {
        Objects.requireNonNull(zipCode, "ZIP code is required");
    }
}

// ❌ ERRADO - Lombok
@Data
@AllArgsConstructor
public class Product { ... }
```

### 2. Pattern Matching (Java 25)
```java
// ✅ CERTO - Pattern Matching for switch
public String getOrderStatusMessage(Order order) {
    return switch (order.status()) {
        case PENDING -> "Aguardando pagamento";
        case PAID -> "Pagamento confirmado";
        case SHIPPED -> "Pedido enviado";
        case DELIVERED -> "Pedido entregue";
        case CANCELLED -> "Pedido cancelado";
    };
}

// ✅ CERTO - Pattern Matching with instanceof
public BigDecimal calculateDiscount(Discount discount) {
    return switch (discount) {
        case PercentageDiscount pd -> price.multiply(pd.percentage());
        case FixedDiscount fd -> fd.amount();
        case NoDiscount nd -> BigDecimal.ZERO;
    };
}
```

### 3. Sequenced Collections (Java 21+)
```java
// ✅ CERTO - Usar métodos de SequencedCollection
List<Order> orders = orderRepository.findByCustomerId(customerId);
Order mostRecent = orders.getFirst();  // Java 21+
Order oldest = orders.getLast();       // Java 21+
List<Order> reversed = orders.reversed(); // Java 21+

// ❌ EVITAR
Order mostRecent = orders.get(0);
Order oldest = orders.get(orders.size() - 1);
Collections.reverse(orders);
```

### 4. Text Blocks
```java
// ✅ CERTO - Query complexa com Text Block
@Query("""
    {
        'category': ?0,
        'stock': { $gt: 0 },
        'price': { $gte: ?1, $lte: ?2 }
    }
    """)
List<Product> findAvailableByCategory(String category, BigDecimal minPrice, BigDecimal maxPrice);
```

### 5. Virtual Threads (Java 21+)
```java
// ✅ CERTO - Configurar Virtual Threads no Spring Boot 4
@Configuration
public class AsyncConfig {
    
    @Bean
    public AsyncTaskExecutor applicationTaskExecutor() {
        TaskExecutorAdapter adapter = new TaskExecutorAdapter(
            Executors.newVirtualThreadPerTaskExecutor()
        );
        return adapter;
    }
}
```

## 🌱 Spring Boot 4 - Novos Recursos

### 1. RestClient (substitui RestTemplate)
```java
// ✅ CERTO - RestClient (Spring Boot 4)
@Service
public class PaymentService {
    private final RestClient restClient;
    
    public PaymentService(RestClient.Builder builder) {
        this.restClient = builder
            .baseUrl("https://api.stripe.com")
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build();
    }
    
    public PaymentResponse charge(ChargeRequest request) {
        return restClient.post()
            .uri("/v1/charges")
            .body(request)
            .retrieve()
            .body(PaymentResponse.class);
    }
}

// ❌ EVITAR - RestTemplate (deprecated)
RestTemplate restTemplate = new RestTemplate();
```

### 2. Problem Details (RFC 7807)
```java
// ✅ CERTO - Problem Details para erros
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleNotFound(ProductNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problemDetail.setTitle("Product Not Found");
        problemDetail.setProperty("sku", ex.getSku());
        return problemDetail;
    }
}
```

### 3. Observability (Micrometer)
```java
// ✅ CERTO - Instrumentação com Observation API
@Service
public class OrderService {
    private final ObservationRegistry observationRegistry;
    
    @Observed(name = "order.checkout", contextualName = "checkout-order")
    public Order checkout(CheckoutRequest request) {
        return Observation.createNotStarted("order.checkout", observationRegistry)
            .observe(() -> {
                // Lógica de checkout
                return processOrder(request);
            });
    }
}
```

### 4. Constructor Injection (Obrigatório)
```java
// ✅ CERTO - Constructor Injection
@Service
public class ProductService {
    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final StockService stockService;
    
    public ProductService(
        ProductRepository repository,
        ProductMapper mapper,
        StockService stockService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.stockService = stockService;
    }
}

// ❌ ERRADO - Field Injection
@Service
public class ProductService {
    @Autowired
    private ProductRepository repository;
}
```

## 🗄️ MongoDB Best Practices

### 1. Embedding vs Referencing
```java
// ✅ CERTO - Embedding (dependência forte)
@Document(collection = "customers")
public record Customer(
    @Id String id,
    String name,
    String email,
    Address address,        // Embedded - Address não existe sozinho
    Preferences preferences  // Embedded
) {}

// ✅ CERTO - Referencing (agregados independentes)
@Document(collection = "orders")
public record Order(
    @Id String id,
    String customerId,      // Reference - Customer existe independentemente
    List<OrderItem> items,
    BigDecimal totalAmount,
    OrderStatus status
) {}
```

### 2. Snapshots para Dados Históricos
```java
// ✅ CERTO - Snapshot de preço no momento da compra
@Document(collection = "orders")
public record Order(
    @Id String id,
    String customerId,
    List<OrderItem> items,
    BigDecimal totalAtPurchase,  // Snapshot - preço pode mudar depois
    LocalDateTime orderDate
) {}

public record OrderItem(
    String productId,
    ProductSnapshot product,     // Snapshot completo do produto
    Integer quantity,
    BigDecimal pricePerUnit      // Preço no momento da compra
) {}

public record ProductSnapshot(String id, String sku, String name) {}
```

### 3. Indexes Estratégicos
```java
// ✅ CERTO - Indexes em campos pesquisados
@Document(collection = "products")
public record Product(
    @Id String id,
    
    @Indexed(unique = true)
    String sku,              // Busca por SKU
    
    @Indexed
    String name,             // Busca por nome
    
    @Indexed
    String category,         // Filtro por categoria
    
    BigDecimal price,
    Integer stock
) {}

// ✅ CERTO - Compound Index para queries frequentes
@Document(collection = "products")
@CompoundIndex(name = "category_stock_idx", def = "{'category': 1, 'stock': -1}")
public record Product(...) {}
```

### 4. TTL para Dados Temporários
```java
// ✅ CERTO - Carrinho expira após 7 dias
@Document(collection = "carts")
public record Cart(
    @Id String id,
    String customerId,
    List<CartItem> items,
    
    @Indexed(expireAfter = "7d")
    LocalDateTime lastModifiedAt
) {}
```

### 5. Aggregation Pipelines
```java
// ✅ CERTO - Aggregation para cálculos complexos
public interface ProductRepository extends MongoRepository<Product, String> {
    
    @Aggregation(pipeline = {
        "{ $match: { 'category': ?0 } }",
        "{ $group: { _id: '$category', avgPrice: { $avg: '$price' }, count: { $sum: 1 } } }"
    })
    List<CategoryStats> getCategoryStatistics(String category);
}
```

## 🧪 Testes - Padrões

### 1. JUnit 6 + Instancio
```java
// ✅ CERTO - Teste unitário com Instancio
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService - Create Product")
class ProductServiceTest {
    
    @Mock private ProductRepository repository;
    @Mock private ProductMapper mapper;
    @InjectMocks private ProductService service;
    
    @Test
    @DisplayName("Should create product successfully when SKU is unique")
    void shouldCreateProductSuccessfully() {
        // Arrange - Instancio gera fixtures
        var request = Instancio.of(CreateProductRequest.class)
            .set(field(CreateProductRequest::sku), "PROD-001")
            .set(field(CreateProductRequest::price), new BigDecimal("99.90"))
            .create();
        
        var product = Instancio.of(Product.class)
            .set(field(Product::id), "123")
            .set(field(Product::sku), request.sku())
            .create();
        
        when(repository.existsBySku(request.sku())).thenReturn(false);
        when(repository.save(any(Product.class))).thenReturn(product);
        
        // Act
        var result = service.create(request);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("123");
        assertThat(result.sku()).isEqualTo("PROD-001");
        
        verify(repository).existsBySku(request.sku());
        verify(repository).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Should throw DuplicateSkuException when SKU already exists")
    void shouldThrowExceptionWhenSkuExists() {
        var request = Instancio.create(CreateProductRequest.class);
        
        when(repository.existsBySku(request.sku())).thenReturn(true);
        
        assertThatThrownBy(() -> service.create(request))
            .isInstanceOf(DuplicateSkuException.class)
            .hasMessageContaining("SKU already exists");
    }
}
```

### 2. Testcontainers para Integração
```java
// ✅ CERTO - Teste de integração com MongoDB real
@DataMongoTest
@EnabledIfEnvironmentVariable(named = "ENABLE_DOCKER_TESTS", matches = "true")
@DisplayName("ProductRepository - MongoDB Integration")
class ProductRepositoryTest {
    
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");
    
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
    
    @Autowired
    private ProductRepository repository;
    
    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }
    
    @Test
    @DisplayName("Should find product by SKU")
    void shouldFindBySku() {
        var product = Instancio.of(Product.class)
            .ignore(field(Product::id))
            .set(field(Product::sku), "TEST-SKU")
            .create();
        
        repository.save(product);
        
        var result = repository.findBySku("TEST-SKU");
        
        assertThat(result).isPresent();
        assertThat(result.get().sku()).isEqualTo("TEST-SKU");
    }
}
```

## 🔒 Segurança

### 1. RBAC com @PreAuthorize
```java
// ✅ CERTO - Controle de acesso por role
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @GetMapping
    public Page<ProductDto> findAll(Pageable pageable) {
        // Público - sem autenticação
    }
    
    @GetMapping("/{id}")
    public ProductDto findById(@PathVariable String id) {
        // Público
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ProductDto> create(@Valid @RequestBody CreateProductRequest request) {
        // Apenas MANAGER e ADMIN
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ProductDto update(@PathVariable String id, @Valid @RequestBody UpdateProductRequest request) {
        // Apenas MANAGER e ADMIN
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        // Apenas ADMIN
    }
}
```

### 2. Validação de Autorização no Service Layer
```java
// ✅ CERTO - Validar autorização também no service
@Service
public class OrderService {
    
    public Order findById(String orderId, String requestingUserId) {
        var order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        // Validar que o usuário pode ver este pedido
        if (!order.customerId().equals(requestingUserId) && !isAdmin(requestingUserId)) {
            throw new ForbiddenException("You don't have permission to view this order");
        }
        
        return order;
    }
}
```

### 3. Senhas com BCrypt
```java
// ✅ CERTO - Sempre hashear senhas
@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    
    public Customer register(RegisterRequest request) {
        var hashedPassword = passwordEncoder.encode(request.password());
        
        var customer = new Customer(
            null,
            request.name(),
            request.email(),
            hashedPassword,  // ✅ Hashed
            "ROLE_CUSTOMER"
        );
        
        return customerRepository.save(customer);
    }
}

// ❌ ERRADO - Senha em texto plano
customerRepository.save(new Customer(..., request.password(), ...));
```

## 🌐 REST API - Convenções

### 1. Naming e HTTP Methods
```java
// ✅ CERTO - RESTful endpoints
GET    /api/products              → findAll()
GET    /api/products/{id}         → findById()
POST   /api/products              → create()
PUT    /api/products/{id}         → update()
PATCH  /api/products/{id}         → partialUpdate()
DELETE /api/products/{id}         → delete()

GET    /api/products/{id}/reviews → findReviewsByProductId()
POST   /api/products/{id}/reviews → addReview()
```

### 2. Status HTTP Corretos
```java
// ✅ CERTO - Status apropriados
@PostMapping
public ResponseEntity<ProductDto> create(@Valid @RequestBody CreateProductRequest request) {
    var product = service.create(request);
    return ResponseEntity
        .status(HttpStatus.CREATED)  // 201
        .body(mapper.toDto(product));
}

@PutMapping("/{id}")
public ProductDto update(@PathVariable String id, @Valid @RequestBody UpdateProductRequest request) {
    return service.update(id, request);  // 200 OK
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable String id) {
    service.delete(id);
    return ResponseEntity.noContent().build();  // 204 No Content
}

@GetMapping("/{id}")
public ProductDto findById(@PathVariable String id) {
    return service.findById(id);  // 200 OK ou 404 Not Found (exception)
}
```

### 3. Paginação
```java
// ✅ CERTO - Suporte a paginação
@GetMapping
public Page<ProductDto> findAll(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(defaultValue = "name") String sort,
    @RequestParam(required = false) String category
) {
    var pageable = PageRequest.of(page, size, Sort.by(sort));
    return service.findAll(category, pageable);
}
```

### 4. Validação com Bean Validation
```java
// ✅ CERTO - DTO com validações
public record CreateProductRequest(
    @NotBlank(message = "SKU is required")
    @Size(min = 3, max = 20, message = "SKU must be between 3 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers and hyphens")
    String sku,
    
    @NotBlank(message = "Name is required")
    @Size(max = 200)
    String name,
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    BigDecimal price,
    
    @NotNull
    @Min(value = 0, message = "Stock cannot be negative")
    Integer stock,
    
    @NotBlank
    String category
) {}
```

## 🧹 Clean Code

### 1. Nomes Descritivos
```java
// ✅ CERTO
public Order checkout(CheckoutRequest request) { ... }
public boolean isProductAvailable(String productId) { ... }
public BigDecimal calculateTotalWithDiscount(Order order, Discount discount) { ... }

// ❌ ERRADO
public Order process(CheckoutRequest req) { ... }
public boolean check(String id) { ... }
public BigDecimal calc(Order o, Discount d) { ... }
```

### 2. Métodos Pequenos e Focados
```java
// ✅ CERTO - Cada método faz uma coisa
@Service
public class OrderService {
    
    public Order checkout(CheckoutRequest request) {
        validateStock(request.items());
        var order = createOrder(request);
        decreaseStock(request.items());
        sendConfirmationEmail(order);
        return order;
    }
    
    private void validateStock(List<OrderItemRequest> items) {
        items.forEach(item -> {
            var product = productRepository.findById(item.productId())
                .orElseThrow(() -> new ProductNotFoundException(item.productId()));
            
            if (product.stock() < item.quantity()) {
                throw new InsufficientStockException(product.sku());
            }
        });
    }
    
    private Order createOrder(CheckoutRequest request) {
        // Lógica de criação
    }
    
    private void decreaseStock(List<OrderItemRequest> items) {
        // Lógica de estoque
    }
    
    private void sendConfirmationEmail(Order order) {
        // Lógica de email
    }
}
```

### 3. Evitar Comentários Óbvios
```java
// ❌ ERRADO
// Salva o produto
repository.save(product);

// ✅ CERTO - Código auto-explicativo
var savedProduct = repository.save(product);

// ✅ CERTO - Comentário útil
// Snapshot do produto é necessário porque o preço pode mudar após a compra
var productSnapshot = new ProductSnapshot(product.id(), product.sku(), product.name());
```

## 📋 Checklist de Code Review

Ao revisar código, verificar:

### ✅ Arquitetura
- [ ] Código na camada correta (domain/application/infrastructure/web)
- [ ] Controllers não têm lógica de negócio
- [ ] Services não chamam outros services desnecessariamente
- [ ] Repositories apenas em infrastructure

### ✅ Java 25
- [ ] Usa Records ao invés de classes/Lombok
- [ ] Usa Pattern Matching quando aplicável
- [ ] Usa Text Blocks para strings multi-linha
- [ ] Usa `getFirst()`/`getLast()` ao invés de `get(0)`/`get(size-1)`

### ✅ Spring Boot 4
- [ ] Constructor Injection (não @Autowired)
- [ ] RestClient ao invés de RestTemplate
- [ ] ProblemDetail para respostas de erro
- [ ] @Observed para métricas críticas

### ✅ MongoDB
- [ ] @Document e @Indexed apropriados
- [ ] Embedding/Referencing correto
- [ ] Snapshots para dados históricos
- [ ] TTL para dados temporários
- [ ] Queries otimizadas (projeções se necessário)

### ✅ Testes
- [ ] Cobertura mínima 80%
- [ ] Usa Instancio para fixtures
- [ ] @DisplayName descritivo
- [ ] Testa cenários de sucesso E falha
- [ ] Integração com Testcontainers quando necessário

### ✅ Segurança
- [ ] @PreAuthorize nos endpoints protegidos
- [ ] Validação de autorização no service layer
- [ ] Senhas hasheadas com BCrypt
- [ ] Dados sensíveis não expostos nos DTOs

### ✅ API REST
- [ ] Endpoints RESTful
- [ ] Status HTTP corretos
- [ ] Paginação em listas
- [ ] Validação Bean Validation nos DTOs
- [ ] Retorna DTOs (não entidades)

### ✅ Clean Code
- [ ] Nomes descritivos
- [ ] Métodos pequenos (< 20 linhas idealmente)
- [ ] Sem código comentado
- [ ] Sem magic numbers
- [ ] Tratamento de exceções apropriado

## 🔗 Referências

- [Plan de Implementação](../plan/README.md)
- [CONTRIBUTING.md](../CONTRIBUTING.md)
- [Pull Request Template](PULL_REQUEST_TEMPLATE.md)
- [Java 25 Release Notes](https://openjdk.org/projects/jdk/25/)
- [Spring Boot 4 Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
