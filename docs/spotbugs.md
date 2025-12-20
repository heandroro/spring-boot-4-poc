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

## Suppressões específicas aplicadas
Ao executar o SpotBugs foi detectado o aviso `EI_EXPOSE_REP2` para classes que recebem `MongoTemplate` via injeção do Spring. Esse aviso aponta que um objeto mutável injetado é armazenado no campo da classe, o que normalmente pode expor o estado interno.

No caso do projeto, `MongoTemplate` é um bean gerenciado pelo Spring e é seguro armazenar sua referência (é thread-safe). Para evitar falsos positivos e manter o relatório limpo, aplicamos uma supressão localizada com justificativa clara nas seguintes classes:

- `src/main/java/com/example/poc/infrastructure/persistence/MongoIndexInitializer.java`
- `src/main/java/com/example/poc/infrastructure/persistence/MongoCustomerRepository.java`

A anotação usada foi:

```java
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "MongoTemplate is injected and thread-safe; storing the reference is intentional and safe")
```

Por que fizemos isso:
- Evita ruído no relatório de SpotBugs quando a condição é intencional e segura.
- Mantém os relatórios acionáveis, focando em problemas reais.

Como reverter ou revisar:
- Para remover a supressão, delete a anotação e execute `./gradlew spotbugsMain spotbugsTest` para ver o alerta novamente.
- Alternativamente, se preferir não usar anotações, podemos encapsular `MongoTemplate` atrás de um wrapper imutável ou adicionar uma regra de exclusão no `config/spotbugs/exclude.xml` (opção menos recomendada que uma supressão localizada).

Commit de referência: `6ebd59e` (suppress SpotBugs EI_EXPOSE_REP2 for injected MongoTemplate)

---

Execute novamente o SpotBugs com:

```bash
./gradlew spotbugsMain spotbugsTest
```

O relatório gerado fica em `build/reports/spotbugs/main.html` e `build/reports/spotbugs/test.html`.