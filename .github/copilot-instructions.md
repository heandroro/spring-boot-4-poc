# Instruções do GitHub Copilot para este Repositório

## 🌐 IDIOMA - REGRA FUNDAMENTAL

**SEMPRE responda, comente e gere código em Português Brasileiro (pt-BR)**, incluindo:
- ✅ Reviews de Pull Requests
- ✅ Descrições de PR
- ✅ Comentários inline de código
- ✅ Sugestões de melhorias
- ✅ Mensagens de commit
- ✅ Respostas no chat

**Exceção**: Use inglês APENAS se o usuário solicitar explicitamente ("Please review in English").

## 📚 Arquivos de Referência Obrigatórios

Antes de gerar qualquer sugestão ou review, SEMPRE consulte estes arquivos:

1. **[.github/instructions.md](.github/instructions.md)** - Padrões gerais do projeto
2. **[.github/PULL_REQUEST_TEMPLATE.md](.github/PULL_REQUEST_TEMPLATE.md)** - Checklist de implementação
3. **[.github/agents/review.md](.github/agents/review.md)** - Diretrizes de review
4. **[CONTRIBUTING.md](../CONTRIBUTING.md)** - Guia de contribuição completo
5. **[.github/instructions/no-fqn.instructions.md](.github/instructions/no-fqn.instructions.md)** - Proibição de nomes totalmente qualificados
6. **[.github/instructions/no-comments.instructions.md](.github/instructions/no-comments.instructions.md)** - Proibição de comentários em classes

## 🏗️ Contexto do Projeto

**Stack Tecnológica:**
- **Backend**: Spring Boot 4 + Java 25 + MongoDB 7.0
- **Arquitetura**: Domain-Driven Design (DDD) com 4 camadas
- **Testes**: JUnit 6, Instancio, Mockito, Testcontainers

**Estrutura de Camadas DDD:**
```
src/main/java/com/example/poc/
├── domain/         # Entidades, Value Objects, Repository interfaces (core puro)
├── application/    # Services, Business Logic (depende só de domain)
├── infrastructure/ # MongoDB, Mappers, Configs
└── web/           # Controllers, DTOs, Exception Handlers
```

## ✅ Padrões Obrigatórios do Projeto

### 1. Java 25 (NÃO usar Lombok)
- **Records** imutáveis: `public record Product(@Id String id, String name) {}`
- **Pattern Matching**: `switch(status) { case PENDING -> "..."; }`
- **Sequenced Collections**: `list.getFirst()`, `list.getLast()`
- **Text Blocks** para queries: `"""SELECT * FROM..."""`

### 2. Spring Boot 4
- **Constructor Injection** obrigatório (NUNCA `@Autowired` em fields)
- **RestClient** (não RestTemplate)
- **ProblemDetail** (RFC 7807) para erros
- **@Observed** para métricas com Micrometer

### 3. MongoDB Best Practices
- **Embedding**: Dados fortemente acoplados (Address dentro de Customer)
- **Referencing**: Agregados independentes (customerId em Order)
- **Snapshots**: Dados históricos (priceAtPurchase em OrderItem)
- **TTL**: `@Indexed(expireAfter = "7d")` para dados temporários (Cart)
- **Indexes**: `@Indexed` em todos os campos pesquisados (sku, category)

### 4. Testes (Cobertura mínima 90%, ideal 95%)
- **JUnit 6**: `@DisplayName` descritivo em português
- **Instancio**: `Instancio.create(Product.class)` para fixtures
- **Mockito**: `@Mock`, `@InjectMocks`, `@ExtendWith(MockitoExtension.class)`
- **Testcontainers**: MongoDB real em testes de integração

### 5. Segurança
- **RBAC**: `@PreAuthorize("hasRole('ADMIN')")` em todos endpoints protegidos
- **BCrypt**: Sempre hashear senhas com `passwordEncoder.encode()`
- **JWT**: Validação obrigatória em endpoints protegidos
- **DTOs**: NUNCA expor entidades de domínio com dados sensíveis

### 6. API REST
- **Status HTTP corretos**: 201 Created, 200 OK, 204 No Content, 404 Not Found
- **Paginação**: `Page<ProductDto> findAll(Pageable pageable)`
- **Bean Validation**: `@NotBlank`, `@NotNull`, `@Email` nos DTOs
- **Naming**: `GET /api/products`, `POST /api/products`, `DELETE /api/products/{id}`

## 🚫 PROIBIÇÕES ABSOLUTAS

### 1. Nomes Totalmente Qualificados (FQN)
❌ **NUNCA USE**: `com.example.poc.domain.Product`
✅ **SEMPRE USE**: `import com.example.poc.domain.Product;` + `Product`

**Razão**: Checkstyle bloqueia FQNs. Ver [.github/instructions/no-fqn.instructions.md](.github/instructions/no-fqn.instructions.md)

### 2. Comentários em Classes
❌ **NUNCA ADICIONE**:
- Javadocs (`/** ... */`)
- Comentários de linha (`// ...`)
- Comentários de bloco (`/* ... */`)

**Razão**: Código deve ser autoexplicativo. Documentação vai em `docs/` ou `CONTRIBUTING.md`.
Ver [.github/instructions/no-comments.instructions.md](.github/instructions/no-comments.instructions.md)

