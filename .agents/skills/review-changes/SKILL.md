---
name: review-changes
description: >
  Use this skill when reviewing code changes against the project's review
  checklist. Guides multi-pass structured review with progressive focus areas.
---

# Review Changes

Use this skill after implementing changes, or when asked to review code.

Read [`.github/instructions/review.instructions.md`][review] for the full
checklist. Then:

1. **Determine applicable sections** by identifying which file types were
   changed (Java, workflows, scripts, documentation, etc.).
   * Any `*.md` changes always require checking the _Documentation Conventions_
     section.
2. **First pass -- correctness**: Walk through each applicable checklist item.
   Flag and fix violations.
3. **Second pass -- simplification**: Question whether each method,
   abstraction, and conditional is necessary. Eliminate redundancy, flatten
   complexity, reduce code to its minimal correct form.
4. **Third pass -- different lens**: Focus on an aspect not yet covered (e.g.,
   naming, test coverage, error handling). Continue passes until diminishing
   returns.
5. **Commit** any improvements.
6. **Finalize**: Invoke [`/finalize-changes`][skill-finalize-changes] to run
   the full build, mutation tests, and self-check.

[review]: ../../../.github/instructions/review.instructions.md
[skill-finalize-changes]: ../finalize-changes/SKILL.md
