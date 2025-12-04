# VS Code Copilot Instructions

Este projeto segue padrões específicos. Use este guia ao gerar ou sugerir código.

## 🏗️ Arquitetura
- **DDD com 4 camadas**: domain → application → infrastructure → web
- Controllers **nunca** chamam Repositories diretamente
- Services contêm toda lógica de negócio

## ☕ Java 25
- **Records** para imutabilidade (não Lombok)
- **Pattern Matching**: `switch(x) { case Type t -> ... }`
- **Sequenced Collections**: `list.getFirst()`, `list.getLast()`
- **Text Blocks** para strings multi-linha

## 🌱 Spring Boot 4
- **Constructor Injection** sempre
- **RestClient** (não RestTemplate)
- **ProblemDetail** para erros (RFC 7807)
- **@Observed** para métricas

## 🗄️ MongoDB
- **Embedding**: Dados acoplados (Address em Customer)
- **Referencing**: Agregados independentes (customerId em Order)
- **Snapshots**: Histórico (priceAtPurchase)
- **@Indexed**: Campos pesquisados
- **TTL**: Dados temporários (Cart expira em 7 dias)

## 🧪 Testes
- **JUnit 6**: `@DisplayName("descrição legível")`
- **Instancio**: `Instancio.create(Product.class)` para fixtures
- **Mockito**: `@Mock`, `@InjectMocks`, `@ExtendWith(MockitoExtension.class)`
- **Testcontainers**: MongoDB real em integração

## 🔒 Segurança
- **@PreAuthorize**: Controle de acesso por role
- **BCrypt**: Hashear senhas sempre
- **JWT**: Validar em endpoints protegidos
- **DTOs**: Nunca expor dados sensíveis

## 🌐 REST API
- **Status HTTP**: 200 OK, 201 Created, 204 No Content, 404 Not Found
- **Bean Validation**: `@NotBlank`, `@NotNull`, `@Email`
- **Paginação**: `Page<T> findAll(Pageable pageable)`

## 📝 Clean Code
- Nomes descritivos e auto-explicativos
- Métodos pequenos (máximo 20 linhas)
- Evitar comentários óbvios
- Separar lógica complexa

## ✅ Exemplos

### Entity com Record
```java
@Document(collection = "products")
public record Product(
    @Id String id,
    @Indexed String sku,
    String name,
    BigDecimal price
) {}
```

### Service com Constructor Injection
```java
@Service
public class ProductService {
    private final ProductRepository repository;
    
    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }
}
```

### Controller com Validação
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;
    
    public ProductController(ProductService service) {
        this.service = service;
    }
    
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductDto> create(@Valid @RequestBody CreateProductRequest request) {
        var product = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
}
```

### Teste com Instancio
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService - Create Product")
class ProductServiceTest {
    @Mock private ProductRepository repository;
    @InjectMocks private ProductService service;
    
    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProduct() {
        var request = Instancio.create(CreateProductRequest.class);
        var product = Instancio.create(Product.class);
        
        when(repository.save(any())).thenReturn(product);
        
        var result = service.create(request);
        
        assertThat(result).isNotNull();
        verify(repository).save(any());
    }
}
```

Detalhes completos: [CONTRIBUTING.md](../CONTRIBUTING.md)
