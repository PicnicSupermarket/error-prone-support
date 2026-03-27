---
applyTo: "**/.agents/skills/**"
---

# Skill File Conventions

This document describes how AI agent skill files are organized in this project.

## Skills are small and reference instruction files
<!-- check: Skill file is small (under 30 lines) and references instruction files -->

Skill files in `.agents/skills/*/SKILL.md` should be thin pointers that tell
the agent which instruction files to read and which workflow to follow. Do not
duplicate instruction file content in a skill.

A typical skill file contains:

1. Frontmatter with `name` and `description` (used for skill
   triggering/discovery).
2. A one-line description of when to use the skill.
3. References to the relevant `.github/instructions/*.instructions.md` files.
4. A reference to the review checklist.
5. A reference to the `AGENT.md` workflow.

**Example:**

```markdown
---
name: write-java-code
description: >
  Use this skill when writing or modifying general Java code in this
  repository.
---

# Write Java Code

Use this skill when working on general Java code outside `*/bugpatterns/*` and
`*/refasterrules/*`.

Read `.github/instructions/java-style.instructions.md` for code style
conventions and `.github/instructions/testing.instructions.md` for testing
patterns. After implementation, review your changes against the applicable
sections of `.github/instructions/review.instructions.md`. Then follow the
workflow in `AGENT.md`.
```

## Skill description determines when the skill triggers
<!-- check: Skill `description` clearly states when to use the skill -->

The `description` field in the frontmatter is used by agent platforms to decide
when to activate the skill. Write it as a clear trigger condition: "Use this
skill when..." followed by the specific context.

## One skill per task type
<!-- check: skip -->

Each distinct type of task (creating a `BugChecker`, writing a Refaster rule,
modifying a workflow, writing general Java code, etc.) should have its own
skill. Do not combine unrelated tasks into a single skill.
