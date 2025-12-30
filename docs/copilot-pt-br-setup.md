# Configuração do GitHub Copilot para Português (pt-BR)

## 📋 Resumo

Este documento explica como foi configurado o GitHub Copilot para gerar **todos os reviews, descrições e comentários em Português Brasileiro (pt-BR)**.

## 🎯 Problema Original

Apesar de configurar:
- `.github/copilot.yml` com `description_language: pt-BR`
- `.github/agents/review-config.yml` com `language: "pt-BR"`
- `.github/PULL_REQUEST_TEMPLATE.md` com instruções em português

O **GitHub Copilot Pull Request Reviewer** continuava gerando reviews em **inglês**.

## ✅ Solução Implementada

A solução foi criar o arquivo **`.github/copilot-instructions.md`**, que é o **padrão oficial** do GitHub para instruções customizadas do Copilot a nível de repositório.

### Arquivo Criado

**Localização**: [`.github/copilot-instructions.md`](../.github/copilot-instructions.md)

**Conteúdo principal**:
```markdown
# Instruções do GitHub Copilot para este Repositório

## 🌐 IDIOMA - REGRA FUNDAMENTAL

**SEMPRE responda, comente e gere código em Português Brasileiro (pt-BR)**, incluindo:
- ✅ Reviews de Pull Requests
- ✅ Descrições de PR
- ✅ Comentários inline de código
- ✅ Sugestões de melhorias
- ✅ Mensagens de commit
- ✅ Respostas no chat

**Exceção**: Use inglês APENAS se o usuário solicitar explicitamente.
```

O arquivo também inclui:
- 📚 Referências para todos os arquivos de padrões do projeto
- 🏗️ Contexto completo sobre a stack (Spring Boot 4, Java 25, MongoDB, DDD)
- ✅ Lista detalhada de padrões obrigatórios
- 🚫 Proibições absolutas (FQN, Lombok, comentários, field injection)
- 📋 Checklist de review com prioridades
- 📝 Exemplos de formato de feedback em português
- 🌟 Exemplos de sugestões de código completos

## 🔧 Como Funciona

### 1. Copilot no GitHub.com
Quando o GitHub Copilot gera reviews de PR ou descrições no site do GitHub, ele automaticamente lê o arquivo `.github/copilot-instructions.md` e segue as instruções definidas.

### 2. Copilot no VS Code / JetBrains
Para desenvolvedores usando IDEs localmente, o Copilot também lê este arquivo quando presente no repositório, garantindo consistência nas sugestões de código.

### 3. Hierarquia de Configuração
```
1. .github/copilot-instructions.md (✅ CRIADO - maior prioridade)
2. .github/instructions.md (já existia)
3. .github/agents/review-config.yml (já existia - aspiracional)
4. .github/PULL_REQUEST_TEMPLATE.md (já existia)
```

## 📖 Referências Oficiais

