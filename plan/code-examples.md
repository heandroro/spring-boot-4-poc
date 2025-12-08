# Exemplos Praticos de Boas Praticas

Colecao enxuta de exemplos aplicando os padroes definidos no projeto.

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
