# 📝 Instruções para Atualização do README.md

## 🎯 Objetivo
Manter o README.md sempre atualizado de forma **resumida e objetiva** com cada commit significativo.

## 📋 Regras de Atualização

### 1. **Quando atualizar o README.md**
Atualize o README.md quando houver commits que:
- ✅ Adicionam novas features ou funcionalidades
- ✅ Modificam arquitetura ou estrutura do projeto
- ✅ Introduzem novas dependências ou tecnologias
- ✅ Alteram comandos de build/run/test
- ✅ Adicionam novos endpoints ou APIs
- ✅ Modificam configurações importantes

**NÃO atualize** para:
- ❌ Pequenas correções de bugs
- ❌ Ajustes de formatação/estilo
- ❌ Atualizações de documentação interna apenas
- ❌ Refatorações que não afetam uso externo

### 2. **Como atualizar - Princípio RESUMIDO**

#### ✅ CORRETO - Resumido no README.md
```markdown
## 🔒 Segurança
- JWT authentication com BCrypt
- CORS configurável via environment variables
- @PreAuthorize em endpoints protegidos
- Ver detalhes em [docs/security.md](docs/security.md)
```

#### ❌ ERRADO - Detalhado demais no README.md
```markdown
## 🔒 Segurança
- JWT authentication implementado com biblioteca jjwt versão 0.12.x
- BCrypt password encoder com strength 10
- CORS configuração detalhada:
  - Allowed Origins: configurável via CORS_ALLOWED_ORIGINS
  - Allowed Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
  - Allowed Headers: *
  - Allow Credentials: false
  - Pré-flight requests suportados
- @PreAuthorize com roles: ROLE_CUSTOMER, ROLE_MANAGER, ROLE_ADMIN
- Validação de entrada com Bean Validation
- [... muitos detalhes ...]
```

### 3. **Estrutura de Documentação Detalhada**

Quando detalhes são necessários, crie arquivos em `/docs`:

```
docs/
├── security.md          # Detalhes de autenticação, autorização, CORS
├── architecture.md      # Decisões arquiteturais, DDD, camadas
├── testing.md           # Estratégia de testes, fixtures, cobertura
├── mongodb.md           # Schema design, índices, queries
├── api.md               # Endpoints, contratos, exemplos
├── deployment.md        # Deploy, variáveis de ambiente, Docker
└── troubleshooting.md   # Problemas comuns e soluções
```

### 4. **Template de Atualização do README.md**

Ao adicionar nova seção ou atualizar existente:

```markdown
## 🆕 [Nome da Feature/Seção]

[Descrição de 1-3 linhas sobre o que é]

**Principais recursos:**
- Recurso 1 (breve)
- Recurso 2 (breve)
- Recurso 3 (breve)

📖 **Detalhes completos:** [docs/nome-arquivo.md](docs/nome-arquivo.md)
```

### 5. **Seções Obrigatórias no README.md**

Mantenha estas seções sempre atualizadas:

1. **Título e Descrição** (1-2 linhas)
2. **Key Points** - Principais tecnologias
3. **Build and Run** - Comandos básicos
4. **Testing** - Como rodar testes
5. **Architecture** - Resumo da estrutura (link para detalhes)
6. **Documentation** - Links para docs detalhadas

### 6. **Prompt para o Copilot**

Use este prompt ao commitar mudanças significativas:

```
@workspace Atualize o README.md seguindo as instruções em .github/README-UPDATE-INSTRUCTIONS.md:

1. Adicione/atualize seção sobre [FEATURE/MUDANÇA]
2. Mantenha descrição RESUMIDA (máximo 3-4 linhas)
3. Se necessário, crie arquivo detalhado em docs/[nome].md
4. Adicione link "📖 Detalhes completos: [docs/arquivo.md]"
5. Não remova seções existentes, apenas complemente

Commits relacionados: [HASH DO COMMIT]
```

### 7. **Checklist de Atualização**

Antes de commitar atualização do README.md:

- [ ] Descrição é concisa (máximo 4 linhas por seção)?
- [ ] Detalhes técnicos foram movidos para docs/?
- [ ] Links para documentação detalhada estão funcionando?
- [ ] Comandos de exemplo estão corretos e testados?
- [ ] Emojis usados de forma consistente com o resto do README?
- [ ] Não há informação duplicada?

### 8. **Exemplo Prático**

**Commit:** "feat: add Customer CRUD endpoints with MongoDB"

**README.md (RESUMIDO):**
```markdown
## 🛒 Funcionalidades

### Customer Management
- CRUD completo de clientes via REST API
- Validação com Bean Validation
- Autenticação JWT obrigatória

📖 **API Reference:** [docs/api.md](docs/api.md)
📖 **Schema MongoDB:** [docs/mongodb.md](docs/mongodb.md)
```

**docs/api.md (DETALHADO):**
```markdown
# API Reference

## Customer Endpoints

### POST /api/customers
Cria novo cliente...
[Detalhes completos: request/response, validações, exemplos]

### GET /api/customers/{id}
Busca cliente por ID...
[Detalhes completos]
```

### 9. **Automação com Git Hooks (Opcional)**

Para lembrar de atualizar o README:

```bash
# .git/hooks/pre-commit
#!/bin/bash
if git diff --cached --name-only | grep -q "src/"; then
    echo "⚠️  LEMBRETE: Atualize o README.md se necessário!"
    echo "   Use: @workspace atualize README.md conforme .github/README-UPDATE-INSTRUCTIONS.md"
fi
```

### 10. **Palavras-chave para Identificar Necessidade de Atualização**

Se o commit message contiver estas palavras, **considere atualizar README.md**:

- `feat:` - Nova feature
- `breaking:` - Breaking change
- `api:` - Mudança em API
- `config:` - Nova configuração
- `security:` - Mudança de segurança
- `deps:` - Dependência importante adicionada

---

## 🤖 Prompt Rápido para Copilot

```
@workspace Seguindo .github/README-UPDATE-INSTRUCTIONS.md:
1. Analise mudanças nos últimos commits
2. Atualize README.md de forma RESUMIDA
3. Crie docs detalhadas se necessário
4. Mantenha estrutura existente
```

---

**Versão:** 1.0  
**Mantido por:** Equipe de Desenvolvimento  
**Última revisão:** Dezembro 2025
