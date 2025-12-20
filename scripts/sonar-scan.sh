#!/usr/bin/env bash
set -euo pipefail

# Simple SonarQube scanner wrapper
# - Prefers local 'sonar-scanner' if available
# - Falls back to Docker image 'sonarsource/sonar-scanner-cli'

export SONAR_HOST_URL=${SONAR_HOST_URL:-http://localhost:9000}
export SONAR_TOKEN=${SONAR_TOKEN:-sqa_87e499f4c58c33a32fe663f08d4a4388b6dc69aa}
export SONAR_PROJECT_KEY=${SONAR_PROJECT_KEY:-spring-boot-4-poc}

JACOCO_XML=build/reports/jacoco/test/jacocoTestReport.xml

if [ -z "${SONAR_TOKEN}" ]; then
  echo "ERROR: SONAR_TOKEN environment variable is not set. Obtain it from SonarQube and export it before running this script."
  echo "Example: export SONAR_TOKEN=\"${SONAR_TOKEN}\""
  exit 1
fi

# Common scanner properties
COMMON_ARGS=(
  -Dsonar.projectKey="${SONAR_PROJECT_KEY}"
  -Dsonar.url="${SONAR_HOST_URL}"
  -Dsonar.token="${SONAR_TOKEN}"
)

if command -v sonar-scanner >/dev/null 2>&1; then
  echo "Using local sonar-scanner"
  sonar-scanner "${COMMON_ARGS[@]}"
else
  echo "Local sonar-scanner not found, using Docker image sonarsource/sonar-scanner-cli"

  # If Sonar is running on the host (localhost), containers must reach it via
  # host.docker.internal on Docker Desktop (macOS/Windows). If the user didn't
  # override SONAR_HOST_URL and it contains localhost, attempt to use
  # host.docker.internal automatically when reachable.
  DOCKER_SONAR_HOST_URL="${SONAR_HOST_URL}"
  if [[ "${SONAR_HOST_URL}" == *"localhost"* || "${SONAR_HOST_URL}" == *"127.0.0.1"* ]]; then
    if curl -s --connect-timeout 2 "http://host.docker.internal:9000/api/system/status" | grep -q UP; then
      DOCKER_SONAR_HOST_URL="http://host.docker.internal:9000"
      echo "Detected Sonar on host; using host.docker.internal for Docker run: ${DOCKER_SONAR_HOST_URL}"
    else
      echo "Warning: host.docker.internal is not reachable; ensure Sonar is accessible to containers or set SONAR_HOST_URL to an accessible host."
    fi
  fi

  # On Apple Silicon hosts, explicitly request an amd64 image if necessary to avoid
  # subtle compatibility issues (emulation will be used). This is optional and
  # can be overridden by setting DOCKER_SONAR_PLATFORM environment variable.
  PLATFORM_ARG=()
  ARCH=$(uname -m || true)
  if [[ -n "${DOCKER_SONAR_PLATFORM:-}" ]]; then
    PLATFORM_ARG=(--platform "${DOCKER_SONAR_PLATFORM}")
  elif [[ "${ARCH}" == "arm64" || "${ARCH}" == "aarch64" ]]; then
    PLATFORM_ARG=(--platform linux/amd64)
  fi

  docker run --rm "${PLATFORM_ARG[@]}" \
    -e SONAR_HOST_URL="${DOCKER_SONAR_HOST_URL}" \
    -e SONAR_TOKEN="${SONAR_TOKEN}" \
    -v "$(pwd)":/usr/src \
    -w /usr/src \
    sonarsource/sonar-scanner-cli:latest \
    "${COMMON_ARGS[@]}"
fi

# Save coverage and issues reports from Sonar to build/sonar
OUTPUT_DIR="./sonar"
mkdir -p "${OUTPUT_DIR}"

# Fetch measures (coverage, lines_to_cover)
COVERAGE_API_URL="${SONAR_HOST_URL}/api/measures/component?component=${SONAR_PROJECT_KEY}&metricKeys=coverage,lines_to_cover,uncovered_lines"
if curl -s -u "${SONAR_TOKEN}:" "$COVERAGE_API_URL" -o "${OUTPUT_DIR}/coverage.json"; then
  echo "Saved coverage measures to ${OUTPUT_DIR}/coverage.json"
else
  echo "Warning: Failed to fetch coverage measures from SonarQube"
fi

# Fetch issues (first 500 results)
ISSUES_API_URL="${SONAR_HOST_URL}/api/issues/search?componentKeys=${SONAR_PROJECT_KEY}&ps=500"
if curl -s -u "${SONAR_TOKEN}:" "$ISSUES_API_URL" -o "${OUTPUT_DIR}/issues.json"; then
  echo "Saved issues to ${OUTPUT_DIR}/issues.json"
else
  echo "Warning: Failed to fetch issues from SonarQube"
fi

# Print a short summary if jq is available
if command -v jq >/dev/null 2>&1; then
  COVERAGE=$(jq -r '.component.measures[] | select(.metric=="coverage") | .value' "${OUTPUT_DIR}/coverage.json" 2>/dev/null || echo "N/A")
  TOTAL_ISSUES=$(jq -r '.total' "${OUTPUT_DIR}/issues.json" 2>/dev/null || echo "0")
  echo "Sonar summary: coverage=${COVERAGE}%, issues=${TOTAL_ISSUES} (saved to ${OUTPUT_DIR})"
else
  echo "Summary files saved to ${OUTPUT_DIR}; install 'jq' to print a short summary (e.g. 'brew install jq')."
fi
