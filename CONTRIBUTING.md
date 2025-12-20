# Guia de Contribuição - Spring Boot 4 POC

## 📋 Índice
- [Padrões de Arquitetura](#padrões-de-arquitetura)
- [Convenções de Código](#convenções-de-código)
- [Testes](#testes)
- [MongoDB](#mongodb)
- [Segurança](#segurança)
- [API REST](#api-rest)
- [Git e Commits](#git-e-commits)
- [Code Review](#code-review)

## 🏗️ Padrões de Arquitetura

### Domain-Driven Design (DDD)
O projeto segue DDD com 4 camadas:

```
src/main/java/com/example/poc/
├── domain/              # Entidades, Value Objects, Repositories (interfaces)
├── application/         # Services, Use Cases, Regras de Negócio
├── infrastructure/      # Implementações (MongoDB, Mapping, Configs)
└── web/                # Controllers, DTOs, Exception Handlers
```

### ✅ CERTO
```java
// domain/Product.java - Entidade de domínio
@Document(collection = "products")
public record Product(
    @Id String id,
    @Indexed String sku,
    String name,
    BigDecimal price,
    Integer stock
) {}

// domain/ProductRepository.java - Interface no domínio
public interface ProductRepository extends MongoRepository<Product, String> {
    Optional<Product> findBySku(String sku);
}

// application/ProductService.java - Lógica de negócio
@Service
public class ProductService {
    private final ProductRepository repository;
    
    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }
    
    public Product create(CreateProductRequest request) {
        // Lógica aqui
    }
}

// web/ProductController.java - Endpoint REST
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;
    
    public ProductController(ProductService service) {
        this.service = service;
    }
    
    @PostMapping
    public ResponseEntity<ProductDto> create(@Valid @RequestBody CreateProductRequest request) {
        var product = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(product));
    }
}
```

### ❌ ERRADO
```java
// Controller chamando Repository diretamente
@RestController
public class ProductController {
    @Autowired
    private ProductRepository repository; // ❌ Pula o Service Layer
    
    @PostMapping("/products")
    public Product create(@RequestBody Product product) { // ❌ Retorna entidade
        return repository.save(product); // ❌ Sem validação
    }
}
```

## 💻 Convenções de Código

### 1. Java Records (NÃO usar Lombok)
**Java 25 nativo**, sem Lombok no projeto.

#### ✅ CERTO - Record Imutável
```java
public record Customer(
    @Id String id,
    String name,
    String email,
    Address address
) {}

public record Address(String street, String city, String zipCode) {}
```

#### ❌ ERRADO - Lombok
```java
@Data // ❌ NÃO USAR
@AllArgsConstructor
public class Customer {
    private String id;
    private String name;
}
```

#### 📝 @JsonProperty em Records
**Jackson mapeia automaticamente** os componentes de records para JSON. Anotações `@JsonProperty` são **redundantes** quando os nomes coincidem.

```java
// ❌ ERRADO - Anotações redundantes
public record CustomerDto(
    @JsonProperty("id") String id,        // ❌ Redundante
    @JsonProperty("name") String name,    // ❌ Redundante
    @JsonProperty("email") String email   // ❌ Redundante
) {}

// ✅ CERTO - Limpo e simples
public record CustomerDto(
    String id,
    String name,
    String email
) {}

// ✅ CERTO - @JsonProperty apenas quando nomes diferem
public record CustomerDto(
    String id,
    String name,
    @JsonProperty("e-mail") String email  // ✅ JSON usa "e-mail"
) {}
```

Ver [docs/java-records-best-practices.md](docs/java-records-best-practices.md) para detalhes.

### 2. Injeção de Dependências

#### ✅ CERTO - Constructor Injection
```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final EmailService emailService;
    
    // Constructor Injection - recomendado
    public OrderService(
        OrderRepository orderRepository,
        PaymentService paymentService,
        EmailService emailService
    ) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
        this.emailService = emailService;
    }
}
```

#### ❌ ERRADO - Field Injection
```java
@Service
public class OrderService {
    @Autowired // ❌ NÃO USAR
    private OrderRepository orderRepository;
    
    @Autowired
    private PaymentService paymentService;
}
```

### 3. Validações

#### ✅ CERTO - Bean Validation nos DTOs
```java
public record CreateProductRequest(
    @NotBlank(message = "SKU is required")
    @Size(min = 3, max = 20)
    String sku,
    
    @NotBlank(message = "Name is required")
    String name,
    
    @NotNull
    @DecimalMin(value = "0.01")
    BigDecimal price,
    
    @NotNull
    @Min(0)
    Integer stock
) {}
```

### 4. Imports (sem nomes qualificados)

Evite usar nomes de classe totalmente qualificados no corpo do código. Sempre adicione `import` e utilize o nome simples da classe.

#### ✅ CERTO - Usando imports
```java
import com.example.poc.domain.Customer;
import com.example.poc.web.dto.CustomerDto;

public class CustomerService {
    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public CustomerDto get(String id) {
        Customer customer = repository.findById(id).orElseThrow();
        return mapper.toDto(customer);
    }
}
```

#### ❌ ERRADO - Nome totalmente qualificado
```java
public class CustomerService {
    public CustomerDto get(String id) {
        Customer customer = repo.findById(id).orElseThrow();
        return mapper.toDto(customer);
    }
}
```

Diretriz: Nunca use nomes totalmente qualificados em classes, métodos ou variáveis. Prefira organizar imports. Esta regra é aplicada automaticamente por Checkstyle (`config/checkstyle/checkstyle.xml`) e pela verificação automática do PR (`./gradlew check`) — PRs que introduzirem FQNs serão bloqueados até serem corrigidos. Em casos raros de conflito de nomes, **evite** trazer ambas as classes para o mesmo escopo (extraia chamadas para métodos auxiliares/fachadas) de modo a manter o código sem FQNs.

### 5. Exceções Customizadas

```java
// domain/exceptions/ProductNotFoundException.java
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String sku) {
        super("Product not found with SKU: " + sku);
    }
}

// web/GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ProductNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }
}
```

## 🧪 Testes

### Stack de Testes
- **JUnit 6 (Jupiter 5.11+)** - Framework
- **Mockito** - Mocks e Stubs
- **Instancio** - Geração de dados de teste
- **Testcontainers** - MongoDB real em testes de integração
- **AssertJ** - Assertions fluentes

### 1. Testes Unitários

#### ✅ CERTO - Service com Mocks
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService - Business Logic")
class ProductServiceTest {
    
    @Mock
    private ProductRepository repository;
    
    @Mock
    private ProductMapper mapper;
    
    @InjectMocks
    private ProductService service;
    
    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProduct() {
        // Arrange - Usar Instancio para fixtures
        var request = Instancio.of(CreateProductRequest.class)
            .set(field(CreateProductRequest::sku), "PROD-001")
            .create();
        
        var product = Instancio.of(Product.class)
            .set(field(Product::id), "123")
            .create();
        
        when(repository.existsBySku(request.sku())).thenReturn(false);
        when(repository.save(any(Product.class))).thenReturn(product);
        
        // Act
        var result = service.create(request);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("123");
        verify(repository).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Should throw exception when SKU already exists")
    void shouldThrowExceptionWhenSkuExists() {
        var request = Instancio.create(CreateProductRequest.class);
        
        when(repository.existsBySku(request.sku())).thenReturn(true);
        
        assertThatThrownBy(() -> service.create(request))
            .isInstanceOf(DuplicateSkuException.class)
            .hasMessageContaining("SKU already exists");
    }
}
```

### 2. Testes de Integração

#### ✅ CERTO - Repository com Testcontainers
```java
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
    
    @Test
    @DisplayName("Should find product by SKU")
    void shouldFindBySku() {
        // Arrange
        var product = Instancio.of(Product.class)
            .ignore(field(Product::id))
            .set(field(Product::sku), "TEST-SKU")
            .create();
        
        repository.save(product);
        
        // Act
        var result = repository.findBySku("TEST-SKU");
        
        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().sku()).isEqualTo("TEST-SKU");
    }
}
```

### 3. Testes de Controller

```java
@WebMvcTest(ProductController.class)
@DisplayName("ProductController - REST API")
class ProductControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ProductService service;
    
    @Test
    @DisplayName("POST /api/products should return 201 Created")
    void shouldCreateProduct() throws Exception {
        var request = Instancio.create(CreateProductRequest.class);
        var product = Instancio.create(Product.class);
        
        when(service.create(any())).thenReturn(product);
        
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(product.id()));
    }
}
```

### Executar Testes

```bash
# Testes unitários (sem Docker)
./gradlew test

# Testes de integração (com Testcontainers)
ENABLE_DOCKER_TESTS=true ./gradlew test

# Com cobertura
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## 🗄️ MongoDB

### 1. Entidades

#### ✅ CERTO - Configuração Completa
```java
@Document(collection = "products")
public record Product(
    @Id String id,
    
    @Indexed(unique = true)
    String sku,
    
    @Indexed
    String name,
    
    BigDecimal price,
    Integer stock,
    
    @Indexed
    String category,
    
    @CreatedDate LocalDateTime createdAt,
    @LastModifiedDate LocalDateTime updatedAt
) {}
```

### 2. Repositories

```java
public interface ProductRepository extends MongoRepository<Product, String> {
    
    Optional<Product> findBySku(String sku);
    
    boolean existsBySku(String sku);
    
    @Query("{ 'category': ?0, 'stock': { $gt: 0 } }")
    List<Product> findAvailableByCategory(String category);
    
    // Paginação
    Page<Product> findByCategory(String category, Pageable pageable);
}
```

### 3. Embedding vs Referencing

#### Embedding (Dependência Forte)
```java
@Document(collection = "customers")
public record Customer(
    @Id String id,
    String name,
    String email,
    Address address,  // ✅ Embedded - Address não existe sozinho
    Preferences preferences  // ✅ Embedded
) {}

public record Address(String street, String city, String zipCode) {}
```

#### Referencing (Agregados Independentes)
```java
@Document(collection = "orders")
public record Order(
    @Id String id,
    String customerId,  // ✅ Reference - Customer existe independentemente
    List<OrderItem> items,
    OrderStatus status
) {}
```

### 4. Snapshots para Dados Históricos

```java
@Document(collection = "orders")
public record Order(
    @Id String id,
    String customerId,
    
    // Snapshot do preço no momento da compra
    ProductSnapshot product,
    BigDecimal priceAtPurchase,  // ✅ Preço pode mudar depois
    
    LocalDateTime orderDate
) {}

public record ProductSnapshot(String id, String name, String sku) {}
```

### 5. TTL para Dados Temporários

```java
@Document(collection = "carts")
public record Cart(
    @Id String id,
    String customerId,
    List<CartItem> items,
    
    @Indexed(expireAfter = "7d")  // ✅ Expira após 7 dias
    LocalDateTime lastModifiedAt
) {}
```

### 6. Transações

```java
@Service
public class OrderService {
    private final MongoTemplate mongoTemplate;
    
    @Transactional
    public Order checkout(CheckoutRequest request) {
        // Reduz estoque
        productRepository.decreaseStock(request.productId(), request.quantity());
        
        // Cria pedido
        var order = orderRepository.save(new Order(...));
        
        // Remove carrinho
        cartRepository.deleteByCustomerId(request.customerId());
        
        return order;
    }
}
```

## 🔒 Segurança

### 1. RBAC (Role-Based Access Control)

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @GetMapping
    public List<ProductDto> findAll() {
        // Público - sem auth
    }
    
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductDto> create(@Valid @RequestBody CreateProductRequest request) {
        // Apenas MANAGER e ADMIN
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        // Apenas ADMIN
    }
}
```

### 2. Senha com BCrypt

```java
@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    
    public Customer register(RegisterRequest request) {
        var hashedPassword = passwordEncoder.encode(request.password());  // ✅ BCrypt
        
        return customerRepository.save(new Customer(
            null,
            request.email(),
            hashedPassword,  // ✅ Nunca armazena texto plano
            "ROLE_CUSTOMER"
        ));
    }
}
```

### 3. JWT

```java
@Service
public class JwtService {
    
    public String generateToken(Customer customer) {
        return Jwts.builder()
            .setSubject(customer.email())
            .claim("role", customer.role())
            .setIssuedAt(new Date())
            .setExpiration(Date.from(Instant.now().plus(24, ChronoUnit.HOURS)))
            .signWith(getSigningKey())
            .compact();
    }
}
```

### 4. Não Expor Dados Sensíveis

```java
// ✅ CERTO - DTO sem senha
public record CustomerDto(
    String id,
    String name,
    String email
    // ❌ NÃO incluir password
) {}
```

## 🌐 API REST

### 1. Convenções de Endpoints

| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| GET | `/api/products` | Listar todos | 200 OK |
| GET | `/api/products/{id}` | Buscar por ID | 200 OK / 404 Not Found |
| POST | `/api/products` | Criar | 201 Created |
| PUT | `/api/products/{id}` | Atualizar | 200 OK / 404 Not Found |
| DELETE | `/api/products/{id}` | Deletar | 204 No Content |

### 2. Paginação

```java
@GetMapping
public Page<ProductDto> findAll(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(defaultValue = "name") String sort
) {
    var pageable = PageRequest.of(page, size, Sort.by(sort));
    return productService.findAll(pageable);
}
```

### 3. Status HTTP Corretos

```java
@PostMapping
public ResponseEntity<ProductDto> create(@Valid @RequestBody CreateProductRequest request) {
    var product = service.create(request);
    return ResponseEntity
        .status(HttpStatus.CREATED)  // ✅ 201
        .body(mapper.toDto(product));
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable String id) {
    service.delete(id);
    return ResponseEntity.noContent().build();  // ✅ 204
}
```

## 📝 Git e Commits

### Conventional Commits

```bash
# Features
git commit -m "feat: add product search by category"

# Fixes
git commit -m "fix: correct stock validation in checkout"

# Docs
git commit -m "docs: update README with MongoDB setup"

# Refactor
git commit -m "refactor: extract payment logic to separate service"

# Tests
git commit -m "test: add integration tests for order repository"

# Chore
git commit -m "chore: update dependencies"
```

## 👀 Code Review

### Checklist do Reviewer

#### ✅ Arquitetura
- [ ] Código segue DDD (domain/application/infrastructure/web)
- [ ] Separação de responsabilidades clara
- [ ] Services não chamam outros services desnecessariamente
- [ ] Controllers não têm lógica de negócio

#### ✅ Código
- [ ] Usa Records (não Lombok)
- [ ] Constructor injection (não @Autowired em fields)
- [ ] Validações com Bean Validation
- [ ] Exceções customizadas apropriadas
- [ ] Código legível e bem nomeado
- [ ] Não usa nomes de classe totalmente qualificados (usa imports)
- [ ] SpotBugs: supressões (`@SuppressFBWarnings`) só devem ser usadas localmente com justificativa clara na anotação e documentadas em `docs/spotbugs.md`; evite supressões globais ou exclusões amplas.

#### ✅ Testes
- [ ] Cobertura mínima de 90% (ideal 95%)
- [ ] Testa cenários de sucesso e falha
- [ ] Usa Instancio para fixtures
- [ ] **Naming:** Integration tests must use the `IT` suffix (example: `CustomerRepositoryIT`). Tests annotated with `@SpringBootTest`, `@Testcontainers`, `@DataMongoTest`, or `@EnabledIfEnvironmentVariable` are considered integration tests and should follow this convention.
- [ ] Mocks apropriados
- [ ] Testes de integração com Testcontainers

#### ✅ MongoDB
- [ ] @Document e @Indexed corretos
- [ ] Embedding/Referencing apropriado
- [ ] Snapshots para dados históricos
- [ ] TTL para dados temporários
- [ ] Queries otimizadas

#### ✅ Segurança
- [ ] @PreAuthorize nos endpoints protegidos
- [ ] Senhas hasheadas com BCrypt
- [ ] JWT validado
- [ ] Dados sensíveis não expostos

#### ✅ API
- [ ] Endpoints RESTful
- [ ] Status HTTP corretos
- [ ] Paginação para listas
- [ ] DTOs (não entidades) nos responses

## 🔗 Links Úteis

- [Plan de Implementação](plan/README.md)
- [Resumo Executivo](plan/executive-summary.md)
- [Diagramas de Arquitetura](plan/architecture-diagrams.md)
- [Especificação de Entidades](plan/1-entities.md)
- [SpotBugs: documentação e política](docs/spotbugs.md)

## 📚 Comandos Úteis

```bash
# Build
./gradlew build

# Testes
./gradlew test

# Testes com Docker
ENABLE_DOCKER_TESTS=true ./gradlew test

# Rodar aplicação
./gradlew bootRun

# Limpar build
./gradlew clean

# Verificar dependências
./gradlew dependencies

# Formatar código
./gradlew spotlessApply
```
