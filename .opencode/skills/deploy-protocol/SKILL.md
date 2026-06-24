---
name: deploy-protocol
description: Spring Boot deployment preparation and instructions
---

# Deploy Preparation - Spring Boot

## Package Artifact
```bash
# Maven
./mvnw clean package

# Gradle
./gradlew bootJar

# JAR location: target/*.jar
```
Deploy Instructions (for Human)
```
=== DEPLOYMENT READY ===
Artifact: target/app-1.2.3.jar
Environment: staging/production
Commands:
  java -jar app-1.2.3.jar --spring.profiles.active=prod
```

Staging Checks

    ✅ JAR file exists

    ✅ Application properties configured

    ✅ Database migration scripts ready

## Rollback (if needed)
```bash
# Deploy previous version
java -jar app-1.2.2.jar --spring.profiles.active=prod
```
## Database Migration
```bash
./mvnw flyway:migrate -Dflyway.profiles=prod
```

## Branch Naming
- Feature: `feature/<task-name>`
- Hotfix: `hotfix/<issue-id>-<description>`
- Release: `release/v<version>`

## PR Body Template
```markdown
## What
[Summary of changes]

## Why
[Reason for change]

## Quality Checklist
- [ ] Tests pass
- [ ] CI/CD passes
- [ ] Review approved
```