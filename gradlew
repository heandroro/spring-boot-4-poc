#!/usr/bin/env sh
set -e
DIRNAME="$(cd "$(dirname "$0")" >/dev/null 2>&1 && pwd)"
DIST_VERSION="9.2.1"
DIST_ZIP="gradle-${DIST_VERSION}-bin.zip"
DIST_URL="https://services.gradle.org/distributions/${DIST_ZIP}"
INSTALL_DIR="$DIRNAME/.gradle-dist/gradle-${DIST_VERSION}"

download_and_unpack() {
  echo "Gradle wrapper JAR not found; fetching Gradle ${DIST_VERSION} distribution..."
  mkdir -p "$DIRNAME/.gradle-dist"
  if command -v curl >/dev/null 2>&1; then
    curl -fSL "$DIST_URL" -o "$DIRNAME/.gradle-dist/${DIST_ZIP}"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$DIRNAME/.gradle-dist/${DIST_ZIP}" "$DIST_URL"
  else
    echo "Error: neither curl nor wget is available to download Gradle." >&2
    exit 1
  fi
  if command -v unzip >/dev/null 2>&1; then
    unzip -q "$DIRNAME/.gradle-dist/${DIST_ZIP}" -d "$DIRNAME/.gradle-dist"
  else
    echo "Error: unzip is required to extract Gradle distribution." >&2
    exit 1
  fi
}

if [ -f "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" ]; then
  # If the wrapper jar exists, use it (legacy behavior)
  CLASSPATH="$DIRNAME/gradle/wrapper/gradle-wrapper.jar"
  if [ -z "$JAVA_HOME" ]; then
    JAVA_CMD=java
  else
    JAVA_CMD="$JAVA_HOME/bin/java"
  fi
  exec "$JAVA_CMD" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
else
  # Download and unpack a Gradle distribution and run its gradle script
  if [ ! -d "$INSTALL_DIR" ]; then
    download_and_unpack
  fi
  GRADLE_BIN="$INSTALL_DIR/bin/gradle"
  if [ ! -x "$GRADLE_BIN" ]; then
    chmod +x "$GRADLE_BIN" || true
  fi
  exec "$GRADLE_BIN" "$@"
fi
