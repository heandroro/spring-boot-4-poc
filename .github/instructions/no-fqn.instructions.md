Rule: Never use Fully-Qualified Class Names (FQN) in code

When generating or modifying Java code in this repository, do NOT use fully-qualified class names (e.g., `com.example.Foo`) inside classes, methods, or variable declarations. Always add an `import` statement and reference the simple type name instead.

Scope
- Applies to `src/main/java` and `src/test/java` files.
- Applies to generated and hand-written code, and to code suggestions made by AI-assisted tools (e.g., Copilot).

Why
- Keeps code readable and consistent with project style.
- Avoids long inline types and makes refactorings and imports easier to manage.

Behavior
- If a class name would collide with another type in scope, prefer adding an alias via a small helper or refactor to avoid requiring FQN.
- Do not introduce FQN in new code or leave them in edited code during PRs.

Enforcement
- This repository already includes a Checkstyle rule that forbids FQNs: `config/checkstyle/checkstyle.xml` (RegexpSingleline rule).
- The CI review workflow runs `./gradlew check` on PRs; any violation (including FQNs) will fail the job and block merge until fixed.

Copilot / Code Creation Agent guidance
- When suggesting or generating code, prefer imports and simple type names.
- If a generated snippet would normally include a FQN, replace it with an `import` and the simple name before submitting the suggestion or PR.
- Add a short PR description comment if any non-trivial import choices were made (e.g., to resolve ambiguous type names).

Review Agent guidance
- During automated or human review, flag any FQNs introduced in PRs and request a fix (convert to imports).
- If CI fails due to Checkstyle's FQN rule, add a comment explaining how to rewrite the code using imports and point to `CONTRIBUTING.md` and this instruction file.

If you need to update this policy, modify this file and add the reason to the PR body.