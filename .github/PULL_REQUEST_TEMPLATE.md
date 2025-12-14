# Pull Request

<!-- 
💡 DICA: Use o GitHub Copilot para gerar a descrição automaticamente!

Para gerar uma descrição completa do PR com Copilot:
1. Abra o VS Code
2. Pressione Cmd+I (Mac) ou Ctrl+I (Windows/Linux) para abrir o Copilot Chat
3. Digite: "@workspace gere uma descrição detalhada deste PR incluindo: resumo das mudanças, arquivos modificados, motivação, impacto e exemplos de código se relevante"
4. Ou use o comando: "Copilot: Generate Pull Request Description" na paleta de comandos
5. Cole a descrição gerada abaixo

Alternativamente, no GitHub:
1. Clique em "Copilot" no canto superior direito da caixa de descrição
2. Selecione "Generate description"
3. Revise e ajuste conforme necessário
-->

## Descrição
<!-- Descreva as mudanças implementadas (ou use Copilot para gerar automaticamente) -->

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
- [ ] README atualizado se houver mudanças em setup
- [ ] Diagramas atualizados em `plan/architecture-diagrams.md` se necessário
- [ ] CHANGELOG.md atualizado com a mudança

## Links Relacionados
- Issue: #
- Documentação: [plan/README.md](../plan/README.md)
- Boas práticas: [CONTRIBUTING.md](../CONTRIBUTING.md) | [Copilot Instructions](../.github/instructions.md) | [VS Code Copilot](../.vscode/copilot-instructions.md)
- Exemplos práticos: [plan/code-examples.md](../plan/code-examples.md)

## Screenshots (se aplicável)
<!-- Adicione prints se for mudança visual -->
