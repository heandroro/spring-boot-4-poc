# SpotBugs Local

SpotBugs é uma ferramenta de análise estática que detecta bugs e padrões problemáticos em código Java. No projeto, ela complementa o SonarQube e o Checkstyle, focando em defeitos de implementação que podem passar despercebidos por testes e regras de estilo.

## Como funciona
- O SpotBugs é executado automaticamente pelo Gradle via o comando `./gradlew check`.
- O relatório é gerado mesmo que ocorram falhas, pois o projeto está usando Java 25, que ainda não é suportado oficialmente pelo SpotBugs (por isso, `ignoreFailures=true`).
- Classes geradas, de teste e de configuração estão excluídas da análise via o filtro em `config/spotbugs/exclude.xml`.

## Execução manual
Para rodar o SpotBugs localmente:

```sh
./gradlew spotbugsMain spotbugsTest
```

Os relatórios são gerados em:
- `build/reports/spotbugs/main.html`
- `build/reports/spotbugs/test.html`

## Interpretação dos resultados
- **Avisos e erros**: São exibidos no relatório HTML. Com Java 25, muitos arquivos não são analisados devido à incompatibilidade, mas o relatório ainda pode apontar problemas em arquivos compatíveis.
- **Falhas não quebram o build**: O build não falha por causa do SpotBugs enquanto não houver suporte oficial para Java 25.

## Configuração
- Plugin e configuração estão em `build.gradle.kts`.
- Exclusões em `config/spotbugs/exclude.xml`.

## Referências
- [SpotBugs](https://spotbugs.github.io/)
- [Plugin Gradle SpotBugs](https://github.com/spotbugs/spotbugs-gradle-plugin)

## Futuro
Quando o SpotBugs suportar Java 25, remova `ignoreFailures=true` para que falhas passem a quebrar o build e aumente a cobertura da análise.