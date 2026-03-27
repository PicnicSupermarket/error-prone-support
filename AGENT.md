# Agent Instructions

This file provides guidance to AI coding agents such as Claude Code, Google
Gemini, GitHub Copilot and OpenAI Codex when working with code in this
repository.

## Project overview

Error Prone Support is a Picnic-opinionated extension of Google's Error Prone
static analysis tool for Java. It provides additional `BugChecker`
implementations and Refaster rules that improve code quality, focusing on
maintainability, consistency, and avoidance of common pitfalls.

- **Maven Group ID**: `tech.picnic.error-prone-support`
- **License**: MIT
- **Docs**: https://error-prone.picnic.tech

See the [task-specific instructions](#task-specific-instructions) below and the
files in `.github/instructions/` for detailed conventions.
[CONTRIBUTING.md][contributing] provides additional context for human
contributors (bug reporting, PR process, module overview).

This project demands the highest quality bar. Favour correctness and precision
over speed. Do not cut corners, skip edge cases, or settle for "good enough".

## Build commands

Building requires **JDK 25** (use `sdk env` with SDKMAN). The project targets
JDK 21.

```sh
# Standard build and install.
mvn clean install

# Fast build skipping checks.
mvn clean install -Dverification.skip

# Build with non-fatal warnings.
mvn clean install -Dverification.warn

# Format code (google-java-format).
mvn fmt:format

# Full build: first against vanilla Error Prone, then against Picnic fork with
self-check.
./run-full-build.sh

# Apply Error Prone auto-fixes to this project (also formats).
./apply-error-prone-suggestions.sh

# Run mutation tests on changed code (vs. upstream default branch).
./run-branch-mutation-tests.sh
```

### Running a single test

```sh
mvn test -pl error-prone-contrib -Dtest=DirectReturnTest -Dverification.skip
```

Replace the module (`-pl`) and test class (`-Dtest=`) as needed.

### Key Maven flags

- `-Dverification.skip`: disable non-essential plugins (fastest build).
- `-Dverification.warn`: make warnings non-fatal.
- `-Pself-check`: apply this project's checks against itself.

## Workflow

Implementation tasks must adhere to the following procedure:

1.  Create a solid plan before attempting implementation.
    * Tip: use `/using-superpowers` (Claude Code) or similar.
2.  Implement the desired changes and associated tests. When creating
    new files that must ultimately be committed, stage them immediately
    using `git add <file>`.
3.  Run the tests, and iterate until the tests pass.
4.  Commit your changes. Follow the conventions in
    [`.github/instructions/commit-message.instructions.md`][commit-message].
5.  Critically review your changes (both code and tests). Go through the
    applicable sections of
    [`.github/instructions/review.instructions.md`][review] and verify each
    item. Question whether each method, abstraction, and conditional is
    necessary. Simplify aggressively: eliminate redundancy, flatten unnecessary
    complexity, and reduce the code to its minimal correct form.
    * Use all code review skills available to you. Execute as adversarial
      subagents.
6.  Repeat the previous step, focusing on a different aspect each pass (e.g.
    control flow, then naming, then test coverage). Continue until diminishing
    returns.
7.  Commit your new changes (if any).
8.  Run a quick full build using `mvn clean install -DskipTests
    -Dverification.skip`.
9.  Run `./run-branch-mutation-tests.sh` to determine mutation test coverage
    using Pitest (PIT) and try to resolve any surviving mutants listed in
    `<module>/target/pit-reports/mutations.csv`. See
    [`.github/instructions/testing.instructions.md`][testing] for guidance on
    interpreting results and documenting unkillable mutants.
10. If there are changes, commit them.
11. Run `./apply-error-prone-suggestions.sh` to clean up the code and inspect
    the resultant changes using `git diff`.
    - If this command fails, try to understand why and fix the issue.
    - If there are changes, validate that they make sense. If not, and they are
      due to newly introduced changes, undo the changes and attempt to fix the
      bug.
12. Once again, if there are changes, commit them.
13. Run `./run-full-build.sh` and attempt to resolve any build failures.
    Be efficient by attempting to resolve multiple warnings and errors in one
    go.
14. If applicable, commit changes once more.

## Code style

When writing or modifying Java code in this repository, follow the conventions
in [`.github/instructions/java-style.instructions.md`][java-style].

When writing or modifying tests, also follow
[`.github/instructions/testing.instructions.md`][testing].

## Task-specific instructions

When working on Refaster rules (files with `*/refasterrules/*` paths), read
[`.github/instructions/refaster-rules.instructions.md`][refaster-instructions]
for detailed conventions and step-by-step instructions.

When working on `BugChecker` implementations (files with `*/bugpatterns/*`
paths), read
[`.github/instructions/bug-checkers.instructions.md`][bug-checker-instructions]
for detailed conventions and step-by-step instructions.

When working on GitHub Actions workflows (files in `.github/workflows/`), read
[`.github/instructions/github-actions.instructions.md`][github-actions] for
detailed conventions.

When working on shell scripts (`*.sh`), read
[`.github/instructions/scripts.instructions.md`][scripts] for
detailed conventions.

## Writing conventions

When writing or editing documentation (Markdown files, comments), follow
[`.github/instructions/documentation.instructions.md`][documentation].

When writing or editing instruction files, also follow
[`.github/instructions/instructions.instructions.md`][instructions].

When writing or editing skill files, follow
[`.github/instructions/skills.instructions.md`][skills].

For the conventions governing agent configuration files themselves, see
[`.github/instructions/agents.instructions.md`][agents].

Whenever creating a commit, follow
[`.github/instructions/commit-message.instructions.md`][commit-message].

When creating a pull request, follow
[`.github/instructions/pull-request.instructions.md`][pull-request] for title,
label, milestone, reviewer, and description conventions.

[agents]: .github/instructions/agents.instructions.md
[bug-checker-instructions]: .github/instructions/bug-checkers.instructions.md
[commit-message]: .github/instructions/commit-message.instructions.md
[contributing]: CONTRIBUTING.md
[documentation]: .github/instructions/documentation.instructions.md
[github-actions]: .github/instructions/github-actions.instructions.md
[instructions]: .github/instructions/instructions.instructions.md
[java-style]: .github/instructions/java-style.instructions.md
[pull-request]: .github/instructions/pull-request.instructions.md
[refaster-instructions]: .github/instructions/refaster-rules.instructions.md
[review]: .github/instructions/review.instructions.md
[scripts]: .github/instructions/scripts.instructions.md
[skills]: .github/instructions/skills.instructions.md
[testing]: .github/instructions/testing.instructions.md
