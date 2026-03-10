# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Error Prone Support is a Picnic-opinionated extension of Google's Error Prone static analysis tool for Java. It provides additional `BugChecker` implementations and Refaster rules that improve code quality, focusing on maintainability, consistency, and avoidance of common pitfalls.

- **Group ID**: `tech.picnic.error-prone-support`
- **License**: MIT
- **Docs**: https://error-prone.picnic.tech

## Build commands

Building requires **JDK 25** (use `sdk env` with SDKMAN). The project targets JDK 21.

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

- `-Dverification.skip`: disable non-essential plugins (fastest build).
- `-Dverification.warn`: make warnings non-fatal.
- `-Pself-check`: apply this project's checks against itself.

## Module structure

| Module | Purpose |
|---|---|
| `error-prone-contrib` | Main bug checkers and Refaster rules |
| `error-prone-experimental` | Experimental checks not yet promoted |
| `error-prone-guidelines` | Guidelines-based checks |
| `error-prone-utils` | Shared utilities (`MoreASTHelpers`, `SourceCode`, `Documentation`, etc.) |
| `refaster-compiler` | Compiles Refaster rules |
| `refaster-runner` | Exposes Refaster rules as Error Prone checks |
| `refaster-support` | Annotations and matchers for Refaster rules |
| `refaster-test-support` | Testing utilities for Refaster rules |
| `documentation-support` | Documentation generation helpers |

## Architecture and patterns

### BugChecker development

Bug checkers mostly live in `error-prone-contrib/src/main/java/tech/picnic/errorprone/bugpatterns/`. The pattern:

1. Extend `BugChecker` and implement a matcher interface (e.g., `MethodInvocationTreeMatcher`).
2. Annotate with `@AutoService(BugChecker.class)` for auto-registration.
3. Annotate with `@BugPattern` specifying summary, `linkType = CUSTOM`, `link = BUG_PATTERNS_BASE_URL + "CheckName"`, severity, and tags.
4. Return `Description` with `SuggestedFix` for auto-fixes.
5. Use utilities from `error-prone-utils` (`MoreASTHelpers`, `SourceCode`, `MoreMatchers`, etc.).

### Refaster rule development

Refaster rules live in `error-prone-contrib/src/main/java/tech/picnic/errorprone/refasterrules/`. The pattern:

1. Create a `final class` named `{Topic}Rules` (e.g., `BigDecimalRules`) annotated with `@OnlineDocumentation`.
2. Define static inner classes, each with `@BeforeTemplate` and `@AfterTemplate` methods.
3. Use `Refaster.anyOf(...)` to match multiple before-patterns.
4. Apply `@UseImportPolicy(STATIC_IMPORT_ALWAYS)` where appropriate.
5. Use matchers from `refaster-support` (`IsEmpty`, `RequiresComputation`, etc.) to constrain parameter types where relevant.

### Testing patterns

Tests use JUnit 5 and follow this structure:

**Bug checker tests**: use `CompilationTestHelper` for identification tests and `BugCheckerRefactoringTestHelper` for fix tests:
```java
@Test
void identification() {
  CompilationTestHelper.newInstance(MyCheck.class, getClass())
      .addSourceLines("A.java", "// BUG: Diagnostic contains:", "...")
      .doTest();
}

@Test
void replacement() {
  BugCheckerRefactoringTestHelper.newInstance(MyCheck.class, getClass())
      .addInputLines("A.java", "...")
      .addOutputLines("A.java", "...")
      .doTest(TestMode.TEXT_MATCH);
}
```

Requirements:
- In identification tests, precede lines with expected diagnostics with `// BUG: Diagnostic contains:` comments.
- Try to have a single `identification` and `replacement` test per test class.
- In each case, try to have a single `A.java` file. Introduce additional files only if required.

**Refaster rule tests**: use the custom `refaster-test-support` module infrastructure.

## Code style

1. Favour `Optional` parameters and return types over `@Nullable` parameters and return types.

## Workflow

1. Implement the changes and associated tests.
2. Run the tests, and iterate until the tests pass.
3. Commit your changes.
4. Run a quick full build using `mvn clean install -DskipTests -Dverification.skip`.
5. Run `./run-branch-mutation-tests.sh` to determine mutation test coverage using Pitest (PIT) and try to resolve any surviving mutants listed in `<module>/target/pit-reports/mutations.csv`.
6. If there are changes, commit them.
7. Run `./apply-error-prone-suggestions.sh` to clean up the code; ask for help if this step fails.
8. Again, if there are changes, commit them.
9. Run `./run-full-build.sh` and attempt to resolve any build failures.
10. If applicable, commit changes once more.
