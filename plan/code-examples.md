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
