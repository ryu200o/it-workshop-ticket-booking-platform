# Project Boundary

This repository is limited to repo-local OpenCode workflow and boundary policy during Sprint 0.

- Keep global OpenCode config untouched.
- Use repo-local config to define project behavior.
- Treat Linear MCP as supporting context, not final authority.
- Keep GitHub as the source of truth for code, config, and docs.

TODO: RYU-57 will define AGENTS.md project rules.
TODO: RYU-58 will define project-local prompts and agent profiles.
TODO: RYU-59 will define repo-local multi-model opencode.json.
TODO: RYU-60 will define OpenCode workflow commands.
TODO: RYU-62 will define GitHub source-of-truth and secret handling policy.
TODO: RYU-63 will define Knowledge Vault automation policy.
TODO: RYU-65 will define local human review before PR workflow.

## Local Human Review Gate

This is a learning-oriented project.

For learning tasks, GitHub PR must not be the first review surface.

Default workflow:

1. Agent creates or updates a feature branch.
2. Agent makes local changes.
3. Agent stops before commit, push, or PR creation.
4. Human reviews local diff in IDE.
5. Human discusses findings with Architect/Mentor if needed.
6. Human explicitly approves commit.
7. Agent commits and pushes branch.
8. Agent creates PR.
9. Architect/Mentor reviews PR through GitHub connector.
10. Human and Architect/Mentor decide merge or rework.

Rules:

- Agents must not commit before local human review unless explicitly instructed.
- Agents must not push before local human review unless explicitly instructed.
- Agents must not create PR before local human review unless explicitly instructed.
- GitHub PR review is the formal review layer.
- Local IDE review is the learning layer.
- Merge requires explicit human approval.
