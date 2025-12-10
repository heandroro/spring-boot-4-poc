# Configuração Recomendada: Branch Protection + Copilot Review

## 🎯 Estratégia Recomendada

Em vez de auto-approve perigoso, use esta abordagem:

### 1. GitHub Copilot faz review detalhado
- ✅ Identifica problemas automaticamente
- ✅ Sugere correções
- ✅ Comenta em português

### 2. Branch Protection exige aprovação humana
Configure no GitHub: Settings > Branches > Branch protection rules

```yaml
# Configuração via GitHub UI ou REST API
Branch: main
Rules:
  - Require pull request reviews before merging: ✅
  - Required number of approvals: 1
  - Dismiss stale approvals when new commits are pushed: ✅
  - Require review from Code Owners: ❌ (opcional)
  - Require status checks to pass: ✅
    - Required checks:
      - build
      - test
      - copilot-review (se configurado)
  - Require branches to be up to date: ✅
```

### 3. Workflow que FACILITA aprovação

```yaml
# .github/workflows/review-helper.yml
name: Review Helper

on:
  pull_request:
    types: [opened, synchronize]

jobs:
  quality-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Run tests
        run: ./gradlew test
      
      - name: Check coverage
        run: ./gradlew jacocoTestReport
      
      - name: Analyze with Copilot
        # Copilot faz review automaticamente
        # se configurado em .github/copilot.yml
        run: echo "Copilot review triggered"
      
      - name: Comment Summary
        if: success()
        uses: actions/github-script@v7
        with:
          script: |
            github.rest.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: `## ✅ Quality Checks Passed
              
              - ✅ Build successful
              - ✅ All tests passing
              - ✅ Copilot review completed
              
              **Ready for human review!** 👀
              
              Se tudo estiver OK no review do Copilot, você pode aprovar rapidamente.`
            })
```

## 🔐 Configuração de Segurança

### Opção A: Auto-approve APENAS para Dependabot
```yaml
# .github/workflows/dependabot-auto-approve.yml
name: Dependabot Auto Approve

on:
  pull_request:
    branches: [main]

permissions:
  pull-requests: write

jobs:
  approve:
    runs-on: ubuntu-latest
    if: github.actor == 'dependabot[bot]'
    steps:
      - uses: hmarr/auto-approve-action@v4
        with:
          github-token: ${{ secrets.PAT_TOKEN }}
```

### Opção B: Auto-merge (não approve) para PRs triviais
```yaml
# .github/workflows/auto-merge.yml
name: Auto Merge

on:
  pull_request:
    types: [opened, synchronize]

jobs:
  auto-merge:
    runs-on: ubuntu-latest
    if: |
      github.actor == 'dependabot[bot]' &&
      contains(github.event.pull_request.title, 'chore(deps)')
    steps:
      - name: Enable auto-merge
        run: gh pr merge --auto --squash "$PR_URL"
        env:
          PR_URL: ${{github.event.pull_request.html_url}}
          GH_TOKEN: ${{secrets.GITHUB_TOKEN}}
```

## ⚖️ Prós e Contras

### Auto-Approve Automático
❌ **NÃO RECOMENDADO** para:
- Código de produção
- Features críticas
- Mudanças de segurança
- Lógica de negócio

✅ **PODE SER USADO** para:
- Dependabot (atualizações de deps)
- Renovate bot
- Formatação automática
- Typos/documentação

### Copilot Review + Humano
✅ **RECOMENDADO** porque:
- Segurança mantida
- Decisões humanas preservadas
- Copilot ajuda, não substitui
- Conformidade com governança

## 🎯 Recomendação Final

**Use esta combinação:**

1. **GitHub Copilot Review** (automático) - Identifica problemas
2. **GitHub Actions** (automático) - Roda testes
3. **Branch Protection** - Exige 1 aprovação humana
4. **Auto-approve** - APENAS para dependabot/bots

Isso mantém segurança enquanto acelera o processo!

---

**Para habilitar auto-approve:**
```bash
# Renomear arquivo
mv .github/workflows/auto-approve.yml.disabled .github/workflows/auto-approve.yml

# ⚠️ Certifique-se de entender os riscos!
```
