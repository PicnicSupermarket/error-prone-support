---
name: refaster-rule
description: >
  Use this skill when asked to create or modify Refaster rules in this
  repository. It covers choosing the right rule collection, updating test
  fixtures and registration, and following the repository validation workflow.
---

# Refaster Rule

Use this skill for requests to add or change files in `*/refasterrules/*`.

Read [`.github/instructions/refaster-rules.instructions.md`][refaster-rules]
for the full conventions. Then:

1. **Determine the target collection** based on the topic: add to an existing
   `{Topic}Rules.java` or create a new one.
2. **Implement the rule** following the conventions in the instructions (rule
   file structure and advanced patterns).
3. **Write the test input and output files** following the test conventions.
4. **Register the collection** (if new) in `RefasterRulesTest.java`.
5. **Verify** by running the tests.
6. **Review** all changes against
   [`.github/instructions/review.instructions.md`][review].
7. **Follow the workflow** in [`AGENT.md`][agent-md] (apply suggestions, full
   build).

[agent-md]: ../../../AGENT.md
[refaster-rules]: ../../../.github/instructions/refaster-rules.instructions.md
[review]: ../../../.github/instructions/review.instructions.md
