---
name: github-protocol
description: GitHub operations - PR creation, releases, tags
---

# GitHub Protocol

## PR Creation
```bash
# Push branch
git push -u origin feature/<task-name>

# Create PR
gh pr create --title "feat: description" --body "Details" --base main
```

## Releases
```bash
# Create tag
git tag -a v1.2.3 -m "Release v1.2.3"
git push origin v1.2.3

# Create release
gh release create v1.2.3 --title "v1.2.3" --notes-file CHANGELOG.md
```

## Status Check
```bash
# Check PR status
gh pr status
gh pr checks --watch
```