### 3. Lombok
❌ **NUNCA USE**: `@Data`, `@Getter`, `@Setter`, `@Builder`, `@AllArgsConstructor`
✅ **SEMPRE USE**: Java Records nativos

### 4. Field Injection
❌ **NUNCA USE**:
```java
@Autowired
private ProductRepository repository;
```

✅ **SEMPRE USE**:
```java
private final ProductRepository repository;

public ProductService(ProductRepository repository) {
    this.repository = repository;
}
```

## 📋 Checklist de Review (Prioridades)

Ao revisar Pull Requests, verifique nesta ordem:

1. **🔒 SEGURANÇA** (Prioridade Crítica)
   - Vulnerabilidades de segurança
   - Exposição de dados sensíveis
   - Falta de `@PreAuthorize` em endpoints protegidos
   - Senhas não hasheadas

2. **🐛 FUNCIONALIDADE** (Prioridade Alta)
   - Bugs óbvios
   - Lógica incorreta
   - Casos edge não tratados
   - Falta de validação de entrada

3. **🏗️ ARQUITETURA** (Prioridade Alta)
   - Violações do DDD (controller chamando repository diretamente)
   - Acoplamento inadequado
   - Responsabilidades mal definidas
   - Field injection em vez de constructor injection

4. **⚡ PERFORMANCE** (Prioridade Média)
   - Queries N+1
   - Índices MongoDB faltantes
   - Operações custosas sem otimização

5. **🧪 TESTES** (Prioridade Média)
   - Cobertura < 90%
   - Testes faltando para cenários críticos
   - Testes sem `@DisplayName` descritivo

6. **💅 ESTILO** (Prioridade Baixa)
   - Conformidade com padrões do projeto
   - Nomes de variáveis/métodos confusos

## 📝 Formato de Feedback (SEMPRE EM PORTUGUÊS)

Ao encontrar problemas, forneça feedback neste formato:

```markdown
📍 **Localização**: src/main/java/com/example/poc/application/service/ProductService.java:45
❌ **Problema**: Campo @Autowired sendo usado em vez de constructor injection
🔧 **Sugestão**: Mova a dependência para o construtor
⚠️ **Prioridade**: Alta

**Código Atual**:
@Autowired
private ProductRepository repository;

**Código Sugerido**:
private final ProductRepository repository;

public ProductService(ProductRepository repository) {
    this.repository = repository;
}
```

## 🎯 Exemplo de Review Completo em Português

```markdown
## 📊 Resumo da Revisão

✅ **Pontos Positivos**:
- Boa separação de camadas DDD
- Uso correto de Records do Java 25
- Cobertura de testes adequada (92%)

⚠️ **Problemas Encontrados**:

### 🔒 Segurança (CRÍTICO)
1. Endpoint `/api/products` sem `@PreAuthorize` → Exposto publicamente
2. Senha em `UserDto` não está sendo hasheada → Usar `BCryptPasswordEncoder`

### 🏗️ Arquitetura (ALTO)
3. `ProductController.java:45` → Controller chamando `repository` diretamente → Usar `ProductService`
4. `OrderService.java:78` → Field injection com `@Autowired` → Mudar para constructor injection

### ⚡ Performance (MÉDIO)
5. `ProductRepository.findByCategory()` → Campo `category` sem `@Indexed` → Adicionar índice

### 🧪 Testes (MÉDIO)
6. `ProductServiceTest.java` → Faltam testes para cenário de erro (produto duplicado)

## 🔄 Próximos Passos
1. Corrigir problemas críticos de segurança (itens 1-2)
2. Ajustar violações arquiteturais (itens 3-4)
3. Adicionar índice MongoDB (item 5)
4. Completar cobertura de testes (item 6)
```

## 🌟 Exemplo de Sugestão de Código

Quando sugerir melhorias, forneça código completo e funcional:

```java
// ❌ Código Atual (com problemas)
@RestController
public class ProductController {
    @Autowired
    private ProductRepository repository;
    
    @GetMapping("/api/products")
    public List<Product> getAll() {
        return repository.findAll();
    }
}

// ✅ Código Sugerido (correto)
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;
    private final ProductMapper mapper;
    
    public ProductController(ProductService service, ProductMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ProductDto>> getAll(Pageable pageable) {
        var products = service.findAll(pageable);
        var dtos = products.map(mapper::toDto);
        return ResponseEntity.ok(dtos);
    }
}
```

## 📖 Documentação Adicional

Para mais detalhes, consulte:
- **Exemplos práticos**: [plan/code-examples.md](../plan/code-examples.md)
- **Diagramas de arquitetura**: [plan/architecture-diagrams.md](../plan/architecture-diagrams.md)
- **Estratégia de testes**: [docs/testing.md](../docs/testing.md)
- **Boas práticas Instancio**: [docs/instancio-best-practices.md](../docs/instancio-best-practices.md)

---

**🎯 LEMBRE-SE**: O objetivo é manter código limpo, seguro, testável e aderente aos padrões DDD com Java 25 e Spring Boot 4. SEMPRE responda em Português (pt-BR) e priorize segurança acima de tudo.
