# Exemplos Praticos de Boas Praticas

Colecao enxuta de exemplos aplicando os padroes definidos no projeto.

🚫 **Lombok nao permitido:** Evite qualquer anotacao Lombok (`@Data`, `@Builder`, `@Getter`, `@Setter`, `@Value`, `@AllArgsConstructor`, `@NoArgsConstructor`). Prefira Records, construtores explicitos e MapStruct para mapeamento.

## 📋 Índice

1. [Entidade de Dominio (MongoDB)](#entidade-de-dominio-mongodb)
2. [DTO com Bean Validation](#dto-com-bean-validation)
3. [Mapper com MapStruct](#mapper-com-mapstruct)
4. [Service com Regras e Constructor Injection](#service-com-regras-e-constructor-injection)
5. [Controller REST com @PreAuthorize e Status Corretos](#controller-rest-com-preauthorize-e-status-corretos)
6. [Tratamento de Erros com ProblemDetail](#tratamento-de-erros-com-problemdetail)
7. [Teste Unitario com JUnit 6, Mockito e Instancio](#teste-unitario-com-junit-6-mockito-e-instancio)
8. [Teste de Repository com Testcontainers](#teste-de-repository-com-testcontainers)
9. [Evitando IFs Desnecessarios](#evitando-ifs-desnecessarios)
   - 9.1 [Validacoes com Bean Validation](#1-validacoes-com-bean-validation-ao-inves-de-ifs-manuais)
   - 9.2 [Optional ao inves de IF null](#2-optional-ao-inves-de-if-null)
   - 9.3 [Query MongoDB ao inves de IF em memoria](#3-query-mongodb-ao-inves-de-if-em-memoria)
   - 9.4 [Expressoes ternarias](#4-expressoes-ternarias-simples-ao-inves-de-ifelse)
   - 9.5 [Guard Clauses](#5-guard-clauses-em-vez-de-if-aninhados)
   - 9.6 [Spring Data features](#6-spring-data-features-ao-inves-de-if-manualmente)
10. [Java 25 Features - Explore Melhor](#java-25-features---explore-melhor)
    - 10.1 [Pattern Matching em Switch](#1-pattern-matching-em-switch-record-patterns)
    - 10.2 [Sequenced Collections](#2-sequenced-collections---getfirst-e-getlast)
    - 10.3 [Text Blocks](#3-text-blocks-para-queries-mongodb-e-sql)
    - 10.4 [Sealed Classes](#4-sealed-classes---controle-de-hierarquia)
    - 10.5 [Records com Validacao Compacta](#5-records-com-validacao-compacta)
    - 10.6 [Virtual Threads](#6-virtual-threads-preludio)
11. [Spring Boot 4 Features - Explore Melhor](#spring-boot-4-features---explore-melhor)
    - 11.1 [RestClient](#1-restclient-substitui-resttemplate)
    - 11.2 [ProblemDetail (RFC 7807)](#2-problemdetail-rfc-7807)
    - 11.3 [@Observed (Micrometer)](#3-observability-com-observed-micrometer)
    - 11.4 [ConfigurationProperties Typed](#4-configurationproperties-typed)
12. [Helpers e Utility Classes - Quando e Como Usar](#helpers-e-utility-classes---quando-e-como-usar)
    - 12.1 [Converters/Parsers](#1-convertersparsers---transformacoes-de-dados)
    - 12.2 [Validators](#2-validators---logica-de-validacao-complexa)
    - 12.3 [Formatters](#3-formatters---formatacao-de-saida)
    - 12.4 [Calculators](#4-calculators---logica-de-calculo-pura)
    - 12.5 [Collection Utilities](#5-collection-utilities---operacoes-em-colecoes)
13. [Design Patterns para IFs Complexos](#design-patterns-para-ifs-complexos)
    - 13.1 [Strategy Pattern](#1-strategy-pattern---multiplos-comportamentos)
    - 13.2 [State Pattern](#2-state-pattern---mudancas-de-estado)
    - 13.3 [Builder Pattern](#3-builder-pattern---construcao-complexa)
    - 13.4 [Chain of Responsibility](#4-chain-of-responsibility---pipeline-de-validacoes)
    - 13.5 [Factory Pattern](#5-factory-pattern---criacao-baseada-em-tipo)
    - 13.6 [Decorator Pattern](#6-decorator-pattern---adicionar-comportamentos)
14. [Constantes vs Enums - Boas Praticas](#constantes-vs-enums---boas-praticas)
    - 14.1 [Quando Usar Enums](#quando-usar-enums)
    - 14.2 [Quando Usar Constantes](#quando-usar-constantes)
    - 14.3 [Padroes de Enum Avancado](#padroes-de-enum-avancado)

---

## Entidade de Dominio (MongoDB)
```java
@Document(collection = "products")
public record Product(
    @Id String id,
    @Indexed(unique = true) String sku,
    @Indexed String name,
    BigDecimal price,
    Integer stock,
    ProductSnapshot snapshot,
    @CreatedDate LocalDateTime createdAt,
    @LastModifiedDate LocalDateTime updatedAt
) {}

public record ProductSnapshot(String description, BigDecimal originalPrice) {}
```

Evitar:
```java
@Document
@Data // Lombok nao permitido
public class Product {
    @Id ObjectId id; // nao exponha ObjectId
    String sku; // sem indice
    String name;
}
```

## DTO com Bean Validation
```java
public record CreateProductRequest(
    @NotBlank(message = "SKU obrigatorio") String sku,
    @NotBlank(message = "Nome obrigatorio") String name,
    @NotNull @DecimalMin(value = "0.01") BigDecimal price,
    @NotNull @Min(0) Integer stock
) {}
```

Evitar:
```java
public record CreateProductRequest(String sku, String name, BigDecimal price, Integer stock) {}
// Sem Bean Validation -> abre brechas de dados invalidos
```

## Mapper com MapStruct
```java
@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toEntity(CreateProductRequest request);
    ProductResponse toResponse(Product product);
}
```

Evitar:
```java
public class ProductMapperManual {
    // Mapeamento manual aumenta risco de esquecer campos e nao aplica convencoes
    public Product toEntity(CreateProductRequest request) {
        var p = new Product(null, request.sku(), request.name(), request.price(), request.stock(), null, null, null);
        return p;
    }
}
```

## Service com Regras e Constructor Injection
```java
@Service
public class ProductService {
    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductService(ProductRepository repository, ProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public ProductResponse create(CreateProductRequest request) {
        if (repository.existsBySku(request.sku())) {
            throw new DuplicateSkuException(request.sku());
        }
        var entity = mapper.toEntity(request);
        var saved = repository.save(entity);
        return mapper.toResponse(saved);
    }
}
```

Evitar:
```java
@Service
public class ProductServiceBad {
    @Autowired private ProductRepository repository; // field injection

    public Product create(CreateProductRequest request) {
        // sem regra de negocio e sem mapper
        return repository.save(new Product(...));
    }
}
```

## Controller REST com @PreAuthorize e Status Corretos
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        var response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

Evitar:
```java
@RestController
public class ProductControllerBad {
    @PostMapping("/products")
    public Product create(@RequestBody Product product) { // retorna entidade e sem validacao
        return repository.save(product); // pula service e regras
    }
}
```

## Tratamento de Erros com ProblemDetail
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateSkuException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateSku(DuplicateSkuException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        problem.setTitle("SKU duplicado");
        problem.setDetail(ex.getMessage());
        return ResponseEntity.unprocessableEntity().body(problem);
    }
}
```

Evitar:
```java
@RestControllerAdvice
public class GlobalExceptionHandlerBad {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception ex) {
        return ResponseEntity.status(500).body("Erro generico"); // sem ProblemDetail e mistura erros
    }
}
```

## Teste Unitario com JUnit 6, Mockito e Instancio
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService - Criar produto")
class ProductServiceTest {

    @Mock private ProductRepository repository;
    @Mock private ProductMapper mapper;
    @InjectMocks private ProductService service;

    @Test
    @DisplayName("Deve criar produto quando SKU eh unico")
    void shouldCreateProduct() {
        var request = Instancio.create(CreateProductRequest.class);
        var entity = Instancio.create(Product.class);
        var saved = Instancio.create(Product.class);
        var response = Instancio.create(ProductResponse.class);

        when(repository.existsBySku(request.sku())).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        var result = service.create(request);

        assertThat(result).isEqualTo(response);
        verify(repository).save(entity);
    }
}
```

Evitar:
```java
@SpringBootTest // pesado para unit
class ProductServiceSlowTest {
    @Autowired ProductService service; // teste de integracao involuntario
}
```

## Teste de Repository com Testcontainers
```java
@DataMongoTest
@EnabledIfEnvironmentVariable(named = "ENABLE_DOCKER_TESTS", matches = "true")
@DisplayName("ProductRepository - MongoDB")
class ProductRepositoryTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private ProductRepository repository;

    @Test
    @DisplayName("Deve encontrar por SKU")
    void shouldFindBySku() {
        var product = Instancio.of(Product.class)
            .set(field(Product::id), null)
            .set(field(Product::sku), "TEST-SKU")
            .create();

        repository.save(product);

        var result = repository.findBySku("TEST-SKU");

        assertThat(result).isPresent();
        assertThat(result.get().sku()).isEqualTo("TEST-SKU");
    }
}
```

## Evitando IFs Desnecessarios

### 1. Validacoes com Bean Validation ao inves de IFs manuais

✅ CERTO:
```java
public record CreateProductRequest(
    @NotBlank String sku,
    @NotNull @DecimalMin("0.01") BigDecimal price
) {}

@PostMapping
public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
}
// Validacao eh feita automaticamente, sem IFs
```

❌ ERRADO:
```java
public record CreateProductRequest(String sku, BigDecimal price) {}

@PostMapping
public ResponseEntity<?> create(@RequestBody CreateProductRequest request) {
    if (request.sku() == null || request.sku().isBlank()) { // IF desnecessario
        return ResponseEntity.badRequest().body("SKU required");
    }
    if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) <= 0) { // IF desnecessario
        return ResponseEntity.badRequest().body("Price invalid");
    }
    return ResponseEntity.ok(service.create(request));
}
```

### 2. Optional ao inves de IF null

✅ CERTO:
```java
public Optional<ProductResponse> findBySku(String sku) {
    return repository.findBySku(sku).map(mapper::toResponse);
}

// No service/controller:
var response = service.findBySku(sku)
    .orElseThrow(() -> new ProductNotFoundException(sku));
```

❌ ERRADO:
```java
public ProductResponse findBySku(String sku) {
    var product = repository.findBySku(sku);
    if (product == null) { // IF desnecessario
        throw new ProductNotFoundException(sku);
    }
    return mapper.toResponse(product);
}
```

### 3. Query MongoDB ao inves de IF em memoria

✅ CERTO:
```java
public interface ProductRepository extends MongoRepository<Product, String> {
    @Query("{ 'price': { $gte: ?0, $lte: ?1 }, 'stock': { $gt: 0 } }")
    List<Product> findAvailableInPriceRange(BigDecimal min, BigDecimal max);
}

var products = repository.findAvailableInPriceRange(min, max);
// Filtragem no banco, sem IFs
```

❌ ERRADO:
```java
var allProducts = repository.findAll();
var filtered = allProducts.stream()
    .filter(p -> {
        if (p.getPrice().compareTo(min) >= 0 && p.getPrice().compareTo(max) <= 0) { // IF desnecessario
            if (p.getStock() > 0) { // IF desnecessario
                return true;
            }
        }
        return false;
    })
    .collect(toList());
```

### 4. Expressoes ternarias simples ao inves de IF/ELSE

✅ CERTO:
```java
var status = order.isPaid() ? "CONFIRMED" : "PENDING";
var discountedPrice = customer.isPremium() ? price.multiply(PREMIUM_DISCOUNT) : price;
```

❌ ERRADO:
```java
String status;
if (order.isPaid()) { // IF desnecessario para logica simples
    status = "CONFIRMED";
} else {
    status = "PENDING";
}
```

### 5. Guard Clauses em vez de IF aninhados

✅ CERTO:
```java
public ProductResponse create(CreateProductRequest request) {
    if (repository.existsBySku(request.sku())) {
        throw new DuplicateSkuException(request.sku());
    }
    if (!isAuthorized(getCurrentUser())) {
        throw new UnauthorizedException();
    }
    // Logica principal aqui - nao aninhada
    return saveProduct(request);
}
```

❌ ERRADO:
```java
public ProductResponse create(CreateProductRequest request) {
    if (!repository.existsBySku(request.sku())) { // IF aninhado
        if (isAuthorized(getCurrentUser())) {
            // Logica principal aqui - muito aninhada
            return saveProduct(request);
        } else {
            throw new UnauthorizedException();
        }
    } else {
        throw new DuplicateSkuException(request.sku());
    }
}
```

### 6. Spring Data features ao inves de IF manualmente

✅ CERTO:
```java
public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByCategory(String category);
    boolean existsBySku(String sku);
    void deleteByCategory(String category);
}
// Metodos gerados automaticamente, sem IF
```

❌ ERRADO:
```java
public List<Product> findByCategory(String category) {
    var all = repository.findAll();
    var filtered = new ArrayList<Product>();
    for (var product : all) {
        if (product.getCategory().equals(category)) { // IF desnecessario
            filtered.add(product);
        }
    }
    return filtered;
}
```

## Java 25 Features - Explore Melhor

### 1. Pattern Matching em Switch (Record Patterns)

✅ CERTO:
```java
public OrderStatus processOrder(Order order) {
    return switch (order) {
        case Order(var id, var items, var customer, var total) when items.isEmpty() ->
            throw new EmptyOrderException();
        
        case Order(var id, var items, var customer, var total) when total.compareTo(HIGH_VALUE_THRESHOLD) > 0 ->
            OrderStatus.PENDING_APPROVAL;
        
        case Order(var id, var items, var customer, _) when customer.isPremium() ->
            OrderStatus.AUTO_CONFIRMED;
        
        case Order(_, _, _, _) ->
            OrderStatus.PENDING_PAYMENT;
    };
}

// Pattern matching tambem funciona com instanceof
if (entity instanceof Product(var id, @Indexed var sku, var name)) {
    log.info("Product {} with SKU: {}", id, sku);
}
```

❌ ERRADO:
```java
public OrderStatus processOrder(Order order) {
    if (order.getItems().isEmpty()) { // IF tradicional
        throw new EmptyOrderException();
    }
    
    if (order.getTotal().compareTo(HIGH_VALUE_THRESHOLD) > 0) {
        return OrderStatus.PENDING_APPROVAL;
    }
    
    if (order.getCustomer().isPremium()) {
        return OrderStatus.AUTO_CONFIRMED;
    }
    
    return OrderStatus.PENDING_PAYMENT;
}
```

### 2. Sequenced Collections - getFirst() e getLast()

✅ CERTO:
```java
public record Order(
    String id,
    List<OrderItem> items,
    Customer customer,
    BigDecimal total
) {
    public OrderItem getFirstItem() {
        return items.getFirst(); // Novo em Java 21+
    }

    public OrderItem getLastItem() {
        return items.getLast();
    }

    public boolean hasMultipleItems() {
        return items.size() > 1;
    }
}

// Tambem funciona com outros tipos sequenciados
var firstProduct = products.getFirst();
var lastCustomer = customers.getLast();
var firstEntry = sortedMap.sequencedEntrySet().getFirst();
```

❌ ERRADO:
```java
public OrderItem getFirstItem() {
    return items.get(0); // Index explícito e perigoso
}

public OrderItem getLastItem() {
    return items.get(items.size() - 1); // Off-by-one risk
}
```

### 3. Text Blocks para Queries MongoDB e SQL

✅ CERTO:
```java
public interface ProductRepository extends MongoRepository<Product, String> {
    @Query("""
        {
            'category': ?0,
            'stock': { $gt: 0 },
            'price': { $gte: ?1, $lte: ?2 }
        }
        """)
    List<Product> findAvailableInPriceRange(String category, BigDecimal min, BigDecimal max);
}

// Text blocks tambem em strings comuns
String errorTemplate = """
    Validacao falhou:
    - Campo: %s
    - Valor: %s
    - Mensagem: %s
    """;
```

❌ ERRADO:
```java
@Query("{ 'category': ?0, 'stock': { $gt: 0 }, 'price': { $gte: ?1, $lte: ?2 } }")
List<Product> findAvailableInPriceRange(String category, BigDecimal min, BigDecimal max);
// Difícil de ler em uma linha
```

### 4. Sealed Classes - Controle de Hierarquia

✅ CERTO:
```java
// Apenas Order pode estender OrderState (segurança)
public sealed interface OrderState permits PendingState, ConfirmedState, ShippedState {}

public final class PendingState implements OrderState {
    public void confirm() {
        // Apenas tipos conhecidos podem implementar
    }
}

public final class ConfirmedState implements OrderState {
    public void ship() { }
}

public final class ShippedState implements OrderState { }

// Combinado com pattern matching:
String status = switch (orderState) {
    case PendingState ps -> "Aguardando confirmacao";
    case ConfirmedState cs -> "Confirmado";
    case ShippedState ss -> "Enviado";
};
```

❌ ERRADO:
```java
public interface OrderState { } // Qualquer um pode implementar
// Risco de implementacoes inesperadas

class UnknownState implements OrderState { } // Surprise!
```

### 5. Records com Validacao Compacta

✅ CERTO:
```java
public record CreateProductRequest(
    @NotBlank String sku,
    @NotBlank String name,
    @NotNull @DecimalMin("0.01") BigDecimal price,
    @NotNull @Min(0) Integer stock
) {
    // Compact constructor - validacoes sem repetir parametros
    public CreateProductRequest {
        Objects.requireNonNull(sku, "SKU cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        
        if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        
        if (stock != null && stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
    }
}
```

❌ ERRADO:
```java
public class CreateProductRequest {
    private String sku;
    private String name;
    private BigDecimal price;
    private Integer stock;
    
    public CreateProductRequest(String sku, String name, BigDecimal price, Integer stock) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.stock = stock;
        // Sem validacao
    }
    // Getters, setters, equals, hashCode, toString manualmente
}
```

### 6. Virtual Threads (Preludio)

✅ CERTO:
```java
@Configuration
public class ExecutorConfig {
    @Bean
    public Executor taskExecutor() {
        // Executor baseado em virtual threads (Java 21+)
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}

@Service
public class OrderService {
    @Async
    public CompletableFuture<OrderResponse> processAsync(CreateOrderRequest request) {
        return CompletableFuture.completedFuture(create(request));
    }
}
```

## Spring Boot 4 Features - Explore Melhor

### 1. RestClient (Substitui RestTemplate)

✅ CERTO:
```java
@Configuration
public class RestClientConfig {
    @Bean
    public RestClient restClient() {
        return RestClient.builder()
            .baseUrl("https://api.stripe.com")
            .defaultHeader("Authorization", "Bearer ${STRIPE_KEY}")
            .requestInterceptor((request, body, execution) -> {
                log.debug("Outgoing request: {} {}", request.getMethod(), request.getURI());
                return execution.execute(request, body);
            })
            .build();
    }
}

@Service
public class StripeService {
    private final RestClient restClient;

    public StripeService(RestClient restClient) {
        this.restClient = restClient;
    }

    public PaymentResponse charge(BigDecimal amount) {
        return restClient.post()
            .uri("/v1/charges")
            .body(Map.of("amount", amount))
            .retrieve()
            .body(PaymentResponse.class);
    }
}
```

❌ ERRADO:
```java
@Service
public class StripeServiceOld {
    private final RestTemplate restTemplate; // Deprecated
    
    public PaymentResponse charge(BigDecimal amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + stripeKey);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
            Map.of("amount", amount),
            headers
        );
        
        return restTemplate.postForObject(
            "https://api.stripe.com/v1/charges",
            request,
            PaymentResponse.class
        );
    }
}
```

### 2. ProblemDetail (RFC 7807)

✅ CERTO:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ProductNotFoundException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Produto nao encontrado");
        problem.setDetail(ex.getMessage());
        problem.setProperty("errorCode", "PRODUCT_NOT_FOUND");
        problem.setProperty("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidation(ValidationException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        problem.setTitle("Falha na validacao");
        problem.setDetail(ex.getMessage());
        problem.setProperty("fields", ex.getFieldErrors());
        return ResponseEntity.unprocessableEntity().body(problem);
    }
}
```

❌ ERRADO:
```java
@RestControllerAdvice
public class GlobalExceptionHandlerOld {
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ProductNotFoundException ex) {
        return ResponseEntity.status(404).body("Product not found: " + ex.getMessage());
        // Sem estrutura padrao, difícil de processar no cliente
    }
}
```

### 3. Observability com @Observed (Micrometer)

✅ CERTO:
```java
@Service
public class OrderService {
    private final OrderRepository repository;

    @Observed(name = "order.create", description = "Criar novo pedido")
    public OrderResponse create(CreateOrderRequest request) {
        // Metrica automatica: tempo de execucao, sucesso/erro
        var order = new Order(...);
        return repository.save(order);
    }

    @Observed(name = "order.find", description = "Buscar pedido")
    public Optional<OrderResponse> findById(String id) {
        return repository.findById(id).map(this::toResponse);
    }
}

// Spring automaticamente rastreia:
// - Tempo de execução
// - Taxa de erro
// - Histograma de latência
```

❌ ERRADO:
```java
@Service
public class OrderServiceOld {
    public OrderResponse create(CreateOrderRequest request) {
        long start = System.currentTimeMillis();
        try {
            var order = repository.save(new Order(...));
            return toResponse(order);
        } finally {
            long duration = System.currentTimeMillis() - start;
            // Log manual e inconsistente
            log.info("Order creation took {} ms", duration);
        }
    }
}
```

### 4. ConfigurationProperties Typed

✅ CERTO:
```java
@ConfigurationProperties(prefix = "app.payment")
public record PaymentProperties(
    String stripeKey,
    String stripeSecret,
    @DurationUnit(ChronoUnit.SECONDS) Duration timeout,
    Integer maxRetries,
    Boolean enableSandbox
) {}

@Configuration
@EnableConfigurationProperties(PaymentProperties.class)
public class AppConfig {
    @Bean
    public StripeService stripeService(PaymentProperties props) {
        return new StripeService(props.stripeKey(), props.stripeSecret());
    }
}

// application.yml
/*
app:
  payment:
    stripe-key: sk_test_...
    stripe-secret: sk_secret_...
    timeout: 30s
    max-retries: 3
    enable-sandbox: true
*/
```

❌ ERRADO:
```java
@Component
public class PaymentConfig {
    @Value("${app.payment.stripeKey}")
    private String stripeKey;
    
    @Value("${app.payment.stripeSecret}")
    private String stripeSecret;
    
    // Sem type safety, sem validacao
}
```

## Helpers e Utility Classes - Quando e Como Usar

Helpers devem ser **utilitários puros** (sem estado), **testáveis** e **reutilizáveis**. Evite acumular lógica neles.

### 1. Converters/Parsers - Transformacoes de Dados

Quando precisa converter entre tipos ou formatos.

✅ CERTO:
```java
// infrastructure/converter/MoneyConverter.java
public final class MoneyConverter {
    private MoneyConverter() {} // Nao pode instanciar

    public static String formatCurrency(BigDecimal value) {
        return String.format("R$ %.2f", value);
    }

    public static BigDecimal parseAmount(String formatted) {
        return new BigDecimal(formatted.replaceAll("[^0-9.]", ""));
    }

    public static boolean isValidAmount(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }
}

// Uso em mapper ou converter
@Mapper(componentModel = "spring", uses = MoneyConverter.class)
public interface OrderMapper {
    @Mapping(target = "totalFormatted", expression = "java(MoneyConverter.formatCurrency(order.total()))")
    OrderResponse toResponse(Order order);
}
```

❌ ERRADO:
```java
@Service
public class OrderService {
    // Nao coloque helpers em services
    public String formatCurrency(BigDecimal value) {
        return String.format("R$ %.2f", value);
    }
}
```

### 2. Validators - Logica de Validacao Complexa

Quando validacao vai além de Bean Validation.

✅ CERTO:
```java
// domain/validator/OrderValidator.java
public final class OrderValidator {
    private OrderValidator() {}

    public static void validateOrder(Order order) {
        if (order.items().isEmpty()) {
            throw new OrderValidationException("Order must have at least one item");
        }

        for (var item : order.items()) {
            if (item.quantity() <= 0) {
                throw new OrderValidationException("Item quantity must be positive");
            }
        }

        if (order.total().compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrderValidationException("Order total must be positive");
        }
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}

// Uso em service
@Service
public class OrderService {
    public OrderResponse create(CreateOrderRequest request) {
        var order = mapper.toEntity(request);
        OrderValidator.validateOrder(order); // Chamada explícita
        return repository.save(order);
    }
}
```

❌ ERRADO:
```java
@Service
public class OrderService {
    public OrderResponse create(CreateOrderRequest request) {
        var order = mapper.toEntity(request);
        
        // Validacao espalhada no service
        if (order.items().isEmpty()) {
            throw new OrderValidationException("...");
        }
        if (order.items().stream().anyMatch(i -> i.quantity() <= 0)) {
            throw new OrderValidationException("...");
        }
        
        return repository.save(order);
    }
}
```

### 3. Formatters - Formatacao de Saida

Quando precisa formatar valores para exibicao.

✅ CERTO:
```java
// infrastructure/formatter/DateTimeFormatter.java
public final class DateTimeFormatter {
    private DateTimeFormatter() {}

    public static String formatBrazilianDate(LocalDate date) {
        return date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public static String formatBrazilianDateTime(LocalDateTime dateTime) {
        return dateTime.format(java.time.format.DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm:ss")
            .withLocale(Locale.of("pt", "BR")));
    }

    public static String formatOrderStatus(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Pendente";
            case CONFIRMED -> "Confirmado";
            case SHIPPED -> "Enviado";
            case DELIVERED -> "Entregue";
            case CANCELLED -> "Cancelado";
        };
    }
}

// Uso em mapper
@Mapper(componentModel = "spring", uses = DateTimeFormatter.class)
public interface OrderMapper {
    @Mapping(target = "createdAtFormatted", 
             expression = "java(DateTimeFormatter.formatBrazilianDateTime(order.createdAt()))")
    @Mapping(target = "statusFormatted",
             expression = "java(DateTimeFormatter.formatOrderStatus(order.status()))")
    OrderResponse toResponse(Order order);
}
```

❌ ERRADO:
```java
// DTO com logica de formatacao
public record OrderResponse(
    String id,
    String createdAtFormatted
) {
    public OrderResponse(Order order) {
        this(order.id(), formatBrazilianDate(order.createdAt())); // Logica no DTO
    }

    private static String formatBrazilianDate(LocalDateTime dt) {
        return dt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
}
```

### 4. Calculators - Logica de Calculo Pura

Quando temos calculos reusaveis, especialmente para dominio.

✅ CERTO:
```java
// domain/calculator/DiscountCalculator.java
public final class DiscountCalculator {
    private DiscountCalculator() {}

    private static final BigDecimal PREMIUM_RATE = new BigDecimal("0.10"); // 10%
    private static final BigDecimal VOLUME_DISCOUNT_THRESHOLD = new BigDecimal("1000");
    private static final BigDecimal VOLUME_RATE = new BigDecimal("0.05"); // 5%

    public static BigDecimal calculateDiscount(Customer customer, Order order) {
        var basePrice = order.total();

        if (customer.isPremium()) {
            return basePrice.multiply(PREMIUM_RATE);
        }

        if (basePrice.compareTo(VOLUME_DISCOUNT_THRESHOLD) > 0) {
            return basePrice.multiply(VOLUME_RATE);
        }

        return BigDecimal.ZERO;
    }

    public static BigDecimal applyTax(BigDecimal amount, String region) {
        var taxRate = switch (region) {
            case "SP" -> new BigDecimal("0.18"); // 18%
            case "RJ" -> new BigDecimal("0.20"); // 20%
            default -> new BigDecimal("0.15"); // 15%
        };
        
        return amount.multiply(taxRate.add(BigDecimal.ONE));
    }

    public static BigDecimal calculateFinalPrice(Order order, Customer customer) {
        var discount = calculateDiscount(customer, order);
        var afterDiscount = order.total().subtract(discount);
        return applyTax(afterDiscount, customer.region());
    }
}

// Uso em service
@Service
public class PricingService {
    public BigDecimal getFinalPrice(Order order, Customer customer) {
        return DiscountCalculator.calculateFinalPrice(order, customer);
    }
}
```

❌ ERRADO:
```java
@Service
public class PricingService {
    // Calculos espalhados no service
    public BigDecimal getFinalPrice(Order order, Customer customer) {
        var basePrice = order.total();
        BigDecimal discount = BigDecimal.ZERO;
        
        if (customer.isPremium()) {
            discount = basePrice.multiply(new BigDecimal("0.10"));
        } else if (basePrice.compareTo(new BigDecimal("1000")) > 0) {
            discount = basePrice.multiply(new BigDecimal("0.05"));
        }
        
        var afterDiscount = basePrice.subtract(discount);
        var taxRate = customer.region().equals("SP") ? new BigDecimal("0.18") : new BigDecimal("0.15");
        
        return afterDiscount.multiply(taxRate.add(BigDecimal.ONE));
    }
}
```

### 5. Collection Utilities - Operacoes em Colecoes

Utilitarios para operacoes comuns em colecoes sem criar streams verbose.

✅ CERTO:
```java
// infrastructure/util/CollectionHelper.java
public final class CollectionHelper {
    private CollectionHelper() {}

    public static <T> boolean isNullOrEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> List<T> emptyIfNull(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }

    public static <T> T getFirstOrNull(List<T> list) {
        return list != null && !list.isEmpty() ? list.getFirst() : null;
    }

    public static <T> List<T> filterByPredicate(List<T> list, java.util.function.Predicate<T> predicate) {
        return emptyIfNull(list).stream()
            .filter(predicate)
            .toList();
    }
}

// Uso
@Service
public class OrderService {
    public List<OrderItem> getAvailableItems(Order order) {
        return CollectionHelper.filterByPredicate(
            order.items(),
            item -> item.quantity() > 0
        );
    }
}
```

❌ ERRADO:
```java
@Service
public class OrderService {
    public List<OrderItem> getAvailableItems(Order order) {
        if (order.items() == null) {
            return new ArrayList<>();
        }
        
        return order.items().stream()
            .filter(item -> item.quantity() > 0)
            .toList();
    }
    
    public boolean hasItems(Order order) {
        return order.items() != null && !order.items().isEmpty();
    }
}
```

## Design Patterns para IFs Complexos

### 1. Strategy Pattern - Multiplos Comportamentos

Quando temos multiplos IFs verificando tipos ou condicoes para executar diferentes logicas.

✅ CERTO:
```java
// Interface da estrategia
public interface DiscountStrategy {
    BigDecimal apply(BigDecimal price);
}

// Implementacoes concretas
public class PremiumDiscount implements DiscountStrategy {
    @Override
    public BigDecimal apply(BigDecimal price) {
        return price.multiply(PREMIUM_RATE);
    }
}

public class SeasonalDiscount implements DiscountStrategy {
    @Override
    public BigDecimal apply(BigDecimal price) {
        return price.multiply(SEASONAL_RATE);
    }
}

public class NoDiscount implements DiscountStrategy {
    @Override
    public BigDecimal apply(BigDecimal price) {
        return price;
    }
}

// Service usa strategy sem IFs
@Service
public class PricingService {
    private final Map<String, DiscountStrategy> strategies;

    public BigDecimal calculatePrice(Product product, Customer customer) {
        var strategy = strategies.getOrDefault(customer.getType(), new NoDiscount());
        return strategy.apply(product.getPrice());
    }
}
```

❌ ERRADO:
```java
public BigDecimal calculatePrice(Product product, Customer customer) {
    if (customer.isPremium()) { // IF complexo
        return product.getPrice().multiply(PREMIUM_RATE);
    } else if (isSeasonalPromo()) { // Outro IF
        return product.getPrice().multiply(SEASONAL_RATE);
    } else if (customer.isVIP()) { // Outro IF
        return product.getPrice().multiply(VIP_RATE);
    } else {
        return product.getPrice();
    }
}
```

### 2. State Pattern - Mudancas de Estado

Quando temos IFs controlando transicoes de estado (Order: PENDING -> CONFIRMED -> SHIPPED).

✅ CERTO:
```java
// Estados como enums ou interfaces
public interface OrderState {
    void confirm(Order order);
    void ship(Order order);
    void cancel(Order order);
    String getStatus();
}

public class PendingState implements OrderState {
    @Override
    public void confirm(Order order) {
        order.setState(new ConfirmedState());
    }

    @Override
    public void ship(Order order) {
        throw new IllegalStateException("Cannot ship pending order");
    }

    @Override
    public String getStatus() {
        return "PENDING";
    }
}

public class ConfirmedState implements OrderState {
    @Override
    public void confirm(Order order) {
        throw new IllegalStateException("Already confirmed");
    }

    @Override
    public void ship(Order order) {
        order.setState(new ShippedState());
    }

    @Override
    public String getStatus() {
        return "CONFIRMED";
    }
}

// Order nao tem IFs
public record Order(String id, OrderState state) {
    public void confirm() {
        state.confirm(this);
    }

    public void ship() {
        state.ship(this);
    }
}
```

❌ ERRADO:
```java
public void confirm() {
    if (status.equals("PENDING")) { // IF complexo
        status = "CONFIRMED";
    } else if (status.equals("CONFIRMED")) {
        throw new IllegalStateException("Already confirmed");
    } else {
        throw new IllegalStateException("Cannot confirm in state: " + status);
    }
}

public void ship() {
    if (status.equals("CONFIRMED")) { // Outro IF
        status = "SHIPPED";
    } else {
        throw new IllegalStateException("Cannot ship in state: " + status);
    }
}
```

### 3. Builder Pattern - Construcao Complexa

Para evitar multiplos IFs durante construcao de objetos.

✅ CERTO:
```java
public class OrderBuilder {
    private Order order = new Order(null, new ArrayList<>(), null, null);

    public OrderBuilder withCustomer(Customer customer) {
        this.order = new Order(order.id(), order.items(), customer, order.total());
        return this;
    }

    public OrderBuilder addItem(OrderItem item) {
        var items = new ArrayList<>(order.items());
        items.add(item);
        this.order = new Order(order.id(), items, order.customer(), order.total());
        return this;
    }

    public OrderBuilder withTotal(BigDecimal total) {
        this.order = new Order(order.id(), order.items(), order.customer(), total);
        return this;
    }

    public Order build() {
        if (order.customer() == null) throw new IllegalStateException("Customer required");
        if (order.items().isEmpty()) throw new IllegalStateException("At least one item required");
        return order;
    }
}

// Uso fluente, sem IFs no chamador
var order = new OrderBuilder()
    .withCustomer(customer)
    .addItem(item1)
    .addItem(item2)
    .withTotal(total)
    .build();
```

❌ ERRADO:
```java
public Order createOrder(Customer customer, List<OrderItem> items, BigDecimal total) {
    if (customer == null) { // IFs de validacao espalhados
        throw new IllegalStateException("Customer required");
    }
    if (items == null || items.isEmpty()) {
        throw new IllegalStateException("At least one item required");
    }
    if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalStateException("Total invalid");
    }
    return new Order(UUID.randomUUID().toString(), items, customer, total);
}
```

### 4. Chain of Responsibility - Pipeline de Validacoes

Para evitar IFs sequenciais de validacao.

✅ CERTO:
```java
public interface ValidationHandler {
    void validate(CreateOrderRequest request);
    void setNext(ValidationHandler next);
}

public class CustomerValidation implements ValidationHandler {
    private ValidationHandler next;

    @Override
    public void validate(CreateOrderRequest request) {
        if (request.customerId() == null || request.customerId().isBlank()) {
            throw new ValidationException("Customer ID required");
        }
        if (next != null) next.validate(request);
    }

    @Override
    public void setNext(ValidationHandler next) {
        this.next = next;
    }
}

public class ItemsValidation implements ValidationHandler {
    private ValidationHandler next;

    @Override
    public void validate(CreateOrderRequest request) {
        if (request.items().isEmpty()) {
            throw new ValidationException("At least one item required");
        }
        if (next != null) next.validate(request);
    }

    @Override
    public void setNext(ValidationHandler next) {
        this.next = next;
    }
}

// Encadeamento sem IFs
var validator = new CustomerValidation();
validator.setNext(new ItemsValidation());
validator.setNext(new PaymentValidation());

validator.validate(request); // Se alguma falha, lanca excecao
```

❌ ERRADO:
```java
public void validate(CreateOrderRequest request) {
    if (request.customerId() == null || request.customerId().isBlank()) { // IF
        throw new ValidationException("Customer ID required");
    }
    if (request.items().isEmpty()) { // IF
        throw new ValidationException("At least one item required");
    }
    if (request.paymentMethod() == null) { // IF
        throw new ValidationException("Payment method required");
    }
    if (request.total().compareTo(BigDecimal.ZERO) <= 0) { // IF
        throw new ValidationException("Total invalid");
    }
    // Muitos IFs espalhados
}
```

### 5. Factory Pattern - Criacao Baseada em Tipo

Para evitar IFs decidindo qual tipo criar.

✅ CERTO:
```java
public interface PaymentProcessor {
    void process(BigDecimal amount);
}

public class CreditCardProcessor implements PaymentProcessor {
    @Override
    public void process(BigDecimal amount) {
        // Processa cartao de credito
    }
}

public class PayPalProcessor implements PaymentProcessor {
    @Override
    public void process(BigDecimal amount) {
        // Processa via PayPal
    }
}

@Component
public class PaymentProcessorFactory {
    private final Map<String, PaymentProcessor> processors;

    public PaymentProcessorFactory(CreditCardProcessor cc, PayPalProcessor paypal) {
        this.processors = Map.of(
            "CREDIT_CARD", cc,
            "PAYPAL", paypal
        );
    }

    public PaymentProcessor getProcessor(String type) {
        return processors.getOrDefault(type, new CreditCardProcessor());
    }
}

// Service usa factory sem IF
@Service
public class OrderService {
    private final PaymentProcessorFactory factory;

    public void checkout(Order order) {
        var processor = factory.getProcessor(order.getPaymentMethod());
        processor.process(order.getTotal());
    }
}
```

❌ ERRADO:
```java
public void checkout(Order order) {
    if (order.getPaymentMethod().equals("CREDIT_CARD")) { // IF
        processCreditCard(order.getTotal());
    } else if (order.getPaymentMethod().equals("PAYPAL")) { // IF
        processPayPal(order.getTotal());
    } else if (order.getPaymentMethod().equals("BITCOIN")) { // IF
        processBitcoin(order.getTotal());
    } else {
        throw new IllegalArgumentException("Payment method not supported");
    }
}
```

### 6. Decorator Pattern - Adicionar Comportamentos

Para adicionar comportamentos sem multiplicar IFs.

✅ CERTO:
```java
public interface OrderProcessor {
    Order process(Order order);
}

public class BaseOrderProcessor implements OrderProcessor {
    @Override
    public Order process(Order order) {
        return order; // Processa basicamente
    }
}

public abstract class OrderProcessorDecorator implements OrderProcessor {
    protected OrderProcessor decorated;

    public OrderProcessorDecorator(OrderProcessor decorated) {
        this.decorated = decorated;
    }
}

public class TaxProcessor extends OrderProcessorDecorator {
    @Override
    public Order process(Order order) {
        var processed = decorated.process(order);
        var taxedTotal = processed.total().multiply(TAX_RATE);
        return new Order(processed.id(), processed.items(), processed.customer(), taxedTotal);
    }
}

public class DiscountProcessor extends OrderProcessorDecorator {
    @Override
    public Order process(Order order) {
        var processed = decorated.process(order);
        var discountedTotal = processed.total().multiply(DISCOUNT_RATE);
        return new Order(processed.id(), processed.items(), processed.customer(), discountedTotal);
    }
}

// Composicao sem IFs
var processor = new DiscountProcessor(new TaxProcessor(new BaseOrderProcessor()));
var finalOrder = processor.process(order);
```

❌ ERRADO:
```java
public Order process(Order order, boolean applyTax, boolean applyDiscount) {
    var total = order.total();
    
    if (applyTax) { // IF
        total = total.multiply(TAX_RATE);
    }
    if (applyDiscount) { // IF
        total = total.multiply(DISCOUNT_RATE);
    }
    if (applyTax && applyDiscount) { // IF para combinar
        // Ajuste especial para quando ambas estao ativas
        total = total.add(SPECIAL_ADJUSTMENT);
    }
    
    return new Order(order.id(), order.items(), order.customer(), total);
}
```

## Constantes vs Enums - Boas Praticas

### Quando Usar Enums

Use Enums quando tem um **conjunto fechado e conhecido de valores** que são parte do **domínio** e precisam de comportamento.

✅ CERTO - Enum para Status (Dominio):
```java
// domain/enums/OrderStatus.java
public enum OrderStatus {
    PENDING("Pendente", "Aguardando confirmacao"),
    CONFIRMED("Confirmado", "Pedido confirmado"),
    SHIPPED("Enviado", "Enviado para entrega"),
    DELIVERED("Entregue", "Recebido pelo cliente"),
    CANCELLED("Cancelado", "Pedido foi cancelado");

    private final String displayName;
    private final String description;

    OrderStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PENDING -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == SHIPPED || next == CANCELLED;
            case SHIPPED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}

// Uso
@Document(collection = "orders")
public record Order(
    @Id String id,
    OrderStatus status,  // Type-safe, nao String
    List<OrderItem> items,
    BigDecimal total
) {}

// Service
@Service
public class OrderService {
    public void ship(Order order) {
        if (!order.status().canTransitionTo(OrderStatus.SHIPPED)) {
            throw new InvalidStateTransitionException();
        }
        // Atualizar status
    }
}
```

❌ ERRADO - Strings ou constantes para Status:
```java
public class OrderConstants {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_SHIPPED = "SHIPPED";
}

// Problema: String nao oferece type safety
@Document(collection = "orders")
public record Order(
    @Id String id,
    String status,  // Qualquer string eh aceito, nao validado
    List<OrderItem> items,
    BigDecimal total
) {}

// Service sem validacao de transicao
public void ship(Order order) {
    if ("PENDING".equals(order.status()) || "SHIPPED".equals(order.status())) {
        // Logica duplicada
    }
}
```

### Quando Usar Constantes

Use constantes quando tem **valores literais** que nao sao dominio (URLs, timeouts, limites, chaves externas).

✅ CERTO - Constantes para Configuracao:
```java
// infrastructure/config/AppConstants.java
public final class AppConstants {
    private AppConstants() {} // Nao pode instanciar

    // Timeout e limites
    public static final int DEFAULT_TIMEOUT_SECONDS = 30;
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final int BATCH_SIZE = 100;

    // URLs e endpoints
    public static final String STRIPE_API_BASE_URL = "https://api.stripe.com";
    public static final String PAYMENT_WEBHOOK_PATH = "/api/webhooks/payment";

    // Taxas e descontos
    public static final BigDecimal TAX_RATE_SP = new BigDecimal("0.18");
    public static final BigDecimal PREMIUM_DISCOUNT_RATE = new BigDecimal("0.10");

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
}

// Uso
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @GetMapping
    public Page<OrderResponse> listOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE + "") int size
    ) {
        return service.findAll(PageRequest.of(page, size));
    }
}

@Configuration
@ConfigurationProperties(prefix = "app.stripe")
public record StripeConfig(
    String apiKey,
    String webhookSecret,
    @DurationUnit(ChronoUnit.SECONDS) Duration timeout
) {
    public StripeConfig {
        Objects.requireNonNull(apiKey, "Stripe API key is required");
        if (timeout.getSeconds() < AppConstants.DEFAULT_TIMEOUT_SECONDS) {
            throw new IllegalArgumentException("Timeout must be >= " + AppConstants.DEFAULT_TIMEOUT_SECONDS);
        }
    }
}
```

❌ ERRADO - Constantes para Dominio (deve ser Enum):
```java
public class OrderConstants {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // Isso eh dominio, nao deveria ser constante String
}
```

### Padroes de Enum Avancado

#### 1. Enum com Metadata (Lookup Dictionary)

```java
public enum PaymentMethod {
    CREDIT_CARD("Credit Card", "credit_card", 3, true),
    DEBIT_CARD("Debit Card", "debit_card", 1, true),
    PIX("PIX", "pix", 0, false),
    BANK_TRANSFER("Bank Transfer", "bank_transfer", 1, false),
    WALLET("Wallet", "wallet", 0, true);

    private final String displayName;
    private final String code;
    private final int processingDays;
    private final boolean requiresVerification;

    PaymentMethod(String displayName, String code, int processingDays, boolean requiresVerification) {
        this.displayName = displayName;
        this.code = code;
        this.processingDays = processingDays;
        this.requiresVerification = requiresVerification;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }

    public static Optional<PaymentMethod> fromCode(String code) {
        return Arrays.stream(values())
            .filter(pm -> pm.code.equals(code))
            .findFirst();
    }

    public boolean isInstant() {
        return processingDays == 0;
    }

    public boolean requiresVerification() {
        return requiresVerification;
    }
}

// Uso
@Service
public class PaymentService {
    public int estimateDeliveryDays(PaymentMethod method) {
        return method.getProcessingDays();
    }

    public void validatePayment(PaymentMethod method, String verificationCode) {
        if (method.requiresVerification() && verificationCode == null) {
            throw new PaymentValidationException("Verification required for " + method.getDisplayName());
        }
    }
}
```

#### 2. Enum com Comportamento (Strategy Pattern)

```java
public enum DiscountStrategy {
    NO_DISCOUNT {
        @Override
        public BigDecimal apply(BigDecimal amount) {
            return amount;
        }
    },
    PERCENTAGE {
        private static final BigDecimal RATE = new BigDecimal("0.10");

        @Override
        public BigDecimal apply(BigDecimal amount) {
            return amount.multiply(RATE);
        }
    },
    FIXED_AMOUNT {
        private static final BigDecimal FIXED = new BigDecimal("50.00");

        @Override
        public BigDecimal apply(BigDecimal amount) {
            var discounted = amount.subtract(FIXED);
            return discounted.compareTo(BigDecimal.ZERO) > 0 ? discounted : BigDecimal.ZERO;
        }
    },
    VOLUME {
        @Override
        public BigDecimal apply(BigDecimal amount) {
            return amount.compareTo(new BigDecimal("1000")) > 0
                ? amount.multiply(new BigDecimal("0.05"))
                : BigDecimal.ZERO;
        }
    };

    public abstract BigDecimal apply(BigDecimal amount);

    public BigDecimal calculateFinalPrice(BigDecimal price) {
        return price.subtract(apply(price));
    }
}

// Uso - sem IFs!
@Service
public class PricingService {
    public BigDecimal getFinalPrice(BigDecimal price, DiscountStrategy strategy) {
        return strategy.calculateFinalPrice(price);
    }
}
```

#### 3. Enum com Comparacao (Hierarchy)

```java
public enum UserRole {
    ADMIN(3, "Administrador"),
    MANAGER(2, "Gerenciador"),
    CUSTOMER(1, "Cliente"),
    GUEST(0, "Visitante");

    private final int level;
    private final String displayName;

    UserRole(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }

    public boolean hasPermission(UserRole requiredRole) {
        return this.level >= requiredRole.level;
    }

    public boolean isHigherThan(UserRole other) {
        return this.level > other.level;
    }

    public boolean isAdminOrHigher() {
        return this.level >= ADMIN.level;
    }
}

// Uso em validacao
@RestController
@RequestMapping("/api/users")
public class UserController {
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
        @PathVariable String id,
        @AuthenticationPrincipal User currentUser
    ) {
        if (!currentUser.getRole().hasPermission(UserRole.MANAGER)) {
            throw new AccessDeniedException("Only managers can delete users");
        }
        // Deletar usuario
        return ResponseEntity.noContent().build();
    }
}
```

#### 4. Sealed + Enum para Validacao

```java
// Combina poder de enums com sealed classes
public sealed interface TransactionType permits Transaction.INCOME, Transaction.EXPENSE, Transaction.TRANSFER {
    String getCode();
    boolean isDebit();
}

public enum Transaction {
    INCOME("INCOME") {
        @Override
        public boolean isDebit() { return false; }
    },
    EXPENSE("EXPENSE") {
        @Override
        public boolean isDebit() { return true; }
    },
    TRANSFER("TRANSFER") {
        @Override
        public boolean isDebit() { return true; }
    };

    private final String code;

    Transaction(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }

    public abstract boolean isDebit();
}
```

## Resumo - Decidir Entre Constantes e Enums

| Criterio | Enum | Constante |
|----------|------|-----------|
| **Type Safety** | ✅ Sim | ❌ Nao (String/int) |
| **Comportamento** | ✅ Pode ter metodos | ❌ Apenas valor |
| **Dominio** | ✅ Usa | ❌ Nao usa |
| **Validacao** | ✅ Em tempo de compilacao | ❌ Em runtime |
| **Switch/Pattern Matching** | ✅ Exaustivo | ❌ Nao |
| **Lookup/Busca** | ✅ Facil com metodos | ❌ Manual |
| **Serializacao** | ✅ Nativa | ✅ Nativa |
| **Exemplo** | OrderStatus, UserRole | TIMEOUT_SECONDS, MAX_SIZE |
```
