Rule: No comments inside classes

When generating or modifying code in this repository, do NOT add or preserve comments inside Java classes or methods. This includes:
- Javadoc blocks (/** ... */)
- Line comments (// ...)
- Block comments (/* ... */) inside class or method bodies

Exceptions:
- LICENSE and legal headers at the top of files may remain.
- Comments in documentation files (README.md, docs/) are allowed.

Behavior:
- Prefer self-explanatory code: use descriptive names for classes, methods, variables and small helper functions.
- If a behavior needs explanation longer than the code, put it in docs/ or CONTRIBUTING.md instead of in-code comments.

Copilot-specific guidance:
- When responding or generating code for this repository, remove any comments you would normally add and output only the code with no in-class comments.
- If a generated snippet would normally include a Javadoc, instead generate clean code and add a note in the PR description explaining the intent if necessary.

Rationale:
- Keep source files concise and easier to maintain.
- Avoid stale or conflicting documentation inside code.

If you need to update this policy, modify this file and add the reason to the PR body.