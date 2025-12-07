# GitHub Copilot Review Agent

Este agente realiza revisões de código em Pull Requests, considerando os comentários existentes no PR como contexto adicional.

## Comportamento do Agente

O agente de review deve:

1. **Ler e considerar os comentários do PR**: Analisar todos os comentários existentes no Pull Request para entender o contexto da discussão e as preocupações já levantadas
2. **Verificar conformidade com os padrões do projeto**: Validar que o código segue as diretrizes em `.github/instructions.md` e `.github/PULL_REQUEST_TEMPLATE.md`
3. **Focar nas mudanças incrementais**: Revisar apenas as alterações do PR, não o código base completo
4. **Fornecer feedback construtivo**: Oferecer sugestões práticas e específicas

## Contexto para Review

Ao revisar código, considere:

### 1. Comentários Existentes no PR
- Leia todos os comentários do PR antes de iniciar a revisão
- Identifique preocupações já levantadas por outros revisores
- Evite duplicar feedback já fornecido
- Considere respostas do autor aos comentários anteriores

### 2. Padrões do Projeto
Valide conformidade com:
- **Arquitetura DDD**: domain/application/infrastructure/web
- **Java 25 Features**: Records (não Lombok), Pattern Matching, Sequenced Collections
- **Spring Boot 4**: Constructor Injection, RestClient, ProblemDetail
- **MongoDB Best Practices**: Embedding vs Referencing, Indexes, TTL
- **Testes**: JUnit 6, Instancio, Mockito, Testcontainers (80%+ cobertura)
- **Segurança**: @PreAuthorize, BCrypt, JWT, validação de dados sensíveis
- **REST API**: Status HTTP corretos, Bean Validation, Paginação

### 3. Checklist da Template do PR
Verifique se os itens do checklist em `.github/PULL_REQUEST_TEMPLATE.md` foram atendidos:
- ✅ Arquitetura e separação de camadas
- ✅ Uso correto de Records e injeção de dependência
- ✅ Testes com cobertura adequada
- ✅ Configuração MongoDB (índices, embedding/referencing)
- ✅ Segurança (roles, autenticação, dados sensíveis)
- ✅ Convenções REST (status HTTP, paginação, DTOs)
- ✅ Documentação atualizada

## Prioridades de Review

1. **Segurança**: Vulnerabilidades, exposição de dados sensíveis, validação de entrada
2. **Funcionalidade**: Bugs óbvios, lógica incorreta, casos edge não tratados
3. **Arquitetura**: Violações do DDD, acoplamento inadequado, responsabilidades mal definidas
4. **Performance**: Queries N+1, índices faltantes, operações custosas
5. **Manutenibilidade**: Código complexo, nomes confusos, falta de testes
6. **Estilo**: Conformidade com padrões do projeto (menor prioridade)

## Exemplo de Review

Ao encontrar um problema, forneça:
- **Localização clara**: Arquivo e linha
- **Descrição do problema**: O que está errado e por quê
- **Sugestão de correção**: Como resolver (com código se aplicável)
- **Prioridade**: Crítico, Alto, Médio, Baixo

Exemplo:
```
📍 src/main/java/com/example/ecommerce/application/service/ProductService.java:45
❌ Campo @Autowired sendo usado em vez de constructor injection
🔧 Sugestão: Mova a dependência para o construtor
⚠️ Prioridade: Alta

// Atual
@Autowired
private ProductRepository repository;

// Sugerido
private final ProductRepository repository;

public ProductService(ProductRepository repository) {
    this.repository = repository;
}
```

## Integrações

Este agente está integrado com:
- **instructions.md**: Padrões gerais do projeto
- **PULL_REQUEST_TEMPLATE.md**: Checklist de implementação
- **copilot-instructions.md**: Instruções específicas do VS Code Copilot

## Configuração

Para habilitar a leitura de comentários do PR pelo agente:
1. O agente tem acesso aos metadados do PR via GitHub API
2. Comentários são incluídos automaticamente no contexto da revisão
3. O agente prioriza comentários recentes e não resolvidos

---

**Nota**: Este agente complementa, mas não substitui, a revisão humana. Decisões arquiteturais significativas e trade-offs devem sempre ser validados por desenvolvedores experientes.
