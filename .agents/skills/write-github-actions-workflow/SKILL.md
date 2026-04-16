---
name: write-github-actions-workflow
description: >
  Use this skill when creating or modifying GitHub Actions workflow files in
  this repository. Covers action pinning, security hardening, permissions,
  naming, and formatting conventions.
---

# Write GitHub Actions Workflow

Use this skill for requests to add or change files in `.github/workflows/*`.

* Read [`.github/instructions/github-actions.instructions.md`][github-actions]
  for the full conventions. Pay particular attention to the local testing
  section: every `step-security/harden-runner` step requires
  `if: ${{ !env.ACT }}`.
* After implementation, review your changes against the applicable sections of
  [`.github/instructions/review.instructions.md`][review].
* Finally, follow the workflow in [`AGENT.md`][agent-md].

[agent-md]: ../../../AGENT.md
[github-actions]: ../../../.github/instructions/github-actions.instructions.md
[review]: ../../../.github/instructions/review.instructions.md
