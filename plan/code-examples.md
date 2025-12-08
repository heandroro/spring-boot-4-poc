# Exemplos Praticos de Boas Praticas

Colecao enxuta de exemplos aplicando os padroes definidos no projeto.

🚫 **Lombok nao permitido:** Evite qualquer anotacao Lombok (`@Data`, `@Builder`, `@Getter`, `@Setter`, `@Value`, `@AllArgsConstructor`, `@NoArgsConstructor`). Prefira Records, construtores explicitos e MapStruct para mapeamento.

## 📋 Índice

### PARTE I: Fundamentos da Arquitetura (Camadas DDD)

1. [Entidade de Dominio (MongoDB)](#entidade-de-dominio-mongodb)
2. [DTO com Bean Validation](#dto-com-bean-validation)
3. [Mapper com MapStruct](#mapper-com-mapstruct)
4. [UseCase com Regras e Constructor Injection](#usecase-com-regras-e-constructor-injection)

### PARTE II: Web Layer (REST, Testes e Tratamento de Erros)

5. [Controller REST com @PreAuthorize e Status Corretos](#controller-rest-com-preauthorize-e-status-corretos)
6. [Tratamento de Erros com ProblemDetail](#tratamento-de-erros-com-problemdetail)
7. [Teste Unitario com JUnit 6, Mockito e Instancio](#teste-unitario-com-junit-6-mockito-e-instancio)
8. [Teste de Repository com Testcontainers](#teste-de-repository-com-testcontainers)

### PARTE III: Padrões Arquiteturais e Injeção de Dependências

9. [Spring Annotations - @Bean, @Component, @Service, @Repository](#spring-annotations---bean-component-usecase-repository)
   - 9.1 [@Repository - Acesso a Dados](#repository---acesso-a-dados)
   - 9.2 [@Service - Logica de Negocio](#usecase---logica-de-negocio)
   - 9.3 [@Component - Utilitarios e Helpers](#component---utilitarios-e-helpers)
   - 9.4 [@Bean - Configuracao e Terceiros](#bean---configuracao-e-terceiros)
   - 9.5 [🔑 REGRA OBRIGATÓRIA: @Bean para Interfaces](#-regra-obrigatória-bean-para-interfaces)
   - 9.6 [Anti-patterns: Quando NÃO Usar](#anti-patterns-quando-nao-usar)

### PARTE IV: Java Moderno (25) - Recursos Avancados

10. [Java 25 Features - Explore Melhor](#java-25-features---explore-melhor)
    - 10.1 [Pattern Matching em Switch](#1-pattern-matching-em-switch-record-patterns)
    - 10.2 [Sequenced Collections](#2-sequenced-collections---getfirst-e-getlast)
    - 10.3 [Text Blocks](#3-text-blocks-para-queries-mongodb-e-sql)
    - 10.4 [Sealed Classes](#4-sealed-classes---controle-de-hierarquia)
    - 10.5 [Records com Validacao Compacta](#5-records-com-validacao-compacta)
    - 10.6 [Virtual Threads](#6-virtual-threads-preludio)

11. [Streams - Boas Praticas vs Anti-patterns](#streams---boas-praticas-vs-anti-patterns)
    - 11.1 [Filter + Map + Collect](#1-filter--map--collect)
    - 11.2 [FlatMap para Colecoes Aninhadas](#2-flatmap-para-colecoes-aninhadas)
    - 11.3 [Terminal Operations](#3-terminal-operations---collect-foreach-reduce)
    - 11.4 [Performance e Lazy Evaluation](#4-performance-e-lazy-evaluation)
    - 11.5 [Evitar Anti-patterns](#5-anti-patterns-em-streams)

### PARTE V: Spring Boot 4 - Features Modernas

12. [Spring Boot 4 Features - Explore Melhor](#spring-boot-4-features---explore-melhor)
    - 12.1 [RestClient](#1-restclient-substitui-resttemplate)
    - 12.2 [ProblemDetail (RFC 7807)](#2-problemdetail-rfc-7807)
    - 12.3 [@Observed (Micrometer)](#3-observability-com-observed-micrometer)
    - 12.4 [ConfigurationProperties Typed](#4-configurationproperties-typed)

### PARTE VI: Clean Code - Boas Práticas e Padrões

13. [Helpers e Utility Classes - Quando e Como Usar](#helpers-e-utility-classes---quando-e-como-usar)
    - 13.1 [Converters/Parsers](#1-convertersparsers---transformacoes-de-dados)
    - 13.2 [Validators](#2-validators---logica-de-validacao-complexa)
    - 13.3 [Formatters](#3-formatters---formatacao-de-saida)
    - 13.4 [Calculators](#4-calculators---logica-de-calculo-pura)
    - 13.5 [Collection Utilities](#5-collection-utilities---operacoes-em-colecoes)

14. [Design Patterns para IFs Complexos](#design-patterns-para-ifs-complexos)
    - 14.1 [Strategy Pattern](#1-strategy-pattern---multiplos-comportamentos)
    - 14.2 [State Pattern](#2-state-pattern---mudancas-de-estado)
    - 14.3 [Builder Pattern](#3-builder-pattern---construcao-complexa)
    - 14.4 [Chain of Responsibility](#4-chain-of-responsibility---pipeline-de-validacoes)
    - 14.5 [Factory Pattern](#5-factory-pattern---criacao-baseada-em-tipo)
    - 14.6 [Decorator Pattern](#6-decorator-pattern---adicionar-comportamentos)

15. [Evitando IFs Desnecessarios](#evitando-ifs-desnecessarios)
    - 15.1 [Validacoes com Bean Validation](#1-validacoes-com-bean-validation-ao-inves-de-ifs-manuais)
    - 15.2 [Optional ao inves de IF null](#2-optional-ao-inves-de-if-null)
    - 15.3 [Query MongoDB ao inves de IF em memoria](#3-query-mongodb-ao-inves-de-if-em-memoria)
    - 15.4 [Expressoes ternarias](#4-expressoes-ternarias-simples-ao-inves-de-ifelse)
    - 15.5 [Guard Clauses](#5-guard-clauses-em-vez-de-if-aninhados)
    - 15.6 [Spring Data features](#6-spring-data-features-ao-inves-de-if-manualmente)

16. [Constantes vs Enums - Boas Praticas](#constantes-vs-enums---boas-praticas)
    - 16.1 [Quando Usar Enums](#quando-usar-enums)
    - 16.2 [Quando Usar Constantes](#quando-usar-constantes)
    - 16.3 [Padroes de Enum Avancado](#padroes-de-enum-avancado)

18. [Resumo - Decidir Entre Constantes e Enums](#resumo---decidir-entre-constantes-e-enums)

17. [Tamanho de Metodos - Linhas, Complexidade e Quebras](#tamanho-de-metodos---linhas-complexidade-e-quebras)
    - 17.1 [Recomendacoes de Tamanho (Linhas)](#recomendacoes-de-tamanho-linhas)
    - 17.2 [Quando Quebrar em Metodos Menores](#quando-quebrar-em-metodos-menores)
    - 17.3 [Quando NÃO Quebrar em Metodos Menores](#quando-nao-quebrar-em-metodos-menores)
    - 17.4 [Exemplo Prático: Refatoração](#exemplo-prático-refatoração)

19. [Tabela Decisoria: Qual Anotacao Usar?](#tabela-decisoria-qual-anotacao-usar)

20. [Interfaces Funcionais - Consumer, Predicate, Supplier e Outras](#interfaces-funcionais---consumer-predicate-supplier-e-outras)
    - 20.1 [Consumer e BiConsumer - Executar sem Retorno](#1-consumer-e-biconsumer---executar-sem-retorno)
    - 20.2 [Predicate e BiPredicate - Validacoes e Filtros](#2-predicate-e-bipredicate---validacoes-e-filtros)
    - 20.3 [Supplier - Gerar Valores](#3-supplier---gerar-valores)
    - 20.4 [Function e BiFunction - Transformacoes](#4-function-e-bifunction---transformacoes)
    - 20.5 [Quando NÃO Usar Interfaces Funcionais](#5-quando-nao-usar-interfaces-funcionais)
    - 20.6 [Anti-patterns com Interfaces Funcionais](#6-anti-patterns-com-interfaces-funcionais)

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



## UseCase com Regras e Constructor Injection
```java
public class CreateProductUseCase {
    private final ProductRepository repository;
    private final ProductMapper mapper;

    public CreateProductUseCase(ProductRepository repository, ProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public ProductResponse execute(CreateProductRequest request) {
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
public class CreateProductUseCaseBad {
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
    private final CreateProductUseCase createProductUseCase;

    public ProductController(CreateProductUseCase createProductUseCase) {
        this.createProductUseCase = createProductUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        var response = createProductUseCase.execute(request);
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
@DisplayName("CreateProductUseCase - Criar produto")
class CreateProductUseCaseTest {

    @Mock private ProductRepository repository;
    @Mock private ProductMapper mapper;
    @InjectMocks private CreateProductUseCase useCase;

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

        var result = useCase.execute(request);

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



## Spring Annotations - @Bean, @Component, @Service, @Repository

Escolher a anotacao correta melhora legibilidade, testabilidade e clareza de intenção. **Todo UseCase DEVE usar @Service - é o padrão obrigatório para lógica de negócio.**

### @Repository - Acesso a Dados

Use quando a classe é **responsável por persistência** (acesso a banco de dados).

✅ CERTO:
```java
// domain/CustomerRepository.java - Interface
public interface CustomerRepository extends MongoRepository<Customer, String> {
    Optional<Customer> findByEmail(String email);
    List<Customer> findByRegion(String region);
}

// Nao precisa anotacao aqui - Spring Data cuida automaticamente

// infrastructure/persistence/CustomerRepositoryImpl.java - Implementacao customizada (se necessario)
@Repository
public class CustomerRepositoryImpl implements CustomerRepository {
    private final MongoTemplate mongoTemplate;

    public CustomerRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Customer> findByRegion(String region) {
        return mongoTemplate.find(
            query(where("region").is(region)),
            Customer.class
        );
    }
}
```

❌ ERRADO:
```java
// Nao use @Repository em classes que nao acessam dados
@Repository
public class EmailValidator {
    public boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}

// Nao use em Mappers
@Repository
public interface CustomerMapper {
    Customer toEntity(CustomerDto dto);
}
```

### UseCase - Logica de Negocio

**🔑 REGRA OBRIGATÓRIA: Todo UseCase DEVE usar @Service**

Use `@Service` quando a classe representa um **caso de uso específico** com **regras de negócio** e **orquestração**. Esta é a forma padrão e obrigatória para implementar UseCases em projetos Spring.

#### ✅ PADRÃO OBRIGATÓRIO: UseCase com @Service

```java
// application/usecase/RegisterCustomerUseCase.java
@Service
public class RegisterCustomerUseCase {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public RegisterCustomerUseCase(
        CustomerRepository repository,
        CustomerMapper mapper,
        PasswordEncoder passwordEncoder,
        EmailService emailService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // UseCase: registro com validacoes e orquestracao
    public CustomerResponse execute(RegisterRequest request) {
        if (repository.findByEmail(request.email()).isPresent()) {
            throw new DuplicateEmailException(request.email());
        }

        var customer = mapper.toEntity(request);
        customer.setPassword(passwordEncoder.encode(request.password()));
        
        var saved = repository.save(customer);
        emailService.sendWelcomeEmail(saved.email());
        
        return mapper.toResponse(saved);
    }
}

// application/usecase/UpdateCustomerProfileUseCase.java
@Service
public class UpdateCustomerProfileUseCase {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;
    private final AuditService auditService;

    public UpdateCustomerProfileUseCase(
        CustomerRepository repository,
        CustomerMapper mapper,
        AuditService auditService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    // UseCase: atualizar com auditoria
    public CustomerResponse execute(String id, UpdateProfileRequest request) {
        var customer = repository.findById(id)
            .orElseThrow(() -> new CustomerNotFoundException(id));

        var updated = customer.updateProfile(request.name(), request.phone());
        repository.save(updated);
        auditService.log("profile_updated", id);
        
        return mapper.toResponse(updated);
    }
}
```

❌ ERRADO:
```java
// Nao use UseCase para helpers puros
public class PasswordValidator {
    public boolean isStrong(String password) {
        return password.length() >= 8 && password.matches(".*[0-9].*");
    }
}

// Nao use em Converters/Formatters
public class MoneyFormatter {
    public String format(BigDecimal amount) {
        return String.format("R$ %.2f", amount);
    }
}
```

### @Component - Utilitarios e Helpers

Use para **utilitários genéricos** que precisam ser injetáveis mas não são services ou repositories.

✅ CERTO:
```java
// infrastructure/util/PaginationHelper.java
@Component
public class PaginationHelper {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    public Pageable createPageable(int page, int size) {
        if (page < 0) page = 0;
        if (size < 1 || size > MAX_PAGE_SIZE) size = DEFAULT_PAGE_SIZE;
        return PageRequest.of(page, size, Sort.by("createdAt").descending());
    }
}

// Uso em controller
@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService service;
    private final PaginationHelper paginationHelper;

    public CustomerController(CustomerService service, PaginationHelper paginationHelper) {
        this.service = service;
        this.paginationHelper = paginationHelper;
    }

    @GetMapping
    public Page<CustomerResponse> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = paginationHelper.createPageable(page, size);
        return service.findAll(pageable).map(this::toResponse);
    }
}

// infrastructure/event/OrderEventPublisher.java
@Component
public class OrderEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public OrderEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishOrderCreated(Order order) {
        eventPublisher.publishEvent(new OrderCreatedEvent(this, order));
    }
}
```

❌ ERRADO:
```java
// Nao use @Component para classe puros (usar static ou sem Bean)
@Component
public class DateUtils {
    public static String formatDate(LocalDate date) {
        return date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}

// Melhor: classe sem anotacao
public final class DateUtils {
    private DateUtils() {}

    public static String formatDate(LocalDate date) {
        return date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
```

### @Bean - Configuracao e Terceiros

Use em `@Configuration` para **criar instâncias de classes terceiras** ou **configurações complexas**.

✅ CERTO:
```java
// infrastructure/config/SecurityConfig.java
@Configuration
public class SecurityConfig {
    
    // Bean para terceiro (Spring Security)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // Bean para cliente HTTP terceiro
    @Bean
    public RestClient stripeClient(StripeProperties props) {
        return RestClient.builder()
            .baseUrl("https://api.stripe.com")
            .defaultHeader("Authorization", "Bearer " + props.apiKey())
            .build();
    }

    // Bean para converter complexo
    @Bean
    public ObjectMapper objectMapper() {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    // Bean condicional
    @Bean
    @ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true")
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("customers", "orders");
    }
}

// infrastructure/config/MongoConfig.java
@Configuration
public class MongoConfig {
    
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(Arrays.asList(
            new BigDecimalToStringConverter(),
            new StringToBigDecimalConverter()
        ));
    }
}
```

❌ ERRADO:
```java
// Nao use @Bean para sua logica de negocio
@Configuration
public class AppConfig {
    @Bean
    public CustomerService customerService(CustomerRepository repo, CustomerMapper mapper) {
        // Suas classes devem ter @Service, nao @Bean
        return new CustomerService(repo, mapper);
    }
}

// Melhor: use @Service diretamente
@Service
public class CustomerService {
    // ...
}
```

### 🔑 REGRA OBRIGATÓRIA: @Bean para Interfaces

**TODO Bean que implementa uma interface DEVE ser declarado como `@Bean` em `@Configuration`, não como estereótipo (`@Service`, `@Component`, etc).**

Isso garante:
- ✅ Contrato explícito via interface
- ✅ Injeção correta do tipo interface, não implementação
- ✅ Facilita troca de implementações
- ✅ Melhor desacoplamento

✅ CERTO:
```java
// domain/PaymentProcessor.java - Interface
public interface PaymentProcessor {
    PaymentResult process(Payment payment);
}

// infrastructure/payment/StripePaymentProcessor.java - Implementacao
public class StripePaymentProcessor implements PaymentProcessor {
    private final RestClient stripeClient;
    private final PaymentMapper mapper;

    public StripePaymentProcessor(RestClient stripeClient, PaymentMapper mapper) {
        this.stripeClient = stripeClient;
        this.mapper = mapper;
    }

    @Override
    public PaymentResult process(Payment payment) {
        // Logica de processamento
        return mapper.toResult(payment);
    }
}

// infrastructure/config/PaymentConfig.java - Declarar como @Bean
@Configuration
public class PaymentConfig {
    
    @Bean
    public PaymentProcessor paymentProcessor(RestClient stripeClient, PaymentMapper mapper) {
        return new StripePaymentProcessor(stripeClient, mapper);
    }
}

// Uso em UseCase - Injetar via interface
public class ProcessPaymentUseCase {
    private final PaymentProcessor paymentProcessor; // Interface!
    private final OrderRepository orderRepository;

    public ProcessPaymentUseCase(PaymentProcessor paymentProcessor, OrderRepository orderRepository) {
        this.paymentProcessor = paymentProcessor;
        this.orderRepository = orderRepository;
    }

    public PaymentResult execute(Order order) {
        var result = paymentProcessor.process(order.getPayment());
        if (result.isSuccess()) {
            orderRepository.save(order);
        }
        return result;
    }
}
```

❌ ERRADO:
```java
// Nao use @Service ou @Component para classe com interface
@Service
public class StripePaymentProcessor implements PaymentProcessor {
    // Isso quebra a injecao - Spring nao injetaria com PaymentProcessor
}

// Nao faça isso - injete a interface, nao a implementacao
public class ProcessPaymentUseCase {
    private final StripePaymentProcessor processor; // ERRADO - implementacao concreta!
    
    public ProcessPaymentUseCase(StripePaymentProcessor processor) {
        this.processor = processor;
    }
}
```

### Anti-patterns: Quando NÃO Usar

#### ❌ Não misture Responsabilidades em Uma Classe

```java
// ERRADO: Repository + Service + Validacao
@Service
@Repository
public class CustomerBadService {
    @Autowired private MongoTemplate mongoTemplate;

    public void register(CustomerRequest request) {
        // Validacao
        if (request.email() == null) throw new Exception("Email required");
        
        // Persistencia
        mongoTemplate.save(new Customer(request.email(), ...));
        
        // Negocio
        emailService.send(...);
    }
}

// CERTO: Separacao clara com UseCase
public class RegisterCustomerUseCase {
    private final CustomerRepository repository;
    private final EmailService emailService;
    private final CustomerMapper mapper;

    public RegisterCustomerUseCase(
        CustomerRepository repository,
        EmailService emailService,
        CustomerMapper mapper
    ) {
        this.repository = repository;
        this.emailService = emailService;
        this.mapper = mapper;
    }

    public CustomerResponse execute(RegisterRequest request) {
        // Validacao delegada para Bean Validation ou Validator
        var customer = mapper.toEntity(request);
        
        // Persistencia via repository
        var saved = repository.save(customer);
        
        // Negocio
        emailService.sendWelcomeEmail(saved.email());
        
        return mapper.toResponse(saved);
    }
}
```

#### ❌ Nao Use @Bean para Suas Classes (UseCase SEMPRE com @Service)

```java
// ERRADO: @Bean para classe propria que é UseCase
@Configuration
public class AppConfig {
    @Bean
    public RegisterCustomerUseCase registerCustomerUseCase(CustomerRepository repo, CustomerMapper mapper) {
        // UseCase deve SEMPRE ter @Service, nao ser registrado como @Bean
        return new RegisterCustomerUseCase(repo, mapper);
    }
}

// ERRADO: UseCase sem @Service
public class RegisterCustomerUseCase {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public RegisterCustomerUseCase(CustomerRepository repository, CustomerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    
    public CustomerResponse execute(RegisterRequest request) {
        // UseCase logic
    }
}

// ✅ CERTO: UseCase com @Service (obrigatório)
@Service
public class RegisterCustomerUseCase {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public RegisterCustomerUseCase(CustomerRepository repository, CustomerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    
    public CustomerResponse execute(RegisterRequest request) {
        // UseCase logic
    }
}
```

#### ❌ Nao Use Field Injection (@Autowired em Fields)

```java
// ERRADO: Field Injection
public class CreateOrderUseCase {
    @Autowired private OrderRepository repository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProcessPaymentUseCase processPaymentUseCase;
    
    // Dificil de testar, dependencias ocultas
}

// CERTO: Constructor Injection em UseCase
public class CreateOrderUseCase {
    private final OrderRepository repository;
    private final CustomerRepository customerRepository;
    private final ProcessPaymentUseCase processPaymentUseCase;

    public CreateOrderUseCase(
        OrderRepository repository,
        CustomerRepository customerRepository,
        ProcessPaymentUseCase processPaymentUseCase
    ) {
        this.repository = repository;
        this.customerRepository = customerRepository;
        this.processPaymentUseCase = processPaymentUseCase;
    }
}
```

#### ❌ Nao Use @Component para Utilitarios Puros

```java
// ERRADO: @Component para funcoes puras
@Component
public class MathHelper {
    public BigDecimal calculateDiscount(BigDecimal price, int percent) {
        return price.multiply(new BigDecimal(100 - percent).divide(new BigDecimal(100)));
    }
}

// CERTO: Classe estatica ou sem Bean
public final class MathHelper {
    private MathHelper() {}

    public static BigDecimal calculateDiscount(BigDecimal price, int percent) {
        return price.multiply(new BigDecimal(100 - percent).divide(new BigDecimal(100)));
    }
}
```

#### ❌ Nao Use @Service para Mappers

```java
// ERRADO: @Service para Mapper
public class CustomerMapper {
    public Customer toEntity(CustomerDto dto) {
        return new Customer(...);
    }
}

// CERTO: @Mapper do MapStruct
@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toEntity(CustomerDto dto);
    CustomerDto toDto(Customer customer);
}
```

#### ❌ VIOLAÇÃO CRÍTICA: Não use Estereotipos para Classes com Interface

```java
// ERRADO: Usar @Service/Component para classe que implementa interface
public interface PaymentProcessor {
    PaymentResult process(Payment payment);
}

@Service // ERRADO! Viola a regra de interfaces
public class StripePaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentResult process(Payment payment) {
        // ...
    }
}

// Isso causa problema na injecao:
public class CreateOrderUseCase {
    // Spring nao consegue injetar a interface quando usa @Service
    private final PaymentProcessor processor; // Falha ao tentar injetar StripePaymentProcessor como PaymentProcessor
}

// CERTO: Usar @Bean em @Configuration
public interface PaymentProcessor {
    PaymentResult process(Payment payment);
}

public class StripePaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentResult process(Payment payment) {
        // ...
    }
}

@Configuration
public class PaymentConfig {
    @Bean
    public PaymentProcessor paymentProcessor(RestClient client) {
        return new StripePaymentProcessor(client);
    }
}

// Agora funciona perfeitamente:
public class CreateOrderUseCase {
    private final PaymentProcessor processor; // Injecao funciona!
    
    public CreateOrderUseCase(PaymentProcessor processor) {
        this.processor = processor;
    }
}



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



## Streams - Boas Praticas vs Anti-patterns

Streams são fundamentais em Java moderno. Use com limpeza de código (Clean Code principles).

### 1. Filter + Map + Collect

✅ CERTO - Legível e Declarativo:
```java
// Transformar lista de customers para emails
List<String> emails = customers.stream()
    .map(Customer::email)
    .collect(Collectors.toList());

// Filtrar e transformar
List<CustomerResponse> activeEmails = customers.stream()
    .filter(Customer::isActive)
    .map(customerMapper::toResponse)
    .collect(Collectors.toList());

// Mais complexo: agrupar e contar
Map<String, Long> customersByRegion = customers.stream()
    .collect(Collectors.groupingBy(Customer::region, Collectors.counting()));

// Coletar em set
Set<String> uniqueRegions = customers.stream()
    .map(Customer::region)
    .collect(Collectors.toSet());
```

❌ ERRADO - Loops Imperativos (Evite):
```java
// Nao use loops manuais quando pode usar stream
List<String> emails = new ArrayList<>();
for (Customer customer : customers) {
    emails.add(customer.email());
}

// Nao use multiplos loops
List<CustomerResponse> activeEmails = new ArrayList<>();
for (Customer customer : customers) {
    if (customer.isActive()) {
        activeEmails.add(customerMapper.toResponse(customer));
    }
}

// Nao use acumuladores mutaveis
Map<String, Long> customersByRegion = new HashMap<>();
for (Customer customer : customers) {
    String region = customer.region();
    customersByRegion.put(region, customersByRegion.getOrDefault(region, 0L) + 1);
}
```

### 2. FlatMap para Colecoes Aninhadas

✅ CERTO - FlatMap para Niveis Multiplos:
```java
// Extrair todos os itens de pedidos de multiplos clientes
List<OrderItem> allItems = customers.stream()
    .flatMap(customer -> customer.orders().stream())
    .flatMap(order -> order.items().stream())
    .collect(Collectors.toList());

// Mais legível com método helper
List<OrderItem> allItems = customers.stream()
    .flatMap(customer -> getOrderItems(customer))
    .collect(Collectors.toList());

private List<OrderItem> getOrderItems(Customer customer) {
    return customer.orders().stream()
        .flatMap(order -> order.items().stream())
        .collect(Collectors.toList());
}

// Filtrar aninhado
List<OrderItem> paidItems = customers.stream()
    .flatMap(customer -> customer.orders().stream())
    .filter(order -> order.isPaid())
    .flatMap(order -> order.items().stream())
    .collect(Collectors.toList());
```

❌ ERRADO - Loops Aninhados:
```java
// Evite loops aninhados profundos
List<OrderItem> allItems = new ArrayList<>();
for (Customer customer : customers) {
    for (Order order : customer.orders()) {
        for (OrderItem item : order.items()) {
            allItems.add(item);
        }
    }
}

// Nao use break/continue em streams - use filter
for (Customer customer : customers) {
    if (!customer.isActive()) continue; // Evite
    for (Order order : customer.orders()) {
        if (!order.isPaid()) continue;  // Evite
        for (OrderItem item : order.items()) {
            allItems.add(item);
        }
    }
}
```

### 3. Terminal Operations - collect, forEach, reduce

✅ CERTO - Usar Terminal Operations Apropriadas:
```java
// Coletar em Lista
List<String> names = customers.stream()
    .map(Customer::name)
    .collect(Collectors.toList());

// forEach apenas para side-effects (logging, envio)
customers.stream()
    .filter(Customer::isActive)
    .forEach(customer -> emailService.send(customer.email()));

// reduce para agregacao
BigDecimal totalSpent = orders.stream()
    .map(Order::total)
    .reduce(BigDecimal.ZERO, BigDecimal::add);

// findFirst com Optional
Optional<Customer> premium = customers.stream()
    .filter(Customer::isPremium)
    .findFirst();

// anyMatch / allMatch para validacao
boolean hasActiveCustomers = customers.stream()
    .anyMatch(Customer::isActive);

boolean allPaid = orders.stream()
    .allMatch(Order::isPaid);
```

❌ ERRADO - Terminal Operations Ineficientes:
```java
// Nao use forEach para colecionar dados (use collect)
List<String> names = new ArrayList<>();
customers.stream()
    .map(Customer::name)
    .forEach(names::add); // ERRADO - side effect

// Nao colete apenas para iterar novamente
List<String> result = customers.stream()
    .filter(Customer::isActive)
    .collect(Collectors.toList());
result.forEach(System.out::println); // Ineficiente

// Nao use collect quando findFirst/anyMatch bastaria
boolean exists = customers.stream()
    .filter(c -> c.email().equals("test@example.com"))
    .collect(Collectors.toList())
    .size() > 0; // ERRADO

// Use:
boolean exists = customers.stream()
    .anyMatch(c -> c.email().equals("test@example.com")); // Certo
```

### 4. Performance e Lazy Evaluation

✅ CERTO - Entender Lazy Evaluation:
```java
// Streams sao lazy - nada executa ate o terminal operation
var expensive = customers.stream()
    .filter(this::isExpensiveCheck)  // Nao roda ainda
    .map(this::complexTransform)      // Nao roda ainda
    .limit(10);                        // Nao roda ainda
    // .collect() - AQUI executa tudo

// Limite resultado ANTES de transformacoes caras
List<String> topEmails = customers.stream()
    .limit(10)                              // Primeiro limitar
    .map(this::complexTransformation)       // Depois transformar
    .collect(Collectors.toList());

// Use parallelStream para operacoes pesadas (cuidado!)
List<String> processedItems = largeList.parallelStream()
    .filter(item -> item.meets(criteria()))
    .map(item -> expensiveTransformation(item))
    .collect(Collectors.toList());

// Short-circuit operations (param eficiente)
Optional<Customer> first = customers.stream()
    .filter(Customer::isActive)
    .findFirst();  // Para assim que achar
```

❌ ERRADO - Performance Ruim:
```java
// Nao colete sem necessidade
var result = customers.stream()
    .filter(Customer::isActive)
    .collect(Collectors.toList())
    .subList(0, 10);  // ERRADO - coleta todos, depois limita

// Correto:
var result = customers.stream()
    .filter(Customer::isActive)
    .limit(10)
    .collect(Collectors.toList());

// Nao use parallelStream casualmente
var result = smallList.parallelStream()  // Overhead nao vale
    .map(x -> x * 2)
    .collect(Collectors.toList());

// Nao reutilize streams (sao one-shot)
Stream<String> stream = customers.stream()
    .map(Customer::email);
stream.forEach(System.out::println);
stream.forEach(System.out::println);  // ERRO - stream ja consumido
```

### 5. Anti-patterns em Streams

#### ❌ Nao Use Streams Para Side Effects Principais

```java
// ERRADO: Stream como controle de fluxo
customers.stream()
    .filter(Customer::isActive)
    .forEach(customer -> {
        var order = createOrder(customer);
        orderRepository.save(order);
        emailService.send(customer.email());
    });

// CERTO: Use UseCase ou Service para logica de negocio
public class CreateOrdersForActiveCustomersUseCase {
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    public CreateOrdersForActiveCustomersUseCase(
        CustomerRepository customerRepository,
        OrderRepository orderRepository,
        EmailService emailService
    ) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.emailService = emailService;
    }

    public void execute() {
        var activeCustomers = customerRepository.findAllActive();
        
        activeCustomers.forEach(customer -> {
            var order = createOrder(customer);
            orderRepository.save(order);
            emailService.send(customer.email());
        });
    }
}
```

#### ❌ Nao Use Streams Complexos Sem Extracoes

```java
// ERRADO: Stream muito longo e complexo
List<OrderResponse> responses = orders.stream()
    .filter(order -> order.getCustomer().isActive() && order.getTotal().compareTo(BigDecimal.ZERO) > 0)
    .map(order -> {
        var customer = order.getCustomer();
        var items = order.getItems().stream()
            .map(item -> new ItemResponse(item.getId(), item.getName()))
            .collect(Collectors.toList());
        return new OrderResponse(order.getId(), customer.getEmail(), items, order.getTotal());
    })
    .sorted(Comparator.comparing(OrderResponse::getTotal).reversed())
    .collect(Collectors.toList());

// CERTO: Extrair em metodos pequenos e testáveis
List<OrderResponse> responses = orders.stream()
    .filter(this::isValidOrder)
    .map(this::toOrderResponse)
    .sorted(byTotalDescending())
    .collect(Collectors.toList());

private boolean isValidOrder(Order order) {
    return order.getCustomer().isActive() && 
           order.getTotal().compareTo(BigDecimal.ZERO) > 0;
}

private OrderResponse toOrderResponse(Order order) {
    var customer = order.getCustomer();
    var items = order.getItems().stream()
        .map(this::toItemResponse)
        .collect(Collectors.toList());
    return new OrderResponse(order.getId(), customer.getEmail(), items, order.getTotal());
}

private ItemResponse toItemResponse(OrderItem item) {
    return new ItemResponse(item.getId(), item.getName());
}

private Comparator<OrderResponse> byTotalDescending() {
    return Comparator.comparing(OrderResponse::getTotal).reversed();
}
```

#### ❌ Nao Use Null em Streams

```java
// ERRADO: Null dentro de streams
List<String> emails = customers.stream()
    .map(Customer::email)
    .filter(email -> email != null)  // Nao deveria chegar aqui
    .collect(Collectors.toList());

// CERTO: Use Optional + filter
List<String> emails = customers.stream()
    .map(Customer::optionalEmail)    // Retorna Optional<String>
    .filter(Optional::isPresent)
    .map(Optional::get)
    .collect(Collectors.toList());

// Ou melhor ainda:
List<String> emails = customers.stream()
    .map(Customer::optionalEmail)
    .flatMap(Function.identity())  // Optional.stream() em Java 9+
    .collect(Collectors.toList());
```

---



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


## Tamanho de Metodos - Linhas, Complexidade e Quebras

Tamanho de método é crucial para legibilidade, testabilidade e manutenção. Use essas diretrizes:

### Recomendacoes de Tamanho (Linhas)

| Categoria | Linhas Máx | Descrição |
|-----------|-----------|-----------|
| **Métodos Triviais** | 5-10 | Getters, setters, delegação simples |
| **Métodos Simples** | 15-20 | Lógica clara, poucas responsabilidades |
| **Métodos Normais** | 25-30 | Lógica moderada, pode ter múltiplas operações |
| **Métodos Complexos** | 40-50 | Último recurso, considere refatorar |
| **Nunca Exceder** | 100+ | Quebra em múltiplos métodos |

✅ **REGRA DE OURO:**
- **Métodos menores são melhores.** Se você consegue manter <= 20 linhas, faça isso.
- **Cada método = uma responsabilidade** (Single Responsibility Principle)
- **Métodos testáveis** são menores (fácil mockar, fácil afirmar)

### Quando Quebrar em Metodos Menores

**SEMPRE QUEBRE** quando:

#### 1. Múltiplas Responsabilidades
```java
// ERRADO: 45 linhas, múltiplas responsabilidades
public OrderResponse createOrder(CreateOrderRequest request) {
    // Validacao
    if (request.customerId() == null) throw new InvalidCustomerException();
    if (request.items().isEmpty()) throw new EmptyOrderException();
    if (request.items().stream().anyMatch(i -> i.quantity() <= 0)) throw new InvalidQuantityException();
    
    // Buscar customer
    var customer = customerRepository.findById(request.customerId())
        .orElseThrow(() -> new CustomerNotFoundException(request.customerId()));
    
    // Validar estoque
    for (OrderItemRequest item : request.items()) {
        var product = productRepository.findById(item.productId())
            .orElseThrow(() -> new ProductNotFoundException(item.productId()));
        if (product.stock() < item.quantity()) {
            throw new InsufficientStockException(product.id(), item.quantity());
        }
    }
    
    // Criar order
    var order = new Order(customer, request.items().stream()
        .map(itemRequest -> new OrderItem(
            productRepository.findById(itemRequest.productId()).orElseThrow(),
            itemRequest.quantity(),
            itemRequest.price()
        ))
        .collect(Collectors.toList()));
    
    // Atualizar estoque
    request.items().forEach(itemRequest -> {
        var product = productRepository.findById(itemRequest.productId()).orElseThrow();
        productRepository.save(product.decreaseStock(itemRequest.quantity()));
    });
    
    // Salvar e notificar
    var saved = orderRepository.save(order);
    eventPublisher.publishOrderCreated(saved);
    emailService.notifyCustomer(customer.email(), saved);
    
    return orderMapper.toResponse(saved);
}

// CERTO: Quebrado em métodos pequenos e testáveis
public class CreateOrderUseCase {
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;

    public CreateOrderUseCase(
        CustomerRepository customerRepository,
        ProductRepository productRepository,
        OrderRepository orderRepository,
        OrderMapper orderMapper,
        ApplicationEventPublisher eventPublisher,
        EmailService emailService
    ) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.eventPublisher = eventPublisher;
        this.emailService = emailService;
    }

    public OrderResponse execute(CreateOrderRequest request) {
        validateRequest(request);
        var customer = loadCustomer(request.customerId());
        var items = loadAndValidateItems(request.items());
        
        var order = new Order(customer, items);
        var saved = orderRepository.save(order);
        
        notifyAfterOrderCreation(saved, customer.email());
        
        return orderMapper.toResponse(saved);
    }

    private void validateRequest(CreateOrderRequest request) {
        if (request.customerId() == null) throw new InvalidCustomerException();
        if (request.items().isEmpty()) throw new EmptyOrderException();
        if (request.items().stream().anyMatch(i -> i.quantity() <= 0)) {
            throw new InvalidQuantityException();
        }
    }

    private Customer loadCustomer(String customerId) {
        return customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    private List<OrderItem> loadAndValidateItems(List<OrderItemRequest> itemRequests) {
        return itemRequests.stream()
            .map(this::createOrderItem)
            .collect(Collectors.toList());
    }

    private OrderItem createOrderItem(OrderItemRequest request) {
        var product = productRepository.findById(request.productId())
            .orElseThrow(() -> new ProductNotFoundException(request.productId()));
        
        validateStock(product, request.quantity());
        decreaseStock(product, request.quantity());
        
        return new OrderItem(product, request.quantity(), request.price());
    }

    private void validateStock(Product product, int requiredQuantity) {
        if (product.stock() < requiredQuantity) {
            throw new InsufficientStockException(product.id(), requiredQuantity);
        }
    }

    private void decreaseStock(Product product, int quantity) {
        var updated = product.decreaseStock(quantity);
        productRepository.save(updated);
    }

    private void notifyAfterOrderCreation(Order order, String customerEmail) {
        eventPublisher.publishOrderCreated(order);
        emailService.notifyCustomer(customerEmail, order);
    }
}
```

**Benefício:** Cada método <= 15 linhas, responsabilidade única, testável individualmente.

#### 2. Lógica Complexa Aninhada
```java
// ERRADO: 30 linhas com lógica aninhada profunda
public List<CustomerResponse> findEligibleForPromotion() {
    var customers = customerRepository.findAll();
    var result = new ArrayList<CustomerResponse>();
    
    for (Customer customer : customers) {
        if (customer.isActive()) {
            var orders = orderRepository.findByCustomerId(customer.id());
            if (!orders.isEmpty()) {
                var totalSpent = orders.stream()
                    .map(Order::total)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                if (totalSpent.compareTo(new BigDecimal("1000")) > 0) {
                    var lastOrder = orders.stream()
                        .max(Comparator.comparing(Order::createdAt));
                    
                    if (lastOrder.isPresent()) {
                        var daysAgo = ChronoUnit.DAYS.between(lastOrder.get().createdAt(), LocalDateTime.now());
                        if (daysAgo < 90) {
                            result.add(customerMapper.toResponse(customer));
                        }
                    }
                }
            }
        }
    }
    return result;
}

// CERTO: Quebrado com métodos auxiliares
public List<CustomerResponse> findEligibleForPromotion() {
    return customerRepository.findAll().stream()
        .filter(this::isEligibleForPromotion)
        .map(customerMapper::toResponse)
        .collect(Collectors.toList());
}

private boolean isEligibleForPromotion(Customer customer) {
    return customer.isActive() && 
           hasRecentOrders(customer.id()) &&
           hasHighTotalSpent(customer.id());
}

private boolean hasRecentOrders(String customerId) {
    return orderRepository.findByCustomerId(customerId).stream()
        .map(Order::createdAt)
        .anyMatch(this::isWithin90Days);
}

private boolean hasHighTotalSpent(String customerId) {
    var totalSpent = orderRepository.findByCustomerId(customerId).stream()
        .map(Order::total)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return totalSpent.compareTo(new BigDecimal("1000")) > 0;
}

private boolean isWithin90Days(LocalDateTime date) {
    var daysAgo = ChronoUnit.DAYS.between(date, LocalDateTime.now());
    return daysAgo < 90;
}
```

#### 3. Múltiplos Níveis de Abstração
```java
// ERRADO: Mistura low-level e high-level
public void processPayments() {
    var orders = orderRepository.findUnpaidOrders();
    
    for (Order order : orders) {
        try {
            var payment = new Payment(order.id(), order.total());
            var response = stripeClient.post("/charges", payment);
            
            if (response.getStatusCode() == 200) {
                var result = objectMapper.readValue(response.getBody(), ChargeResponse.class);
                
                var orderPayment = new OrderPayment(
                    order.id(),
                    result.getId(),
                    result.getAmount(),
                    LocalDateTime.now()
                );
                orderPaymentRepository.save(orderPayment);
                
                order.markAsPaid();
                orderRepository.save(order);
            }
        } catch (Exception e) {
            log.error("Failed to process payment for order {}", order.id(), e);
        }
    }
}

// CERTO: Separar abstrações
public void processPayments() {
    var unpaidOrders = orderRepository.findUnpaidOrders();
    unpaidOrders.forEach(this::processPayment);
}

private void processPayment(Order order) {
    try {
        var chargeResponse = chargeCustomer(order);
        recordPayment(order, chargeResponse);
        markOrderAsPaid(order);
    } catch (PaymentException e) {
        logPaymentFailure(order, e);
    }
}

private ChargeResponse chargeCustomer(Order order) {
    var payment = new Payment(order.id(), order.total());
    return paymentGateway.charge(payment);
}

private void recordPayment(Order order, ChargeResponse response) {
    var orderPayment = new OrderPayment(
        order.id(),
        response.getId(),
        response.getAmount(),
        LocalDateTime.now()
    );
    orderPaymentRepository.save(orderPayment);
}

private void markOrderAsPaid(Order order) {
    order.markAsPaid();
    orderRepository.save(order);
}

private void logPaymentFailure(Order order, PaymentException e) {
    log.error("Failed to process payment for order {}", order.id(), e);
}
```

### Quando NÃO Quebrar em Metodos Menores

**NÃO QUEBRE** quando:

#### 1. Métodos Triviais (Getters, Delegação)
```java
// OK: Deixe assim, quebrar seria excessivo
public String getEmail() {
    return email;
}

public boolean isActive() {
    return active;
}

public void updatePassword(String newPassword) {
    this.password = passwordEncoder.encode(newPassword);
}

// OK: Simples delegação
public OrderResponse execute(CreateOrderRequest request) {
    return orderService.execute(request);  // Se é só delegação, pode ficar aqui
}
```

#### 2. Construtor de Objeto (Initialization)
```java
// OK: Construção de objeto pode ser longa se necessário
public class User {
    private final String id;
    private final String email;
    private final String name;
    private final LocalDateTime createdAt;
    private final List<Role> roles;
    private final Map<String, String> metadata;

    public User(String email, String name, List<Role> roles) {
        this.id = UUID.randomUUID().toString();
        this.email = email;
        this.name = name;
        this.createdAt = LocalDateTime.now();
        this.roles = roles != null ? new ArrayList<>(roles) : List.of();
        this.metadata = new HashMap<>();
    }
}
```

#### 3. Configuração/Setup em Testes
```java
// OK: Setup em testes pode ser longo
@Test
void shouldCreateOrderWithAllItems() {
    // Setup pode ser 20-30 linhas de inicialização
    var customer = Instancio.of(Customer.class)
        .set(field(Customer::email), "test@example.com")
        .set(field(Customer::isActive), true)
        .create();
    
    var items = List.of(
        Instancio.of(OrderItem.class).create(),
        Instancio.of(OrderItem.class).create()
    );
    
    var request = new CreateOrderRequest(customer.id(), items);
    
    // Act
    var result = useCase.execute(request);
    
    // Assert
    assertThat(result).isNotNull();
}
```

#### 4. Um Fluxo Linear Sem Decisões
```java
// OK: Sequência de passos linear sem lógica condicional
public void setupDatabase() {
    createSchema();
    insertBaseData();
    buildIndexes();
    validateData();
    enableConstraints();
}

// Vs. ERRADO: Misturar inicialização com lógica
public void setupDatabase() {
    createSchema();
    if (isDevelopment()) {
        insertTestData();
    } else {
        insertProductionData();
    }
    buildIndexes();
    if (shouldValidate()) {
        validateData();
    }
    enableConstraints();
    // ... etc
}
```

### Exemplo Prático: Refatoração

**Antes (50 linhas):**
```java
public void importCustomersFromCSV(String filePath) {
    var file = new File(filePath);
    if (!file.exists()) throw new FileNotFoundException(filePath);
    
    try (var reader = new BufferedReader(new FileReader(file))) {
        var customers = new ArrayList<Customer>();
        var line = reader.readLine();
        
        while ((line = reader.readLine()) != null) {
            var parts = line.split(",");
            if (parts.length != 4) continue;
            
            var customer = new Customer(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                LocalDateTime.parse(parts[3].trim())
            );
            
            if (customerRepository.findByEmail(customer.email()).isEmpty()) {
                customers.add(customer);
            }
        }
        
        customerRepository.saveAll(customers);
        
        log.info("Imported {} customers from {}", customers.size(), filePath);
    } catch (IOException e) {
        throw new ImportException("Failed to import customers", e);
    }
}
```

**Depois (10 linhas na classe, resto em métodos auxiliares):**
```java
public void importCustomersFromCSV(String filePath) {
    var file = validateFile(filePath);
    var customers = readCustomersFromCSV(file);
    var newCustomers = filterExisting(customers);
    
    customerRepository.saveAll(newCustomers);
    log.info("Imported {} customers from {}", newCustomers.size(), filePath);
}

private File validateFile(String filePath) {
    var file = new File(filePath);
    if (!file.exists()) throw new FileNotFoundException(filePath);
    return file;
}

private List<Customer> readCustomersFromCSV(File file) {
    try (var reader = new BufferedReader(new FileReader(file))) {
        return reader.lines()
            .skip(1)  // Skip header
            .map(this::parseCustomerLine)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    } catch (IOException e) {
        throw new ImportException("Failed to read CSV file", e);
    }
}

private Optional<Customer> parseCustomerLine(String line) {
    var parts = line.split(",");
    if (parts.length != 4) return Optional.empty();
    
    return Optional.of(new Customer(
        parts[0].trim(),
        parts[1].trim(),
        parts[2].trim(),
        LocalDateTime.parse(parts[3].trim())
    ));
}

private List<Customer> filterExisting(List<Customer> customers) {
    return customers.stream()
        .filter(customer -> customerRepository.findByEmail(customer.email()).isEmpty())
        .collect(Collectors.toList());
}
```

**Benefícios:**
- ✅ Cada método <= 12 linhas
- ✅ Responsabilidade única
- ✅ Fácil de testar (`testParseCustomerLine()`, `testFilterExisting()`)
- ✅ Fácil de manter e modificar

---



## Tabela Decisoria: Qual Anotacao Usar?

| Classe | Anotacao | Razao |
|--------|----------|-------|
| **PaymentProcessor (interface) + StripePaymentProcessor (impl)** | `@Bean` | **REGRA: Todo Bean com interface usa @Bean** |
| **CustomerRepository (Spring Data)** | Nao precisa | Spring Data cria automaticamente |
| **RegisterCustomerUseCase** | Componente | Logica de negocio (caso de uso) |
| **PaginationHelper** | `@Component` | Utilitario reutilizavel |
| **MoneyConverter** | Nao precisa | Classe pura, pode ser static |
| **PasswordEncoder** | `@Bean` | Classe terceira (Spring Security) |
| **RestClient** | `@Bean` | Configuracao complexa |
| **CustomerDto** | Nao precisa | DTO, nao eh componente |
| **Customer (Entidade)** | Nao precisa | Entidade, nao eh componente |
| **OrderEventPublisher** | `@Component` | Utilitario que publica eventos |
| **SendEmailUseCase** | Componente | UseCase de envio de email |





---

## Interfaces Funcionais - Consumer, Predicate, Supplier e Outras

Interfaces funcionais são ferramentas poderosas para escrever código mais limpo e expressivo. Mas como em qualquer ferramenta, seu uso deve ser criterioso.

### 1. Consumer e BiConsumer - Executar sem Retorno

`Consumer<T>` executa uma ação com um valor, sem retornar nada. `BiConsumer<T, U>` recebe dois parâmetros.

#### ✅ CERTO: Consumer para Efeitos Colaterais Simples

```java
// Bom: Enviar email com Consumer
Consumer<String> sendEmail = email -> {
    var emailService = new EmailService();
    emailService.send(email, "Welcome!");
};

sendEmail.accept("user@example.com");

// Bom: BiConsumer para ações com dois valores
BiConsumer<String, String> logUserAction = (userId, action) -> {
    System.out.printf("User %s performed: %s%n", userId, action);
};

logUserAction.accept("123", "login");

// Bom: Consumer em Streams
List<Order> orders = List.of(...);
orders.forEach(order -> {
    auditService.log("order_created", order.id());
    emailService.sendOrderConfirmation(order);
});
```

#### ✅ CERTO: Consumer em Event Listeners

```java
// application/event/OrderEventListener.java
@Component
public class OrderEventListener {
    private final AuditService auditService;
    private final EmailService emailService;

    public OrderEventListener(AuditService auditService, EmailService emailService) {
        this.auditService = auditService;
        this.emailService = emailService;
    }

    // Consumer: processar evento de ordem criada
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        Consumer<Order> processOrder = order -> {
            auditService.log("order_created", order.id());
            emailService.sendOrderConfirmation(order);
            notifyWarehouse(order);
        };
        
        processOrder.accept(event.order());
    }

    private void notifyWarehouse(Order order) {
        // Notificar warehouse
    }
}
```

#### ❌ ERRADO: Consumer para Lógica Complexa

```java
// ERRADO: Consumer com lógica demais (fica ilegível)
Consumer<Order> complexLogic = order -> {
    if (order.total() > 1000) {
        var discount = order.total() * 0.1;
        var newTotal = order.total() - discount;
        if (order.customer().loyaltyPoints() > 100) {
            // Mais validações...
            // Código ilegível em lambda
        }
    }
};

// CERTO: Extrair para método ou UseCase
public void processLargeOrder(Order order) {
    if (order.total() > 1000) {
        var discountedTotal = calculateDiscount(order);
        applyLoyaltyBonus(order);
    }
}
```

### 2. Predicate e BiPredicate - Validacoes e Filtros

`Predicate<T>` retorna boolean. Ideal para validações e filtros.

#### ✅ CERTO: Predicate para Filtros Simples

```java
// Bom: Predicate em stream filters
Predicate<Order> isHighValue = order -> order.total().compareTo(new BigDecimal("1000")) > 0;
Predicate<Order> isPending = order -> order.status() == OrderStatus.PENDING;

List<Order> highValuePendingOrders = orders.stream()
    .filter(isHighValue)
    .filter(isPending)
    .collect(Collectors.toList());

// Bom: Compor Predicates
Predicate<Customer> isActive = customer -> customer.isActive();
Predicate<Customer> isPremium = customer -> customer.tier() == CustomerTier.PREMIUM;
Predicate<Customer> isActiveAndPremium = isActive.and(isPremium);

List<Customer> premiumCustomers = customers.stream()
    .filter(isActiveAndPremium)
    .collect(Collectors.toList());
```

#### ✅ CERTO: BiPredicate para Comparações

```java
// Bom: BiPredicate para validar relacoes
BiPredicate<LocalDateTime, LocalDateTime> isWithinRange = 
    (startDate, endDate) -> startDate.isBefore(endDate);

BiPredicate<BigDecimal, BigDecimal> isPriceValid = 
    (price, costPrice) -> price.compareTo(costPrice) > 0;

// Uso
if (isWithinRange.test(order.createdAt(), order.deliveredAt())) {
    // Ordem entregue no prazo
}

if (isPriceValid.test(product.salePrice(), product.costPrice())) {
    // Preço venda > preço custo
}
```

#### ✅ CERTO: Validador com Predicate

```java
// infrastructure/validator/OrderValidator.java
@Component
public class OrderValidator {
    
    public boolean isValid(Order order) {
        return hasValidItems(order)
            .and(hasValidCustomer(order))
            .and(hasSufficientStock(order))
            .test(order);
    }

    private Predicate<Order> hasValidItems(Order order) {
        return o -> !o.items().isEmpty() && 
                    o.items().stream().allMatch(item -> item.quantity() > 0);
    }

    private Predicate<Order> hasValidCustomer(Order order) {
        return o -> o.customer() != null && o.customer().isActive();
    }

    private Predicate<Order> hasSufficientStock(Order order) {
        return o -> o.items().stream()
                     .allMatch(item -> item.product().stock() >= item.quantity());
    }
}
```

#### ❌ ERRADO: Predicate com Side Effects

```java
// ERRADO: Predicate com efeitos colaterais (quebra princípios funcionais)
Predicate<Order> invalidPredicate = order -> {
    auditService.log("checking_order", order.id()); // Side effect!
    emailService.send(order.customer().email(), "Checking..."); // Side effect!
    return order.total().compareTo(BigDecimal.ZERO) > 0;
};

// CERTO: Separar validação de efeito colateral
Predicate<Order> hasPositiveTotal = order -> order.total().compareTo(BigDecimal.ZERO) > 0;

orders.stream()
    .filter(hasPositiveTotal)
    .forEach(order -> auditService.log("checking_order", order.id()));
```

### 3. Supplier - Gerar Valores

`Supplier<T>` gera um valor sem receber parâmetros. Útil para lazy initialization e factory patterns.

#### ✅ CERTO: Supplier para Lazy Initialization

```java
// Bom: Supplier para criar valores sob demanda
Supplier<DatabaseConnection> connectionSupplier = () -> new DatabaseConnection("jdbc:mysql://localhost");

// Conexão só é criada quando chamado
DatabaseConnection conn = connectionSupplier.get();

// Bom: Supplier em Optional
Optional.ofNullable(order)
    .orElseGet(() -> Order.empty());

// Bom: Supplier com configuração complexa
Supplier<RestClient> restClientSupplier = () -> RestClient.builder()
    .baseUrl("https://api.example.com")
    .defaultHeader("Authorization", "Bearer " + System.getenv("API_KEY"))
    .build();
```

#### ✅ CERTO: Factory com Supplier

```java
// infrastructure/factory/PaymentProcessorFactory.java
@Component
public class PaymentProcessorFactory {
    private final Map<PaymentMethod, Supplier<PaymentProcessor>> suppliers;

    public PaymentProcessorFactory(
        StripePaymentProcessor stripe,
        PayPalPaymentProcessor paypal,
        Pix2PaymentProcessor pix
    ) {
        this.suppliers = Map.of(
            PaymentMethod.STRIPE, () -> stripe,
            PaymentMethod.PAYPAL, () -> paypal,
            PaymentMethod.PIX, () -> pix
        );
    }

    public PaymentProcessor getProcessor(PaymentMethod method) {
        return suppliers.getOrDefault(
            method, 
            () -> { throw new PaymentMethodNotSupportedException(method); }
        ).get();
    }
}
```

#### ✅ CERTO: Supplier em Configuration

```java
// infrastructure/config/ApplicationConfig.java
@Configuration
public class ApplicationConfig {
    
    @Bean
    public Supplier<LocalDateTime> systemTime() {
        return LocalDateTime::now; // Supplier para hora atual
    }
    
    @Bean
    public Supplier<UUID> uuidGenerator() {
        return UUID::randomUUID; // Supplier para gerar IDs
    }
}

// Uso em UseCase
@Service
public class CreateOrderUseCase {
    private final Supplier<UUID> uuidGenerator;
    private final Supplier<LocalDateTime> systemTime;

    public CreateOrderUseCase(Supplier<UUID> uuidGenerator, Supplier<LocalDateTime> systemTime) {
        this.uuidGenerator = uuidGenerator;
        this.systemTime = systemTime;
    }

    public OrderResponse execute(CreateOrderRequest request) {
        var order = new Order(
            uuidGenerator.get(),  // Gera novo ID
            request.customerId(),
            systemTime.get(),      // Pega hora atual
            request.items()
        );
        return orderRepository.save(order);
    }
}
```

#### ❌ ERRADO: Supplier para Lógica Determinística

```java
// ERRADO: Supplier desnecessário para valor constante
Supplier<String> appName = () -> "MyApp";
var name = appName.get(); // Overcomplicated!

// CERTO: Constante simples
public static final String APP_NAME = "MyApp";
var name = APP_NAME;
```

### 4. Function e BiFunction - Transformacoes

`Function<T, R>` transforma um valor de tipo T em tipo R.

#### ✅ CERTO: Function para Transformacoes

```java
// Bom: Function em streams
Function<Order, OrderResponse> toResponse = order -> OrderResponse.builder()
    .id(order.id())
    .customerId(order.customerId())
    .total(order.total())
    .status(order.status())
    .build();

List<OrderResponse> responses = orders.stream()
    .map(toResponse)
    .collect(Collectors.toList());

// Bom: Function chainada
Function<String, String> trim = String::trim;
Function<String, String> uppercase = String::toUpperCase;
Function<String, String> normalize = trim.andThen(uppercase);

var result = normalize.apply("  hello  "); // "HELLO"
```

#### ✅ CERTO: BiFunction para Operações com Dois Valores

```java
// Bom: BiFunction para cálculos
BiFunction<BigDecimal, Integer, BigDecimal> applyDiscount = 
    (price, discountPercent) -> price.multiply(
        BigDecimal.valueOf(100 - discountPercent)
            .divide(BigDecimal.valueOf(100))
    );

var discountedPrice = applyDiscount.apply(new BigDecimal("100"), 10); // 90

// Bom: BiFunction em map reduce
BiFunction<BigDecimal, Order, BigDecimal> addOrderTotal = 
    (accumulator, order) -> accumulator.add(order.total());

BigDecimal totalSales = orders.stream()
    .reduce(BigDecimal.ZERO, addOrderTotal, BigDecimal::add);
```

### 5. Quando NÃO Usar Interfaces Funcionais

#### ❌ ERRADO: Interfaces Funcionais para Lógica Complexa

```java
// ERRADO: Lambda gigante (ilegível)
Function<Order, OrderResponse> complexTransform = order -> {
    var items = order.items().stream()
        .filter(item -> item.quantity() > 0)
        .map(item -> new OrderItemResponse(
            item.productId(),
            item.product().name(),
            item.quantity(),
            item.unitPrice(),
            item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()))
        ))
        .collect(Collectors.toList());
    
    var discountedTotal = order.total().multiply(BigDecimal.valueOf(0.9));
    var taxes = discountedTotal.multiply(BigDecimal.valueOf(0.18));
    var finalTotal = discountedTotal.add(taxes);
    
    return new OrderResponse(order.id(), items, finalTotal, order.status());
};

// CERTO: Extrair para método na classe
public class OrderResponseMapper {
    public OrderResponse toResponse(Order order) {
        var items = mapItems(order.items());
        var total = calculateTotal(order, items);
        return new OrderResponse(order.id(), items, total, order.status());
    }
    
    private List<OrderItemResponse> mapItems(List<OrderItem> items) {
        return items.stream()
            .filter(item -> item.quantity() > 0)
            .map(this::toItemResponse)
            .collect(Collectors.toList());
    }
    
    private BigDecimal calculateTotal(Order order, List<OrderItemResponse> items) {
        var discounted = order.total().multiply(BigDecimal.valueOf(0.9));
        var taxes = discounted.multiply(BigDecimal.valueOf(0.18));
        return discounted.add(taxes);
    }
}
```

#### ❌ ERRADO: Interfaces Funcionais para Regras de Negócio

```java
// ERRADO: Regras de negócio complexas em lambda
Predicate<Order> shouldProcess = order -> {
    var daysOld = ChronoUnit.DAYS.between(order.createdAt(), LocalDateTime.now());
    if (daysOld > 30) return false;
    
    if (order.customer().tier() == CustomerTier.BRONZE && order.total().compareTo(new BigDecimal("50")) < 0) {
        return false;
    }
    
    return true;
};

// CERTO: UseCase dedicado
@Service
public class ValidateOrderProcessingUseCase {
    private final OrderRepository orderRepository;

    public boolean shouldProcess(String orderId) {
        var order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        return !isOrderTooOld(order) && meetsMinimumRequirements(order);
    }

    private boolean isOrderTooOld(Order order) {
        var daysOld = ChronoUnit.DAYS.between(order.createdAt(), LocalDateTime.now());
        return daysOld > 30;
    }

    private boolean meetsMinimumRequirements(Order order) {
        return !(order.customer().tier() == CustomerTier.BRONZE 
                 && order.total().compareTo(new BigDecimal("50")) < 0);
    }
}
```

### 6. Anti-patterns com Interfaces Funcionais

#### ❌ ERRADO: Interfaces Funcionais com State Mutável

```java
// ERRADO: Lambda alterando variável externa
List<Integer> numbers = List.of(1, 2, 3, 4, 5);
List<Integer> doubled = new ArrayList<>();
numbers.forEach(n -> doubled.add(n * 2)); // Mutando doubled!

// CERTO: Usar streams e collect
List<Integer> doubled = numbers.stream()
    .map(n -> n * 2)
    .collect(Collectors.toList());
```

#### ❌ ERRADO: Lambdas Muito Longas

```java
// ERRADO: Lambda que virou método (ilegível)
orders.forEach(order -> {
    var items = order.items();
    if (items.isEmpty()) return;
    
    var total = BigDecimal.ZERO;
    for (var item : items) {
        total = total.add(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
    }
    
    if (total.compareTo(new BigDecimal("1000")) > 0) {
        emailService.sendHighValueNotification(order.customer().email(), total);
        auditService.log("high_value_order", order.id());
    }
});

// CERTO: Extrair para UseCase
@Service
public class ProcessHighValueOrdersUseCase {
    private final EmailService emailService;
    private final AuditService auditService;

    public void execute(List<Order> orders) {
        orders.stream()
            .filter(this::isHighValue)
            .forEach(this::notifyAndAudit);
    }

    private boolean isHighValue(Order order) {
        return order.calculateTotal().compareTo(new BigDecimal("1000")) > 0;
    }

    private void notifyAndAudit(Order order) {
        emailService.sendHighValueNotification(order.customer().email(), order.calculateTotal());
        auditService.log("high_value_order", order.id());
    }
}
```

#### ❌ ERRADO: Ignorar Exceções em Lambdas

```java
// ERRADO: Exceções silenciosas em forEach
orders.forEach(order -> {
    try {
        processOrder(order);
    } catch (Exception e) {
        // Silenciando erro! Péssima prática
    }
});

// CERTO: Mapear erro ou retornar Result
orders.stream()
    .map(this::safeProcessOrder)
    .filter(result -> result.isSuccess())
    .forEach(result -> handleSuccess(result));

private Result<Order> safeProcessOrder(Order order) {
    try {
        return Result.success(processOrder(order));
    } catch (Exception e) {
        return Result.failure(e);
    }
}
```

### Tabela Decisória: Qual Interface Funcional Usar?

| Interface | Parâmetros | Retorno | Caso de Uso |
|-----------|-----------|---------|-----------|
| **Consumer<T>** | 1 | Nenhum | Efeitos colaterais, logging, eventos |
| **BiConsumer<T,U>** | 2 | Nenhum | Operações com dois valores |
| **Predicate<T>** | 1 | Boolean | Filtros, validações |
| **BiPredicate<T,U>** | 2 | Boolean | Comparações, validações compostas |
| **Supplier<T>** | 0 | T | Lazy init, factories, geradores |
| **Function<T,R>** | 1 | R | Transformações, mapeamentos |
| **BiFunction<T,U,R>** | 2 | R | Operações, cálculos |
| **UnaryOperator<T>** | 1 | T | Transformações mesmo tipo |
| **BinaryOperator<T>** | 2 | T | Operações mesmo tipo |
