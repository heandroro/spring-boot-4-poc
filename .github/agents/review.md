# GitHub Copilot Review Agent

Este agente realiza revisões de código em Pull Requests, considerando os comentários existentes no PR como contexto adicional.

## Comportamento do Agente

O agente de review deve:

1. **Responder em Português (pt-BR)**: Todos os comentários, sugestões e feedback devem ser fornecidos em português
2. **Ler e considerar os comentários do PR**: Analisar todos os comentários existentes no Pull Request para entender o contexto da discussão e as preocupações já levantadas
3. **Verificar conformidade com os padrões do projeto**: Validar que o código segue as diretrizes em `.github/instructions.md` e `.github/PULL_REQUEST_TEMPLATE.md`
4. **Focar nas mudanças incrementais**: Revisar apenas as alterações do PR, não o código base completo
5. **Fornecer feedback construtivo**: Oferecer sugestões práticas e específicas

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
- **Testes**: JUnit 5 (Jupiter), Instancio, Mockito, Testcontainers (80%+ cobertura)
- **Segurança**: @PreAuthorize, BCrypt, JWT, validação de dados sensíveis
- **REST API**: Status HTTP corretos, Bean Validation, Paginação

### 2.1 Arquivos Markdown - Review SELETIVO
**Revisar apenas quando:**
- ✅ Arquivos técnicos críticos: `README.md`, `CONTRIBUTING.md`, `docs/api.md`, `docs/security.md`
- ✅ Documentação de configuração: `.github/instructions.md`

**Focar em:**
- 🔗 Links quebrados
- 🔒 Senhas/tokens expostos em exemplos
- 📝 Sintaxe incorreta de código em blocos
- 🚫 Comandos desatualizados ou caminhos errados

**NÃO revisar:**
- ❌ Arquivos de planejamento (`plan/*.md`)
- ❌ Diagramas e roadmaps
- ❌ CHANGELOG e histórico
- ❌ Documentação interna/estratégias

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

## 🌐 Idioma - IMPORTANTE

**O agente deve comentar em Português (pt-BR)** por padrão. Isso garante:
- ✅ Consistência com a documentação do projeto (toda em português)
- ✅ Melhor compreensão pela equipe brasileira/portuguesa
- ✅ Alinhamento com comentários do `instructions.md`
- ✅ Padrão definido em `review-config.yml` com `language: "pt-BR"`

**Excepção**: Se um usuário explicitamente pedir feedback em inglês (ex: "Please review in English"), o agente deve mudar para inglês apenas para aquele PR.

Exemplos de feedback em português são fornecidos em `.github/agents/EXAMPLE.md`.

## Configuração

Para habilitar a leitura de comentários do PR pelo agente:
1. O agente deve ter acesso aos metadados do PR via GitHub API
2. Comentários devem ser incluídos no contexto da revisão
3. O agente deve priorizar comentários recentes e não resolvidos

**Nota**: As capacidades descritas neste documento representam o comportamento desejado
para o agente de review. A implementação real depende das features disponíveis no
GitHub Copilot. Esta documentação serve como especificação de requisitos.

---

**Nota**: Este agente complementa, mas não substitui, a revisão humana. Decisões arquiteturais significativas e trade-offs devem sempre ser validados por desenvolvedores experientes.
