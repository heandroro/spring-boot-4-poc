# Boas Práticas para Criação de Agentes GitHub Copilot

## Índice
1. [Visão Geral](#visão-geral)
2. [Estrutura de um Agente](#estrutura-de-um-agente)
3. [Limites e Restrições](#limites-e-restrições)
4. [Padrões de Nomenclatura](#padrões-de-nomenclatura)
5. [Organização do Conteúdo](#organização-do-conteúdo)
6. [Boas Práticas](#boas-práticas)
7. [Anti-Padrões](#anti-padrões)
8. [Exemplos](#exemplos)
9. [Checklist de Criação](#checklist-de-criação)

---

## Visão Geral

Agentes GitHub Copilot são arquivos markdown especializados que definem comportamentos, regras e padrões para o Copilot seguir em contextos específicos. Eles permitem modularizar instruções e criar "especialistas" para diferentes tarefas.

### Quando Criar um Agente

✅ **Crie um agente quando:**
- Há um conjunto específico de regras/padrões para uma tarefa
- As instruções ocupariam > 500 linhas no `instructions.md`
- A tarefa requer templates ou exemplos extensos
- Você quer especialização (review, test generation, refactoring)
- Precisa de contexto que não cabe nas instruções principais

❌ **Não crie um agente quando:**
- As regras cabem em < 100 linhas
- É uma configuração one-off sem reuso
- Pode ser documentado diretamente no código
- Já existe um agente similar que pode ser estendido

---

## Estrutura de um Agente

### Template Básico

```markdown
# {Nome do Agente}

Descrição concisa do propósito (1-2 frases).

## Ativação

Este agente é ativado quando:
- Condição 1 (ex: criar arquivo *Test.java)
- Condição 2 (ex: comentário "@agent {nome}")
- Condição 3 (ex: comando específico)

## Responsabilidades

1. ✅ Responsabilidade principal
2. ✅ Responsabilidade secundária
3. ✅ Responsabilidade terciária

## {Seção Principal}

Conteúdo detalhado com templates, exemplos, regras.

### Subseção 1

...

### Subseção 2

...

## Regras Obrigatórias

- ✅ Regra 1
- ✅ Regra 2
- ❌ Anti-padrão 1
- ❌ Anti-padrão 2

## Checklist

- [ ] Item verificável 1
- [ ] Item verificável 2

## Referências

- Link para documentação relacionada
- Link para exemplos no projeto
- Link para specs externas
```

### Seções Obrigatórias

1. **Título** (H1): Nome claro e descritivo
2. **Descrição**: Propósito em 1-2 frases
3. **Ativação**: Quando/como o agente é usado
4. **Responsabilidades**: O que o agente faz
5. **Conteúdo Principal**: Regras, templates, exemplos
6. **Referências**: Links para contexto adicional

### Seções Opcionais (mas Recomendadas)

- **Stack/Ferramentas**: Tecnologias usadas
- **Padrões Obrigatórios**: Regras que devem ser seguidas
- **Anti-Padrões**: O que evitar
- **Checklist**: Validação de saída
- **Exemplos Reais**: Código do projeto

---

## Limites e Restrições

### Tamanho do Arquivo

| Categoria | Limite | Motivo |
|-----------|--------|--------|
| **Ideal** | 300-800 linhas | Fácil de ler e manter |
| **Aceitável** | 800-1500 linhas | Ainda gerenciável |
| **Máximo** | 2000 linhas | Próximo do limite de contexto |
| **Crítico** | > 2000 linhas | Considere dividir em sub-agentes |

**Contexto do Copilot:**
- O Copilot tem limite de contexto (~8k-32k tokens dependendo do modelo)
- Agentes muito grandes podem ser truncados
- Priorize informação essencial no início do arquivo

### Tamanho de Seções

| Elemento | Limite Recomendado | Observação |
|----------|-------------------|------------|
| **Descrição inicial** | 2-3 frases | Seja conciso |
| **Exemplo de código** | 50-100 linhas | Use `...` para omitir |
| **Lista de regras** | 10-15 itens | Agrupe em categorias |
| **Template completo** | 200 linhas | Omita código repetitivo |

### Profundidade de Hierarquia

```markdown
# Agente (H1) - Título principal
## Seção (H2) - Categorias principais
### Subseção (H3) - Detalhes
#### Detalhe (H4) - Use com moderação
```

⚠️ **Evite > 4 níveis** de hierarquia (H1-H4 é suficiente)

---

## Padrões de Nomenclatura

### Nome do Arquivo

```
.github/agents/{nome}-{versão}.md
```

**Exemplos:**
- `review.md` - Agente de code review
- `test-generator.md` - Geração de testes
- `refactor.md` - Refatorações
- `security-audit.md` - Auditoria de segurança

**Regras:**
- Lowercase com hífens
- Sem espaços ou underscores
- Descritivo e específico
- Sem versão no nome (use git tags)

### Título do Agente

```markdown
# {Verbo} {Objeto} Agent
```

**Exemplos:**
- `# Test Generator Agent`
- `# Code Review Agent`
- `# Security Audit Agent`
- `# API Documentation Agent`

---

## Organização do Conteúdo

### Princípio da Pirâmide Invertida

```
1. Mais Importante (Topo)
   - Descrição
   - Ativação
   - Responsabilidades principais

2. Detalhes Importantes (Meio)
   - Templates
   - Exemplos
   - Regras principais

3. Contexto Adicional (Base)
   - Anti-padrões
   - Casos edge
   - Referências
```

### Estrutura de Templates

```markdown
### Template: {Nome}

**Quando usar:**
- Cenário 1
- Cenário 2

**Código:**
```java
// Template com placeholders {claros}
public class {ClassName} {
    // ... código essencial
}
```

**Notas:**
- Ponto importante 1
- Ponto importante 2
```

### Formatação de Regras

```markdown
## Regras Obrigatórias

### ✅ Faça

```java
// ✅ CORRETO - Explicação
código.correto();
```

### ❌ Não Faça

```java
// ❌ ERRADO - Por que evitar
codigo.errado();
```
```

---

## Boas Práticas

### 1. Clareza e Objetividade

✅ **Bom:**
```markdown
Use `Instancio.of(Class.class)` para gerar fixtures de teste.
```

❌ **Ruim:**
```markdown
Você pode talvez considerar usar a biblioteca Instancio, que é uma ferramenta...
```

### 2. Exemplos Concretos

✅ **Bom:**
```markdown
```java
Customer customer = Instancio.of(Customer.class)
    .set(field(Customer::name), "John Doe")
    .create();
```
```

❌ **Ruim:**
```markdown
Crie um customer usando Instancio com os campos necessários.
```

### 3. Use Emojis com Moderação

✅ **Adequado:**
- ✅ Indica ação correta
- ❌ Indica anti-padrão
- ⚠️ Alerta importante
- 🎯 Objetivo/meta

❌ **Excessivo:**
- 🚀💡🔥✨ Sobrecarrega visualmente

### 4. Links Relativos

✅ **Bom:**
```markdown
Ver [testing.md](../../docs/testing.md)
```

❌ **Ruim:**
```markdown
Ver https://github.com/user/repo/blob/main/docs/testing.md
```

### 5. Versionamento por Git

- Use branches/tags para versões
- Não inclua histórico de mudanças no arquivo
- Mantenha changelog separado se necessário

### 6. Modularidade

Se o agente crescer muito:

```
.github/agents/
├── review.md                    # Agente principal
├── review/
│   ├── security-checklist.md   # Sub-módulo
│   ├── performance-rules.md    # Sub-módulo
│   └── style-guide.md          # Sub-módulo
```

---

## Anti-Padrões

### ❌ 1. Agente Monolítico

```markdown
# Ultimate All-Purpose Agent

Este agente faz TUDO:
- Reviews
- Tests
- Refactoring
- Documentation
- Security
- Performance
... (3000 linhas)
```

**Problema:** Muito contexto, dilui especialização.

**Solução:** Divida em agentes específicos.

---

### ❌ 2. Duplicação de Instruções Principais

```markdown
# Test Generator Agent

## Java 25 Features
- Records...
- Pattern Matching...
(repetindo instructions.md)
```

**Problema:** Duplica instruções gerais.

**Solução:** Foque no específico, referencie instruções principais.

---

### ❌ 3. Falta de Estrutura

```markdown
# Agent

Aqui estão algumas coisas importantes...
E também isso...
Ah, e não esqueça...
```

**Problema:** Dificulta leitura e navegação.

**Solução:** Use hierarquia clara (H2, H3).

---

### ❌ 4. Excesso de Abstração

```markdown
Siga os princípios SOLID, DRY, KISS e aplique padrões GOF apropriados.
```

**Problema:** Muito vago.

**Solução:** Exemplos concretos do projeto.

---

### ❌ 5. Código Incompleto ou Quebrado

```markdown
```java
public class Example {
    // TODO: implementar
}
```
```

**Problema:** Não é executável, não ajuda.

**Solução:** Código completo e funcional, ou use `...` explicitamente.

---

## Exemplos

### Exemplo 1: Agente Pequeno e Focado

**Arquivo:** `.github/agents/commit-message.md`

```markdown
# Commit Message Agent

Gera mensagens de commit seguindo Conventional Commits.

## Ativação

Quando o desenvolvedor comitar código.

## Formato

```
<type>(<scope>): <subject>

<body>

<footer>
```

## Tipos Permitidos

- **feat**: Nova funcionalidade
- **fix**: Correção de bug
- **docs**: Documentação
- **refactor**: Refatoração

## Exemplo

```
feat(customer): add credit limit validation

Implement business rule to prevent negative credit limits.
Credit limit must be > 0 and <= 1,000,000.

Closes #123
```

## Referências

- [Conventional Commits](https://www.conventionalcommits.org/)
```

**Tamanho:** ~30 linhas  
**Avaliação:** ✅ Excelente - Focado, claro, conciso

---

### Exemplo 2: Agente Médio com Templates

**Arquivo:** `.github/agents/test-generator.md`

- Seções: Ativação, Stack, Imports, Templates por camada
- Templates: Domain, Infrastructure, Web
- Exemplos: Code snippets reais
- Checklist: Validação de output

**Tamanho:** ~350 linhas  
**Avaliação:** ✅ Bom - Estruturado, completo, gerenciável

---

### Exemplo 3: Agente Grande que Deve ser Dividido

**Arquivo:** `.github/agents/mega-agent.md` (hipotético)

- Reviews de código (500 linhas)
- Geração de testes (400 linhas)
- Refatorações (300 linhas)
- Documentação (200 linhas)
- Total: 1400 linhas

**Avaliação:** ⚠️ Dividir em 4 agentes específicos

---

## Checklist de Criação

### Antes de Criar

- [ ] Verifiquei que não existe agente similar
- [ ] As regras não cabem em `instructions.md` (> 300 linhas)
- [ ] Defini escopo claro e específico
- [ ] Identifiquei condições de ativação

### Durante a Criação

- [ ] Título descritivo e único
- [ ] Descrição concisa (1-2 frases)
- [ ] Seção "Ativação" clara
- [ ] Lista de responsabilidades (3-7 itens)
- [ ] Templates com código completo e executável
- [ ] Exemplos do projeto real (quando possível)
- [ ] Regras obrigatórias claramente marcadas
- [ ] Anti-padrões documentados
- [ ] Links para referências
- [ ] Checklist de validação

### Após Criação

- [ ] Testei o agente com prompt real
- [ ] Verifiquei tamanho (< 2000 linhas idealmente)
- [ ] Validei que código compila
- [ ] Adicionei referência em `instructions.md`
- [ ] Documentei em `agents/README.md`
- [ ] Commitei com mensagem descritiva

---

## Integração com Instructions.md

### Referência Mínima

```markdown
## {Categoria}

Para {tarefa específica}, use o agente especializado:
- **Ativação**: `@workspace {comando}`
- **Referência**: [agents/{nome}.md](agents/{nome}.md)
- **Guia Completo**: [docs/{nome}-guide.md](../docs/{nome}-guide.md)
```

### Exemplo Real

```markdown
## Testes

### Test Generator Agent
Para geração de testes unitários, use o agente especializado:
- **Ativação**: `@workspace gere testes para {ClassName}`
- **Padrões**: Method references, Instancio + Faker
- **Referência**: [agents/test-generator.md](agents/test-generator.md)
```

---

## Métricas de Qualidade

### Agente de Alta Qualidade

- ✅ < 800 linhas
- ✅ Hierarquia clara (H1-H3)
- ✅ 3+ exemplos concretos
- ✅ Código executável
- ✅ Checklist de validação
- ✅ Referências atualizadas
- ✅ Testado e funcional

### Sinais de Alerta

- ⚠️ > 1500 linhas
- ⚠️ Código incompleto/quebrado
- ⚠️ Instruções vagas
- ⚠️ Sem exemplos
- ⚠️ Duplica `instructions.md`
- ⚠️ Não foi testado

---

## Performance: Templates Inline vs Separados

### Como o Copilot Lê Agentes

O Copilot precisa fazer chamadas de ferramenta para ler arquivos:

```
1 leitura inline:  agents/test-generator.md (800 linhas) → 100ms, 2000 tokens
5 leituras separadas: agent.md + 4 templates → 600ms, 2500 tokens
```

**Conclusão:** Templates inline são **5-10x mais rápidos**.

### Estratégia Híbrida Recomendada

#### ✅ Mantenha Inline (Performance)

**Templates pequenos/médios:**
- **< 50 linhas**: Sempre inline
- **50-200 linhas**: Inline se usados em 1 agente
- **Exemplo**: Test templates, snippets, regras

```markdown
### Template: Domain Entity Test

```java
@Test
@DisplayName("Deve criar customer válido")
void shouldCreateValidCustomer() {
    // Template completo aqui (50 linhas)
}
```
```

**Quando manter inline:**
- Template específico de 1 agente
- Alta frequência de uso (chamado sempre)
- Template não compartilhado
- Agente total < 2000 linhas

#### 📁 Separe em Arquivos (Reuso)

**Templates grandes/compartilhados:**
- **> 200 linhas**: Separar em arquivo
- **Múltiplos agentes**: Compartilhar template
- **Exemplo**: Spring Security config completa

```
.github/agents/
├── test-generator.md       # Agente principal
├── review.md               # Agente de review
└── templates/
    ├── security-test.md    # Template compartilhado (300 linhas)
    └── integration-test.md # Template compartilhado (400 linhas)
```

**Referência no agente:**
```markdown
### Template: Integration Test

Para testes de integração completos, veja o template detalhado:
- [templates/integration-test.md](templates/integration-test.md)

**Resumo (inline):**
```java
@SpringBootTest
@Testcontainers
class IntegrationTest {
    // Versão resumida aqui (20 linhas)
}
```
```

**Quando separar:**
- Template > 200 linhas
- Usado por 2+ agentes
- Baixa frequência (edge cases)
- Agente ficaria > 2000 linhas

### Estrutura Otimizada

```
.github/agents/
├── test-generator.md                    # 800 linhas (templates inline)
├── review.md                            # 600 linhas (regras inline)
├── refactor.md                          # 400 linhas (patterns inline)
└── templates/                           # Apenas compartilhados
    ├── testcontainers-setup.md         # Usado por 3 agentes
    └── security-integration-test.md    # Usado por 2 agentes
```

### Métricas de Decisão

| Critério | Inline | Separado |
|----------|--------|----------|
| **Tamanho** | < 200 linhas | > 200 linhas |
| **Uso** | 1 agente | 2+ agentes |
| **Frequência** | Alta (sempre) | Baixa (edge case) |
| **Agente total** | < 2000 linhas | > 2000 linhas |
| **Performance** | ⚡ Rápido | 🐢 Lento |
| **Reuso** | ❌ Duplicação | ✅ Compartilhado |

### Exemplo: Projeto Atual

**test-generator.md atual: 350 linhas**
- ✅ Templates inline (5 templates × 30-50 linhas)
- ✅ Performance otimizada (1 leitura)
- ✅ Developer-friendly (vê tudo)
- ✅ < 2000 linhas (confortável)

**Não precisa separar** - está no tamanho ideal!

Se crescer para > 1500 linhas:
```
.github/agents/
├── test-generator.md           # 400 linhas (core)
└── templates/
    ├── domain-test.md          # 250 linhas
    ├── integration-test.md     # 400 linhas
    └── mockmvc-test.md         # 450 linhas
```

---

## Manutenção

### Quando Atualizar

- Nova versão de ferramenta/framework
- Feedback de uso real (agente não funciona bem)
- Novos padrões no projeto
- Correção de bugs em templates

### Versionamento

Use git tags para versões importantes:

```bash
git tag -a agents/test-generator-v1.0 -m "Release test generator v1.0"
git push origin agents/test-generator-v1.0
```

### Deprecação

Se um agente não é mais útil:

1. Adicione aviso no topo:
   ```markdown
   > ⚠️ **DEPRECATED**: Este agente foi substituído por {novo-agente.md}
   ```

2. Mantenha por 1-2 releases

3. Remova e documente no CHANGELOG

---

## Ferramentas Úteis

### Validação de Tamanho

```bash
# Contar linhas
wc -l .github/agents/test-generator.md

# Contar tokens (aproximado)
wc -w .github/agents/test-generator.md | awk '{print $1 * 1.3}'
```

### Verificação de Links

```bash
# Verificar links quebrados
find .github/agents -name "*.md" -exec markdown-link-check {} \;
```

### Formatação

```bash
# Prettier para markdown
npx prettier --write ".github/agents/*.md"
```

---

## Referências

### Documentação Oficial
- [GitHub Copilot Extensibility](https://github.blog/changelog/2024-05-21-github-copilot-extensibility-now-in-public-preview/)
- [Model Context Protocol](https://modelcontextprotocol.io/)

### Agentes Existentes no Projeto
- [review.md](../.github/agents/review.md) - Code review
- [test-generator.md](../.github/agents/test-generator.md) - Test generation
- [EXAMPLE.md](../.github/agents/EXAMPLE.md) - Template de referência

### Guias Relacionados
- [instructions.md](../.github/instructions.md) - Instruções gerais
- [CONTRIBUTING.md](../CONTRIBUTING.md) - Guia de contribuição
- [testing.md](testing.md) - Estratégia de testes

---

**Última atualização:** 14 de dezembro de 2025  
**Versão:** 1.0  
**Mantido por:** Time de Arquitetura