### Documentação GitHub
- [Como adicionar instruções personalizadas de repositório](https://docs.github.com/pt/copilot/how-tos/configure-custom-instructions/add-repository-instructions)
- [Configurar a revisão automática do GitHub Copilot](https://docs.github.com/pt/copilot/how-tos/use-copilot-agents/request-a-code-review/configure-automatic-review)

### Configuração VS Code
Para garantir pt-BR também no VS Code Copilot, adicione no `settings.json`:
```json
{
  "github.copilot.chat.localeOverride": "pt-BR"
}
```

## 🧪 Como Testar

### Teste 1: Review de PR
1. Crie um novo Pull Request com mudanças de código
2. O GitHub Copilot PR Reviewer deve comentar automaticamente em português
3. Verifique que o sumário e comentários estão em pt-BR

### Teste 2: Descrição de PR
1. Crie um novo Pull Request
2. Clique em "Copilot" no campo de descrição
3. Selecione "Generate description"
4. A descrição gerada deve estar em português

### Teste 3: Chat do Copilot
1. No VS Code, abra o Copilot Chat (Ctrl+I / Cmd+I)
2. Faça uma pergunta: "Explique este código"
3. A resposta deve estar em português

## ✨ Benefícios

1. **Consistência**: Todos os comentários e reviews do Copilot em pt-BR
2. **Padrões do Projeto**: Copilot conhece todos os padrões (DDD, Java 25, Spring Boot 4)
3. **Contexto Completo**: Referências para CONTRIBUTING.md, instructions.md, etc.
4. **Reviews Inteligentes**: Prioriza segurança, funcionalidade e arquitetura
5. **Exemplos Práticos**: Formato de feedback e sugestões de código incluídos

## 🔄 Próximos Passos Recomendados

### 1. Testar com PR Real
Crie um PR de teste pequeno e observe se o Copilot review está em português.

### 2. Configurar VS Code (Opcional)
Se você usa VS Code, adicione a configuração `localeOverride` para garantir pt-BR localmente.

### 3. Feedback Contínuo
Se o Copilot ainda gerar algum conteúdo em inglês:
- Reforce a instrução adicionando exemplos específicos no `copilot-instructions.md`
- Reporte via GitHub Issues para melhorar a configuração

### 4. Automatizar Reviews
Configure regras de branch protection para solicitar reviews automáticos do Copilot:
1. Vá em "Settings" → "Branches" → "Branch protection rules"
2. Adicione regra para branch `main` ou `develop`
3. Habilite "Request Copilot code review"

## 📚 Arquivos Relacionados

| Arquivo | Propósito | Status |
|---------|-----------|--------|
| `.github/copilot-instructions.md` | **Instruções principais do Copilot** | ✅ **CRIADO** |
| `.github/copilot.yml` | Configuração de PR descriptions | ✅ Existente |
| `.github/copilot-pr-description.yml` | Config detalhada de descrições | ✅ Existente |
| `.github/agents/review-config.yml` | Config aspiracional do review agent | ✅ Existente |
| `.github/agents/review.md` | Documentação do review agent | ✅ Existente |
| `.github/instructions.md` | Padrões gerais do projeto | ✅ Existente |
| `.github/PULL_REQUEST_TEMPLATE.md` | Template de PR em português | ✅ Existente |
| `CONTRIBUTING.md` | Guia de contribuição | ✅ Existente |

## ❓ FAQ

### O Copilot vai SEMPRE responder em português agora?
**Sim**, a menos que você explicitamente peça em inglês (ex: "Please review in English").

### Isso funciona para reviews automáticos?
**Sim**, quando configurado nas regras de branch protection.

### Preciso configurar algo no meu VS Code?
**Opcional**, mas recomendado: `"github.copilot.chat.localeOverride": "pt-BR"`

### E se eu quiser um review em inglês pontualmente?
Adicione um comentário no PR: "Copilot, please review this in English"

### Isso afeta apenas este repositório?
**Sim**, cada repositório tem suas próprias instruções customizadas.

## 🎉 Conclusão

Com o arquivo `.github/copilot-instructions.md` criado, o GitHub Copilot agora está configurado para:
- ✅ Responder sempre em Português (pt-BR)
- ✅ Conhecer todos os padrões do projeto
- ✅ Gerar reviews consistentes e contextualizados
- ✅ Priorizar segurança e boas práticas
- ✅ Fornecer feedback construtivo em português

**Para mais informações**:
- Ver arquivo completo: [.github/copilot-instructions.md](../.github/copilot-instructions.md)
- Documentação oficial: [GitHub Copilot Docs (pt-BR)](https://docs.github.com/pt/copilot)
- Suporte da comunidade: [GitHub Community](https://github.com/orgs/community/discussions)

---

**Data de criação**: 30/12/2025  
**Última atualização**: 30/12/2025  
**Issue relacionada**: #28 (Copilot PR Reviewer em Português)
