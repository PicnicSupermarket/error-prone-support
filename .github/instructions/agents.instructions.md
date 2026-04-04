---
applyTo: "**/AGENT.md,**/CLAUDE.md,**/GEMINI.md,.github/copilot-instructions.md"
---

# Agent Configuration Conventions

This document describes how AI coding agent configuration files are organized
in this project.

## Agent-agnostic philosophy
<!-- check: skip -->

This project uses an agent-agnostic approach to AI coding guidance. The bulk of
conventions and instructions live in `.github/instructions/*.instructions.md`
files, which are readable by both humans and any AI agent. Agent-specific
configuration is kept to a minimum.

## `AGENT.md` is the canonical entry point
<!-- check: `AGENT.md` is the single source of truth for agent workflow -->

`AGENT.md` in the repository root is the canonical entry point for all AI
coding agents. It contains:

- Project overview and build commands.
- The implementation workflow (plan, implement, test, review, verify).
- References to task-specific instruction files.

Do not duplicate `AGENT.md` content in agent-specific files.

## Use symlinks to avoid duplication across agent platforms
<!-- check: Agent-specific files use symlinks to canonical sources -->

When multiple agent platforms need the same content, use symbolic links rather
than duplicating files. This applies to:

- **Agent entry points**: `CLAUDE.md`, `GEMINI.md`,
  `.github/copilot-instructions.md` etc. should be symlinks to `AGENT.md`
  unless the agent requires a genuinely different format.
- **Skills directories**: agent-specific skill directories (e.g.,
  `.claude/skills`) should be symlinks to `.agents/skills`.
- **Other shared config**: any configuration that is logically the same across
  agents should live in one canonical location with symlinks from
  agent-specific paths.

If an agent-specific addition is needed (e.g., Claude hooks in
`.claude/hooks/`), keep it in the agent's own directory rather than in the
shared location.

## Follow standard agent specifications
<!-- check: skip -->

This project follows the [AGENTS.md][agents-md] and [Agent
Skills][agent-skills] specifications. For agents that support these
specifications natively, custom entry-point files or symlinks are redundant and
should be omitted in favour of the spec-standard locations.

If an agent detects such redundant files (e.g., after a platform upgrade adds
native support for a specification), it should suggest removing the
customization.

## Detailed conventions live in instruction files
<!-- check: skip -->

`AGENT.md` references `.github/instructions/*.instructions.md` files for
detailed conventions. See [`instructions.instructions.md`][instructions] for
the conventions governing these instruction files themselves.

## Human contributors use `CONTRIBUTING.md`
<!-- check: skip -->

`CONTRIBUTING.md` is the entry point for human contributors. It covers bug
reporting, PR process, and module overview, and cross-references the
instruction files for detailed coding conventions.

[agent-skills]: https://agentskills.io
[agents-md]: https://agents.md
[instructions]: instructions.instructions.md
