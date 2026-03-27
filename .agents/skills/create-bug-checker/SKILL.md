---
name: create-bug-checker
description: >
  Use this skill when asked to create or modify an Error Prone `BugChecker` in
  this repository. It covers choosing the right module, implementing the
  checker and tests, and following the repository validation workflow.
---

# Create `BugChecker`

Use this skill for requests to add or change files in `*/bugpatterns/*`.

Read [`.github/instructions/bug-checkers.instructions.md`][bug-checkers] for
the full conventions and step-by-step guide. Then:

1. **Determine the target module.** Based on the purpose of the requested
   checker, decide whether it belongs in `error-prone-contrib`
   (general-purpose), `error-prone-experimental` (experimental), or
   `error-prone-guidelines` (project-specific conventions).
2. **Create the checker file** following Step 1 (and Step 2 for advanced
   patterns) from the instructions.
3. **Create the test file** following Step 3.
4. **Verify** by running the tests as described in Step 4.
5. **Review** your changes against the applicable sections of
   `.github/instructions/review.instructions.md`.
6. **Follow the workflow** described in `AGENT.md` (mutation tests, apply
   suggestions, full build).

[bug-checkers]: ../../../.github/instructions/bug-checkers.instructions.md
