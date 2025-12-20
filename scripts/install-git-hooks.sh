#!/usr/bin/env bash
set -euo pipefail

echo "Installing git hooks (setting core.hooksPath to .githooks)"

git config core.hooksPath .githooks
chmod +x .githooks/pre-commit

echo "Git hooks installed. To uninstall run: git config --unset core.hooksPath"
