# CLAUDE.md

This file provides guidance to AI coding agents such as Claude Code, GitHub
Copilot and OpenAI Codex when working with code in this repository.

## Project overview

Error Prone Support is a Picnic-opinionated extension of Google's
Error Prone static analysis tool for Java. It provides additional
`BugChecker` implementations and Refaster rules that improve code
quality, focusing on maintainability, consistency, and avoidance of
common pitfalls.

- **Group ID**: `tech.picnic.error-prone-support`
- **License**: MIT
- **Docs**: https://error-prone.picnic.tech

See [CONTRIBUTING.md][contributing] for development patterns,
architecture, and testing conventions.

## Build commands

Building requires **JDK 25** (use `sdk env` with SDKMAN). The
project targets JDK 21.

```sh
# Standard build and install.
mvn clean install

# Fast build skipping checks.
mvn clean install -Dverification.skip

# Build with non-fatal warnings.
mvn clean install -Dverification.warn

# Format code (google-java-format).
mvn fmt:format

# Full build: vanilla Error Prone + Picnic fork with self-check.
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

- `-Dverification.skip`: disable non-essential plugins (fastest
  build).
- `-Dverification.warn`: make warnings non-fatal.
- `-Pself-check`: apply this project's checks against itself.

## Workflow

1.  Implement the changes and associated tests.
2.  Run the tests, and iterate until the tests pass.
3.  Commit your changes.
4.  Carefully review your changes (both code and tests) and bring them down to
    their essence, while fully complying with repository best practices and
    maximizing test coverage. Apply all suggested improvements.
5.  Repeat the previous step with a fresh look, and keep doing this until
    reaching diminishing returns.
6.  Commit your new changes (if any).
7.  Run a quick full build using `mvn clean install -DskipTests
    -Dverification.skip`.
8.  Run `./run-branch-mutation-tests.sh` to determine mutation test coverage
    using Pitest (PIT) and try to resolve any surviving mutants listed in
    `<module>/target/pit-reports/mutations.csv`.
9.  If there are changes, commit them.
10. Run `./apply-error-prone-suggestions.sh` to clean up the code and inspect
    the resultant changes using `git diff`.
    - If this command fails, try to understand why and fix the issue.
    - If there are changes, validate that they make sense. If not, and they are
      due to newly introduced changes, undo the changes and attempt to fix the
      bug.
11. Once again, if there are changes, commit them.
12. Run `./run-full-build.sh` and attempt to resolve any build failures.
13. If applicable, commit changes once more.

## Task-specific instructions

When working on Refaster rules (files in `refasterrules/` directories), read
`.github/instructions/refaster-rules.instructions.md` for detailed conventions
and step-by-step instructions.

When working on `BugChecker` implementations (files in `bugpatterns/`
directories), read `.github/instructions/bug-checkers.instructions.md` for
detailed conventions and step-by-step instructions.

[contributing]: CONTRIBUTING.md
