---
name: cicd-pipeline
description: GitHub Actions CI/CD configuration and monitoring
---

# CI/CD Pipeline (GitHub Actions)

## Trigger Pipeline
- Automatic: On PR creation/update
- Manual: `gh workflow run ci.yml --ref feature/branch`

## Monitor Status
```bash
# Check workflow runs
gh run list --branch feature/<task-name>
gh run view <run-id>
```

## Success Criteria

    ✅ Build passes

    ✅ Tests pass (unit + integration)

    ✅ Quality checks pass (SonarQube)

    ✅ Package created

## Failure Handling
- Build failed → Check `./mvnw compile -X`
- Test failed → Check `target/surefire-reports/*.txt`
- Quality check failed → Check SonarQube dashboard

## Report
Tell Orchestrator if pipeline passes or fails with quick reason.
