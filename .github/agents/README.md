# GitHub Copilot Agents

Este diretório contém configurações de agentes do GitHub Copilot para automação de tarefas no repositório.

> **📝 Importante**: As configurações neste diretório representam o comportamento desejado e as 
> capacidades aspiracionais para agentes do GitHub Copilot. Nem todas as features descritas podem 
> estar disponíveis na versão atual do GitHub Copilot. Esta documentação serve como:
> - 📋 Especificação de requisitos para o agente de review
> - 🎯 Guia de comportamento esperado e ideal
> - 🔮 Preparação para features futuras do GitHub Copilot
> - 📖 Documentação de padrões de revisão do projeto

## 📋 Agentes e Features Disponíveis

### 🤖 Auto-geração de Descrição de PR
**Arquivo**: `../.github/copilot.yml` | **Config Detalhada**: `../.github/copilot-pr-description.yml`

O Copilot gera automaticamente uma descrição completa do PR quando você o cria.

**Como funciona:**
1. Ao criar um PR no GitHub, o Copilot analisa automaticamente os commits e mudanças
2. Gera uma descrição estruturada em português com:
   - 📋 Resumo das mudanças
   - 🎯 Motivação
   - 🔧 Mudanças principais
   - 📂 Arquivos modificados agrupados
   - 🧪 Como testar
3. Você pode aceitar, editar ou regenerar a descrição

**Configurado para:**
- ✅ Idioma: Português (pt-BR)
- ✅ Formato detalhado com emojis
- ✅ Contexto do projeto (.github/instructions.md, CONTRIBUTING.md)
- ✅ Warnings automáticos para mudanças críticas (segurança, configuração)

### 1. Review Agent (Agente de Revisão)
**Arquivo**: `review.md` | **Config**: `review-config.yml`

Agente que realiza revisões automáticas de código em Pull Requests.

#### ✨ Funcionalidades Principais

##### 📖 Leitura de Comentários do PR
O agente **lê e considera todos os comentários do PR** antes de realizar a revisão:
- Comentários gerais no PR
- Comentários inline no código (review comments)
- Respostas do autor aos comentários
- Discussões em threads de review

Isso permite que o agente:
- Entenda o contexto da discussão
- Evite duplicar feedback já fornecido
- Considere preocupações já levantadas
- Adapte sua revisão baseado em conversas anteriores

##### 🎯 Validação Automática
O agente valida automaticamente:
- ✅ Arquitetura DDD (separação de camadas)
- ✅ Java 25 features (Records, Pattern Matching)
- ✅ Spring Boot 4 (Constructor Injection, RestClient)
- ✅ MongoDB best practices (índices, embedding/referencing)
- ✅ Testes (JUnit 6, Instancio, mínimo 90% cobertura, ideal 95%)
- ✅ Segurança (@PreAuthorize, BCrypt, validação)
- ✅ REST API (status HTTP, Bean Validation, paginação)

##### 🔍 Priorização Inteligente
O agente prioriza issues por criticidade:
1. **Segurança** - Vulnerabilidades e exposição de dados
2. **Funcionalidade** - Bugs e lógica incorreta
3. **Arquitetura** - Violações de DDD
4. **Performance** - Queries N+1 e índices faltantes
5. **Manutenibilidade** - Código complexo e testes
6. **Estilo** - Conformidade com padrões

## 🚀 Como Usar

### Para Desenvolvedores

Quando você abre um Pull Request:
1. O agente de review é automaticamente acionado
2. Ele lê **todos os comentários existentes** no PR
3. Analisa as mudanças de código
4. Valida conformidade com os padrões do projeto
5. Fornece feedback contextualizado evitando duplicações

### Para Revisores

O agente complementa (não substitui) sua revisão:
- Use o feedback do agente como ponto de partida
- Adicione comentários sobre decisões arquiteturais
- O agente aprenderá com seus comentários para melhorar
- Decisões de design devem sempre ter validação humana

## ⚙️ Configuração

### Arquivos de Configuração

#### `review.md`
Documento de instrução para o agente contendo:
- Comportamento esperado
- Contexto para review (incluindo leitura de comentários)
- Padrões do projeto
- Prioridades de review
- Formato de feedback

#### `review-config.yml`
Configuração técnica do agente:
```yaml
# Habilitar leitura de comentários do PR
read_pr_comments: true

# Contexto adicional
context_sources:
  - pr_description
  - pr_comments        # 👈 Comentários gerais
  - review_comments    # 👈 Comentários inline
  - commit_messages
  - file_changes

# Comportamento
behavior:
  read_comments_first: true          # Ler comentários antes
  avoid_duplicate_feedback: true     # Evitar duplicações
  consider_author_responses: true    # Considerar respostas
```

### Integrações

O agente se integra com:
- **GitHub API**: Para ler comentários e metadados do PR
- **instructions.md**: Padrões gerais do projeto
- **PULL_REQUEST_TEMPLATE.md**: Checklist de implementação
- **copilot-instructions.md**: Instruções do VS Code Copilot
- **CONTRIBUTING.md**: Guia de contribuição

## 📊 Métricas

O agente monitora:
- Número de PRs revisados
- Issues encontradas por categoria
- Feedback aceito vs rejeitado
- Tempo médio de review
- Taxa de duplicação de feedback

## 🎓 Aprendizado Contínuo

O agente aprende com:
- Feedback dos desenvolvedores
- Correções aplicadas nos PRs
- Comentários e discussões
- Padrões emergentes no código
- Preferências da equipe

## 🔧 Personalização

Para ajustar o comportamento do agente:

1. **Modificar prioridades**: Edite `priority_order` em `review-config.yml`
2. **Adicionar verificações**: Atualize `required_checks` no config
3. **Ajustar detalhamento**: Mude `detail_level` (brief/normal/detailed)
4. **Mudar tom**: Altere `feedback_tone` (strict/constructive/encouraging)

## 🐛 Troubleshooting

### Agente não está lendo comentários
- Verifique que `read_pr_comments: true` em `review-config.yml`
- Confirme permissões `read:pull_request` e `read:comments`
- Valide que o GitHub API está acessível

### Feedback duplicado
- Certifique-se que `avoid_duplicate_feedback: true`
- Verifique se o agente tem acesso aos comentários anteriores

### Muitos falsos positivos
- Ajuste `detail_level` para `normal` ou `brief`
- Aumente `max_suggestions_per_file` se necessário
- Refine `exclude_patterns` para ignorar arquivos específicos

## 📚 Recursos

- [GitHub Copilot Documentation](https://docs.github.com/en/copilot)
- [CONTRIBUTING.md](../../CONTRIBUTING.md)
- [Plan Documentation](../../plan/README.md)

## 🤝 Contribuindo

Para melhorar o agente de review:
1. Teste mudanças no seu fork primeiro
2. Documente alterações no comportamento
3. Atualize exemplos se necessário
4. Submeta PR com descrição detalhada

---

**Versão**: 1.0  
**Última Atualização**: Dezembro 2025  
**Mantido por**: Time de Desenvolvimento
