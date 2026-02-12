Instalação de Git Hooks

Este repositório fornece hooks em `.githooks/` para validações locais pré-commit. Para ativá-los, execute:

```bash
./scripts/install-git-hooks.sh
```

O script configura `git config core.hooksPath .githooks` e torna o hook `pre-commit` executável.

O hook atual executa `./gradlew -q checkIntegrationTestNames` (validação rápida) antes de cada commit.

