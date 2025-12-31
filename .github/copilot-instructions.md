# GitHub Copilot — Instruções Rápidas (pt-BR)

Este arquivo é um resumo curto e acionável para agentes Copilot (VS Code / GitHub) ao trabalhar neste repositório.
Use o conteúdo de `.github/instructions.md` e `.vscode/copilot-instructions.md` para contexto adicional.

## Objetivo
- Priorizar correções seguras e compatíveis com DDD e com as práticas do projeto.
- Propor mudanças pequenas e triviais como PRs; para mudanças arquiteturais, abrir uma issue primeiro.

## Padrões essenciais (resumido)
- Arquitetura: DDD 4 camadas — `domain` → `application` → `infrastructure` → `web`. **Não pular camadas.**
- Java 25: **use Records** (não Lombok), pattern matching, text blocks.
- Spring Boot 4: **Constructor injection** obrigatório; use **RestClient** (não RestTemplate); **ProblemDetail** para erros.
- MongoDB: prefira **embedding** para dados fortemente acoplados e **referencing** para agregados independentes; use `@Indexed`, TTL (`@Indexed(expireAfter = ...)`) quando aplicável.
- Segurança: **@PreAuthorize** para RBAC, BCrypt para senhas, JWT para autenticação.

## Testes e CI
- Testes: **JUnit 6** + **Instancio** para fixtures; use **Testcontainers** (ex.: *MongoDBContainer*) para tests de integração (`src/integrationTest`).
- Sempre use o Gradle wrapper: `./gradlew build` / `./gradlew test` / `./gradlew integrationTest`.
- Pre-commit hook: valida `./gradlew -q checkIntegrationTestNames` — garanta que testes de integração terminem com `IT`.

## Boas práticas para PRs gerados por agentes
- Gerar descrição do PR clara (resumo, motivação, como testar). Arquivo de template: `.github/PULL_REQUEST_TEMPLATE.md`.
- Incluir testes para qualquer comportamento novo (unitários e, quando necessário, integração com Testcontainers).
- Para mudanças de API, atualizar exemplos de DTO e adicionar validações com Bean Validation (`@NotBlank`, `@NotNull`, `@Email`).
- Seguir Code Review Checklist: arquitetura DDD, Java 25 features, segurança, índices de MongoDB, cobertura de testes (mínimo 90%).

## Exemplos (copiar/adaptar)
- Record entity:
```java
@Document("products")
public record Product(@Id String id, @Indexed String sku, String name, BigDecimal price) {}
```
- Service (constructor injection):
```java
@Service
public class ProductService {
  private final ProductRepository repo;
  public ProductService(ProductRepository repo) { this.repo = repo; }
}
```
- Controller com segurança e validação:
```java
@PostMapping
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<ProductDto> create(@Valid @RequestBody CreateProductRequest req) { ... }
```

## Onde encontrar contexto adicional (leia antes de agir)
- Padrões e regras: `.github/instructions.md`, `.vscode/copilot-instructions.md`
- Agentes / review: `.github/agents/review.md`, `.github/agents/review-config.yml`
- Test generation guide: `docs/test-generation-agent.md`
- Pre-commit hook: `.githooks/pre-commit`
- Integração: `docker-compose.yml` (Sonar), `src/integrationTest` (Testcontainers)

## Qualidade de Código & Ferramentas
- SonarQube: há um serviço configurado em `docker-compose.yml` (use `docker-compose up sonarqube` para rodar localmente). Repare em `docs/sonar-local.md` para passos adicionais e como enviar análise local se necessário.
- SpotBugs / Checkstyle: regras e exclusões estão em `config/spotbugs/` e `config/checkstyle/` — garanta que as verificações locais passem com `./gradlew check`.
- Pré-commit: o hook (`.githooks/pre-commit`) roda `./gradlew -q checkIntegrationTestNames` — corrija nomes de testes de integração que não terminam com `IT`.
- Geração de descrição de PR: o Copilot gera descrições automaticamente (config: `.github/copilot-pr-description.yml`) ou use o comando de paleta: **"Copilot: Generate Pull Request Description"**.
- CI: verifique que checks de Sonar, SpotBugs e cobertura passem antes de solicitar merge.

## Restrições e avisos
- Evite fazer mudanças arquitetônicas sem abrir issue e discutir com mantenedores.
- Não substituir revisões humanas — agente dá sugestões que devem ser validadas por um revisor.
- Usar Português (pt-BR) por padrão em mensagens e PR descriptions a menos que solicitado em inglês.

---

Se quiser, eu posso reduzir/formatar este documento em um checklist automático para ser usado por agentes de revisão (ex.: `checklist.json`) — quer que eu gere isso agora?