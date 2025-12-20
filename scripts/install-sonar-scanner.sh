#!/usr/bin/env bash
set -euo pipefail

# Attempt to install sonar-scanner on macOS via Homebrew when available.
# Otherwise, print manual instructions to download the binary.

if command -v sonar-scanner >/dev/null 2>&1; then
  echo "sonar-scanner is already installed: $(command -v sonar-scanner)"
  exit 0
fi

if command -v brew >/dev/null 2>&1; then
  echo "Installing sonar-scanner via Homebrew..."
  if brew install sonar-scanner; then
    echo "sonar-scanner installed via Homebrew"
    exit 0
  fi
  echo "Attempting to tap sonarsource/sonar-scanner and install..."
  brew tap sonarsource/sonar-scanner || true
  brew install sonarsource/sonar-scanner/sonar-scanner || true
  echo "If installation fails, please download the CLI manually from:"
  echo "https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/"
  exit 1
else
  cat <<EOF
Homebrew not found. To install sonar-scanner manually:

1) Download the CLI zip from:
   https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/
2) Extract it and add the 'bin' directory to your PATH.
3) Verify with 'sonar-scanner --version'.

Alternatively, use the included Docker-based runner: ./scripts/sonar-scan.sh (requires Docker + SONAR_TOKEN set).
EOF
  exit 1
fi
