# Local Agent Workflow

## Configuration Boundary

Global OpenCode config defines user-wide defaults and must remain untouched by this repository.
Repo-local OpenCode config defines repository behavior and is the only configuration surface that this project should version and review.

## Runtime Model

OpenChamber is the UI and workspace layer used to interact with agents and repository context.
OpenCode is the core runtime that loads config, instructions, tools, and repo-local workflow behavior.

## Source Of Truth Rules

Linear MCP provides issue and project context, but it is not the final authority for implementation decisions.
GitHub is the source of truth for code, configuration, and repository documentation.
Knowledge Vault is the source of truth for learning notes, working insights, and personal or team knowledge capture.

## Scope For Sprint 0

This repository currently stores only the minimal repo-local boundary needed to guide future workflow files.
No application code, Spring Boot setup, database schema, Docker config, or secret values belong in this step.
