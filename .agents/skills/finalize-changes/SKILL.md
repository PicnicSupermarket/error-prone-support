---
name: finalize-changes
description: >
  Use this skill after completing implementation to run the full
  post-implementation validation workflow: structured review, mutation testing,
  self-check, and full build.
---

# Finalize Changes

Use this skill when implementation is complete and you need to validate before
creating a PR.

Follow [`AGENT.md`][agent-md] steps 5-14:

1. **Review** (5-7): [`/review-changes`][skill-review-changes], then commit.
2. **Quick build** (8): `mvn clean install -DskipTests -Dverification.skip`.
3. **Mutation testing** (9-10):
   [`/run-mutation-tests`][skill-run-mutation-tests], then commit.
4. **Self-check** (11-14): [`/apply-self-check`][skill-apply-self-check], then
   commit.

Re-invoke [`/review-changes`][skill-review-changes] after phases with
significant changes.

[agent-md]: ../../../AGENT.md
[skill-apply-self-check]: ../apply-self-check/SKILL.md
[skill-review-changes]: ../review-changes/SKILL.md
[skill-run-mutation-tests]: ../run-mutation-tests/SKILL.md
