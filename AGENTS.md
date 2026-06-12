# Project Rules

This repository defines repo-local OpenCode workflow and project rules for Sprint 0.

## Project Goal

This is a learning-oriented backend portfolio project.
The product is the IT Workshop Ticket Booking Platform.
The real goal is to strengthen backend fundamentals through deliberate design and implementation practice.

## Workflow

Default collaboration flow is Human Design -> AI Implement -> Human Review.
For learning tasks, local human review happens before commit, push, or PR creation.

## Current Stack Direction

- Java 25
- Spring Boot
- PostgreSQL
- Docker
- Modular Monolith first
- Event-driven concepts later

## Engineering Principles

Prioritize:

- Database design
- SQL thinking
- Transactions
- HTTP fundamentals
- Architecture
- Business rules
- Engineering trade-offs

Avoid:

- Premature microservices
- Premature Kafka
- Premature Redis
- Framework tricks before fundamentals
- Blind code generation

## Agent Behavior

- Do not jump directly into application code.
- Clarify requirements, domain model, API contract, data model, transaction boundary, and failure cases before implementation.
- Challenge over-engineering.
- Mark important lessons as Learning Candidate.

## Source Of Truth

- Linear: planning and backlog
- GitHub: code, config, and docs
- Knowledge Vault: learning notes

## Linear MCP Policy

- Linear MCP is context, not authority.
- Agents may read Linear issues.
- Agents must not manage backlog unless explicitly asked.

## GitHub And Secret Policy

- Repo may document environment variable names only.
- Never commit secret values.
- Project environment variables must use the `IT_BOOKING_*` naming convention.

## Knowledge Vault Policy

- Agents may propose notes.
- Agents must not write to `~/Knowledge` unless explicitly asked.

## Repo Boundary

- Keep global OpenCode config untouched.
- Use repo-local config to define project behavior.
- Keep GitHub as the source of truth for code, config, and docs.

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

## Hard Rules

- No application code before accepted design.
- No global OpenCode config modification.
- No secret values in the repo.
- No merge without explicit human approval.
