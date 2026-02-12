# GUIDE — Lições aprendidas e práticas adotadas neste PoC

Este documento consolida as lições aprendidas, decisões arquiteturais e operacionais tomadas durante a correção e estabilização deste projeto Spring Boot 4 POC.

Formato
- Idioma: Português (pt-BR)
- Objetivo: servir como referência rápida para futuros contribuidores e como checklist para garantir que PRs/commits sigam padrões do repositório.

1) Padrões técnicos obrigatórios (resumo)
- Java 25: use recursos modernos (Records, pattern matching, text blocks). Não usar Lombok.
- Spring Boot 4: constructor injection obrigatório (nunca field @Autowired), use RestClient quando necessário e ProblemDetail para erros.
- MongoDB: seguir práticas de embedding vs referencing; usar `@Indexed` e TTL quando aplicável.
- Segurança: RBAC com `@PreAuthorize`, BCrypt para senhas, JWT para endpoints protegidos.
- DTOs: NUNCA expor entidades de domínio diretamente (especialmente campos sensíveis).

2) Regras de estilo e validação automática
- Proibições do projeto:
  - Nomes totalmente qualificados (FQN) no código (sempre adicione imports)
  - Comentários em classes (javadoc/line/block) — remova comentários de código; documentação vai em `docs/` ou `CONTRIBUTING.md`
  - Lombok proibido — prefira Records e APIs Java nativas
- Checkstyle já configurado (falhará o build se houver violações).

3) Segurança / Tratamento de erros
- Usar `ProblemDetail` para respostas de erro REST (RFC-7807).
- GlobalExceptionHandler: log de exceções via SLF4J e retorno padronizado.

4) Testes e cobertura
- Frameworks: JUnit 6, Mockito, Instancio, Testcontainers (para integração com MongoDB)
- Política de cobertura: cobertura mínima de 90% (ramificações e linhas). Jacoco configurado para verificar `branches` e `lines`.
- Dicas para aumentar cobertura:
  - Testes focados em branches (ternários, null-checks e caminhos alternativos) aumentam `branch coverage` rapidamente.
  - Criar fixtures via Instancio e factories para reduzir boilerplate.

5) Ferramentas de análise
- SpotBugs: observar avisos EI_EXPOSE_REP / EI_EXPOSE_REP2 (exposição de representação interna). Solução: cópias defensivas (Map.copyOf/List.copyOf) ao construir DTOs/records.
- Checkstyle: regras estritas já definidas (no repo `config/checkstyle/checkstyle.xml`) — importante para uniformidade do código.

6) Mudanças realizadas durante a correção
- DTOs (`ProductCreateDto`, `ProductDto`): adição de construtores compactos que aplicam `Map.copyOf` e `List.copyOf` para evitar exposição de coleções mutáveis.
- `ProductService`: cópias defensivas e remoção de usos de fully-qualified names; tipos explícitos onde necessário para evitar `wildcard capture` na hora de passar coleções.
- `GlobalExceptionHandler`: substituído `printStackTrace` por SLF4J `Logger` e uso de `ProblemDetail`.
- Testes: adição de casos em `ProductServiceTest` para cobrir branches faltantes (ex.: specifications não nulo, DTO com currency null, create com null specs/images etc.).
- CI: adicionado workflow GitHub Actions `.github/workflows/ci.yml` para rodar Checkstyle, SpotBugs, tests e Jacoco com verificação.
- Git hooks: `.githooks` + `scripts/install-git-hooks.sh` (o hook atual verifica naming de integration tests e impede commits quando o padrão não é seguido).

7) Procedimentos para desenvolvedores (como reproduzir localmente)
- Instalar hooks locais (recomendado):

```bash
# no diretório do repo
./scripts/install-git-hooks.sh
```

- Rodar checks localmente (rápido):

```bash
./gradlew check -q
```

- Rodar testes e gerar relatório Jacoco:

```bash
./gradlew clean test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

- Rodar SpotBugs/Checkstyle manualmente:

```bash
./gradlew spotbugsMain spotbugsTest
./gradlew checkstyleMain
open build/reports/checkstyle/main.html
```

8) Boas práticas e recomendações (lições aprendidas)
- Prefira testar caminhos (branches) antes de aumentar contagem de linhas: normalmente poucos testes bem posicionados resolvem falhas em `branch coverage`.
- Use cópias defensivas em boundary points (quando receber coleções de bibliotecas externas, DTOs ou camadas web).
- Evite suprimir warnings massivamente; corrija o código sempre que possível. Quando for inevitável, use `@SuppressFBWarnings` com justificativa clara.
- Mantenha o CI como guardião principal: hooks locais dão feedback rápido, mas o CI é a defesa final.

9) Checklist de revisão de PR (resumido)
- Segurança: endpoints protegidos, senhas/hashes, DTOs sem dados sensíveis expostos
- Funcionalidade: testes para happy path e erros
- Arquitetura: respeita DDD (controller -> service -> repository)
- Performance: índices MongoDB quando aplicável
- Testes: cobertura >= 90% e `@DisplayName` em português quando aplicável
- Estilo: sem FQN, sem comentários em classes, sem Lombok

10) Próximos passos sugeridos
- Forçar CI do PR atual e revisar comentários (se houver feedback do code review, aplicar mudanças imediatas)
- Expandir testes de integração com Testcontainers (MongoDB) para validar integração com DB real
- Documentar padrões de mapeamento (MapStruct ou mappers manuais) e convenções para DTOs

11) Contato e onboarding
- Para novos contribuintes, seguir `CONTRIBUTING.md` e rodar hooks/CI localmente antes de abrir PRs.

---

Se quiser, posso:
- 1) abrir/atualizar o PR com um comentário que documenta essas correções (preciso do acesso ao GH ou você pode rodar `gh` localmente e me passar a URL);
- 2) aplicar correções adicionais solicitadas nos comentários do PR (cole aqui os comentários ou me autorize a tentar `gh` para obter os comentários);
- 3) rodar novamente o pipeline completo e salvar os artefatos (jacoco/checkstyle) e enviar o link.

Diga o que quer que eu faça a seguir: buscar comentários do PR (tentar novamente), aplicar correções específicas (cole os comentários), ou abrir/atualizar o PR com este GUIDE e as correções implementadas (faço commit/push).
