# GitHub Copilot Instructions

Backend e-commerce REST API: **Spring Boot 4 + Java 25 + MongoDB 7.0** com **DDD** (domain/application/infrastructure/web).

## Idioma
- Responder e comentar preferencialmente em **Portuguese (pt-BR)**.
- Use English apenas quando o solicitante pedir explicitamente.

## Arquitetura DDD
- `domain/`: Entidades, Value Objects, Repository interfaces (core puro)
- `application/`: Services, Business Logic (depende só de domain)
- `infrastructure/`: MongoDB, Mappers, Configs
- `web/`: Controllers, DTOs, Exception Handlers
- **Regra**: Controllers → Services → Repositories (nunca pular camadas)

## Java 25 Features
- **Records** (não Lombok): `public record Product(@Id String id, String name) {}`
- **Pattern Matching**: `switch(status) { case PENDING -> "..."; }`
- **Sequenced Collections**: Use `list.getFirst()` e `list.getLast()`
- **Text Blocks**: Queries MongoDB com `"""`

## Spring Boot 4
- **Constructor Injection** obrigatório (não `@Autowired` em fields)
- **RestClient** (não RestTemplate): `restClient.post().uri("/api").retrieve()`
- **ProblemDetail** (RFC 7807): Para respostas de erro padronizadas
- **@Observed**: Métricas com Micrometer

## MongoDB Best Practices
- **Embedding**: Dados fortemente acoplados (Address dentro de Customer)
- **Referencing**: Agregados independentes (customerId em Order)
- **Snapshots**: Dados históricos (priceAtPurchase em OrderItem)
- **TTL**: `@Indexed(expireAfter = "7d")` para dados temporários (Cart)
- **Indexes**: `@Indexed` em campos pesquisados (sku, category)

## Testes
- **JUnit 6 + Instancio**: `Instancio.create(Product.class)` para fixtures
- **Testcontainers**: MongoDB real em testes de integração
- **@DisplayName**: Descrições legíveis dos testes
- **Mockito**: `@Mock`, `@InjectMocks`, `@ExtendWith(MockitoExtension.class)`

### Test Generator Agents

Este projeto possui **2 agentes especializados** para geração de testes:

#### 1. Unit Test Agent
Para testes unitários rápidos (sem Spring context):
- **Ativação**: `@workspace gere teste unitário para {ClassName}`
- **Uso**: Domain entities, Value Objects, Mappers, Services (com Mockito)
- **Características**: Rápidos (ms), mocks, sem infraestrutura
- **Referência**: [agents/unit-test.md](agents/unit-test.md)

#### 2. Integration Test Agent
Para testes de integração com infraestrutura real:
- **Ativação**: `@workspace gere teste de integração para {ClassName}`
- **Uso**: Repositories (MongoDB), Controllers (MockMvc), fluxos E2E
- **Características**: Testcontainers, Spring context, RFC 7807
- **Referência**: [agents/integration-test.md](agents/integration-test.md)

**Guias complementares**:
- [docs/instancio-best-practices.md](../docs/instancio-best-practices.md) - Uso de Instancio + Faker
- [docs/testing.md](../docs/testing.md) - Estratégia geral de testes
- [docs/test-generation-agent.md](../docs/test-generation-agent.md) - Especificação detalhada

## Segurança
- **RBAC**: `@PreAuthorize("hasRole('ADMIN')")` nos endpoints
- **BCrypt**: Sempre hashear senhas com `passwordEncoder.encode()`
- **JWT**: Validação em todos endpoints protegidos
- **DTOs**: Nunca expor entidades com dados sensíveis

## REST API
- **Status HTTP**: 201 Created, 200 OK, 204 No Content, 404 Not Found
- **Paginação**: `Page<ProductDto> findAll(Pageable pageable)`
- **Bean Validation**: `@NotBlank`, `@NotNull`, `@Email` nos DTOs
- **Validação fora do construtor**: Prefira Bean Validation com anotações; não coloque validação manual em construtores (classes ou records)
- **Naming**: `GET /api/products`, `POST /api/products`, `DELETE /api/products/{id}`

## Clean Code
- Nomes descritivos (não abreviações)
- Métodos pequenos (< 20 linhas)
- Código auto-explicativo (evitar comentários óbvios)
- Separar lógica complexa em métodos privados

## Code Review Checklist
✅ DDD: Camadas corretas, controllers não têm lógica de negócio
✅ Java 25: Records, pattern matching, sequenced collections
✅ Spring: Constructor injection, RestClient, ProblemDetail
✅ MongoDB: Indexes, embedding/referencing correto, snapshots
✅ Testes: mínimo 90% cobertura (ideal 95%), Instancio, Testcontainers
✅ Segurança: @PreAuthorize, BCrypt, sem dados sensíveis em DTOs
✅ API: Status HTTP corretos, paginação, Bean Validation

## GitHub Copilot Review Agent
Este repositório possui um agente de revisão configurado que:
- ✅ **Lê comentários do PR** durante a revisão para contextualizar feedback
- ✅ **Considera discussões existentes** para evitar duplicação de feedback
- ✅ **Valida conformidade** com os padrões do projeto automaticamente
- ✅ **Prioriza segurança** e funcionalidade antes de estilo

Configuração: [agents/review.md](agents/review.md) | [agents/review-config.yml](agents/review-config.yml)

Ver detalhes completos em: [CONTRIBUTING.md](../CONTRIBUTING.md) | [Plan](../plan/README.md)
