---
name: bug-checker
description: >
  Use this skill when asked to create or modify an Error Prone `BugChecker` in
  this repository. It covers choosing the right module, implementing the
  checker and tests, and following the repository validation workflow.
---

# Bug Checker

Use this skill for requests to add or change files in `*/bugpatterns/*`.

Read [`.github/instructions/bug-checkers.instructions.md`][bug-checkers] for
the full conventions. Then:

1. **Determine the target module** based on the purpose of the checker:
   `error-prone-contrib` (general-purpose), `error-prone-experimental`
   (experimental), or `error-prone-guidelines` (project-specific).
2. **Implement the checker** following the conventions in the instructions
   (checker file structure and advanced patterns).
3. **Write the test** following the test file structure conventions.
4. **Verify** by running the tests.
5. **Review** your changes against
   [`.github/instructions/review.instructions.md`][review].
6. **Follow the workflow** described in [`AGENT.md`][agent-md] (mutation tests,
   apply suggestions, full build).

[agent-md]: ../../../AGENT.md
[bug-checkers]: ../../../.github/instructions/bug-checkers.instructions.md
[review]: ../../../.github/instructions/review.instructions.md
