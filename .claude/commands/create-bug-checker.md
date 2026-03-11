Create a new `BugChecker` implementation based on the following description:

$ARGUMENTS

## Instructions

Read `.github/instructions/bug-checkers.instructions.md` for the
full conventions and step-by-step guide. Then:

1. **Determine the target module.** Based on the purpose of the
   requested checker, decide whether it belongs in
   `error-prone-contrib` (general-purpose),
   `error-prone-experimental` (experimental), or
   `error-prone-guidelines` (project-specific conventions).
2. **Create the checker file** following Step 1 (and Step 2 for
   advanced patterns) from the instructions.
3. **Create the test file** following Step 3.
4. **Verify** by running the tests as described in Step 4.
5. **Follow the workflow** described in `AGENT.md` (mutation tests,
   apply suggestions, full build).
