# Local SonarQube Flow

This document explains how to run SonarQube locally (via Docker) and enforce analysis before commits using the optional pre-commit hook.

## Requirements
- Docker/Docker Compose
- Java 25 (already required for the project)
- SonarQube token generated locally (see below)

## Start SonarQube locally
```bash
docker compose up -d sonarqube
# UI: http://localhost:9000
```
The first run downloads the SonarQube image and creates local volumes for data/logs/extensions.

## Generate a local Sonar token
1) Open http://localhost:9000
2) Login (default admin/admin on first run, then change password)
3) My Account → Security → Generate Token (copy it; treat as secret)

## Run analysis manually (pre-commit dry run)
```bash
export SONAR_TOKEN=your_token
./gradlew clean test jacocoTestReport sonarqube
```
- Uses `sonar.host.url=http://localhost:9000` from sonar-project.properties
- `SONAR_TOKEN` is picked up by the Gradle sonarqube task

## Optional pre-commit hook (enforce locally)
Hook location: .githooks/pre-commit

Enable and run:
```bash
git config core.hooksPath .githooks
export SONAR_TOKEN=your_token
export RUN_SONAR_PRECOMMIT=1
# Warm up caches (optional) before first commit
./gradlew test jacocoTestReport sonarqube
```
Behavior:
- Checks Sonar status via http://localhost:9000/api/system/status
- Fails if Sonar is not UP or SONAR_TOKEN is missing
- Runs `./gradlew test jacocoTestReport sonarqube`

## Stop services
```bash
docker compose down
```

## Notes
- Tokens must not be committed. Store only in environment variables or secure secrets managers.
- The local flow is independent of any remote SonarCloud setup; CI workflow for SonarCloud was removed.
