# Pull Request

## Descrição
<!-- Descreva as mudanças implementadas -->

## Tipo de Mudança
- [ ] 🐛 Bug fix (correção de problema)
- [ ] ✨ Nova feature (funcionalidade)
- [ ] 📝 Documentação
- [ ] ♻️ Refatoração
- [ ] ⚡ Performance
- [ ] ✅ Testes
- [ ] 🔧 Configuração

## Checklist de Implementação

### ✅ Arquitetura e Código
- [ ] Código segue DDD com separação clara de camadas (domain/application/infrastructure/web)
- [ ] Entidades de domínio são **Records** imutáveis (sem Lombok)
- [ ] Repositórios estendem `MongoRepository<Entity, String>`
- [ ] Services usam **constructor injection** (não @Autowired em campos)
- [ ] Controllers são `@RestController` com endpoints RESTful
- [ ] DTOs usam MapStruct para conversão (não mapeamento manual)
- [ ] Validações Bean Validation nos DTOs (@NotNull, @NotBlank, @Email, etc.)
- [ ] Exceções customizadas herdam de `RuntimeException`
- [ ] Código não usa Lombok (preferir Records nativos do Java 25)

### 🧪 Testes
- [ ] Testes unitários com **JUnit 6 (Jupiter)** e `@DisplayName` descritivo
- [ ] Testes usam **Instancio** para criar fixtures (`Instancio.create()`)
- [ ] Mocks com **Mockito** (`@Mock`, `@InjectMocks`, `@ExtendWith(MockitoExtension.class)`)
- [ ] Testes de integração com **Testcontainers** e `@EnabledIfEnvironmentVariable`
- [ ] Cobertura mínima de 80% nas classes de negócio
- [ ] Testes validam cenários de sucesso E falha
- [ ] Testes de repositório verificam queries MongoDB reais

### 🗄️ MongoDB
- [ ] Entidades anotadas com `@Document(collection = "nome")`
- [ ] IDs como `String` (não ObjectId exposto)
- [ ] Campos pesquisáveis têm `@Indexed`
- [ ] Usa **embedding** para dependências fortes (Address dentro de Customer)
- [ ] Usa **referencing** para relações N:N ou agregados independentes
- [ ] Snapshots implementados para dados históricos (preços em Order)
- [ ] TTL index configurado para dados temporários (Cart expira em 7 dias)
- [ ] Queries com `@Query` otimizadas (projeções quando necessário)
- [ ] Timestamps com `@CreatedDate` e `@LastModifiedDate`

### 🔒 Segurança
- [ ] Endpoints protegidos com `@PreAuthorize` apropriado
- [ ] Roles RBAC: `ROLE_CUSTOMER`, `ROLE_MANAGER`, `ROLE_ADMIN`
- [ ] Senhas hasheadas com BCrypt (nunca texto plano)
- [ ] JWT tokens validados em todos os endpoints protegidos
- [ ] Dados sensíveis não expostos nos DTOs públicos
- [ ] Validação de autorização no service layer

### 🌐 API REST
- [ ] Endpoints seguem convenções REST (GET /products, POST /orders, etc.)
- [ ] Status HTTP corretos (200 OK, 201 Created, 204 No Content, 400 Bad Request, 404 Not Found)
- [ ] Paginação com `Pageable` para listas grandes
- [ ] Responses usam DTOs (nunca entidades de domínio diretamente)
- [ ] Tratamento de exceções com `@RestControllerAdvice`
- [ ] Documentação OpenAPI/Swagger atualizada

### 📚 Documentação
- [ ] JavaDoc em métodos públicos complexos
- [ ] README atualizado se houver mudanças em setup
- [ ] Diagramas atualizados em `plan/architecture-diagrams.md` se necessário
- [ ] CHANGELOG.md atualizado com a mudança

## Exemplos de Código

### ✅ CERTO - Entity com Record
```java
@Document(collection = "products")
public record Product(
    @Id String id,
    @Indexed String sku,
    String name,
    BigDecimal price,
    Integer stock,
    @CreatedDate LocalDateTime createdAt
) {}
```

### ❌ ERRADO - Entity com Lombok
```java
@Document
@Data // NÃO USAR
@AllArgsConstructor
public class Product {
    private String id;
    private String name;
}
```

### ✅ CERTO - Service com Constructor Injection
```java
@Service
public class ProductService {
    private final ProductRepository repository;
    private final ProductMapper mapper;
    
    public ProductService(ProductRepository repository, ProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
}
```

### ❌ ERRADO - Field Injection
```java
@Service
public class ProductService {
    @Autowired // NÃO USAR
    private ProductRepository repository;
}
```

### ✅ CERTO - Teste com Instancio
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

## Links Relacionados
- Issue: #
- Documentação: [plan/README.md](../plan/README.md)
- Boas práticas: [CONTRIBUTING.md](../CONTRIBUTING.md) | [Copilot Instructions](../.github/instructions.md) | [VS Code Copilot](../.vscode/copilot-instructions.md)

## Screenshots (se aplicável)
<!-- Adicione prints se for mudança visual -->
