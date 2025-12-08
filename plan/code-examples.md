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
```
