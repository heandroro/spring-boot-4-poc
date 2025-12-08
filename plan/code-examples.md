# Exemplos Praticos de Boas Praticas

Colecao enxuta de exemplos aplicando os padroes definidos no projeto.

🚫 **Lombok nao permitido:** Evite qualquer anotacao Lombok (`@Data`, `@Builder`, `@Getter`, `@Setter`, `@Value`, `@AllArgsConstructor`, `@NoArgsConstructor`). Prefira Records, construtores explicitos e MapStruct para mapeamento.

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
```
