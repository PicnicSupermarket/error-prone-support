---
applyTo: "**/src/test/**"
---

# Testing Conventions

This document serves as the canonical reference for testing conventions in this
repository, for both AI coding agents and human contributors. It covers the
different testing patterns used across modules.

For domain-specific testing conventions, see also:

- [Bug checker testing][bug-checker-instructions] (test file structure)
- [Refaster rule testing][refaster-instructions] (test input/output files)

## General conventions

### Test class structure

- All test classes must be `final`.
- Test class naming: `{ClassName}Test`.
- Use JUnit 5 (`@Test`, `@ParameterizedTest`, `@ValueSource`, `@MethodSource`).
- Avoid `@BeforeEach` / `@AfterEach` lifecycle methods; prefer self-contained
  tests without shared mutable state.
- Test code follows the same style conventions as production code (immutable
  collections, functional style, etc.). See
  [java-style.instructions.md][java-style-instructions].

### Test method naming

- `BugChecker` tests use `identification()` and `replacement()`.
- Refaster rule tests use `test{RuleName}()` methods.
- Other tests use descriptive method names.

### Prefer parameterized tests over multiple similar test methods

When multiple test methods differ only in their input values or configuration,
use `@ParameterizedTest` with `@MethodSource` or `@ValueSource` instead of
separate methods. This reduces duplication and makes it easier to add new test
cases.

### Keep test inputs minimal

Use the simplest possible dummy values in test fixtures. Tests should be easy
to read and focused on the behavior under test.

### Use distinct values per test line

This applies to Refaster tests and `BugChecker` replacement tests.

Use different numbers and strings on each line in test cases so that
correctness of rewriting is verifiable. If two lines used the same values, a
faulty rewrite that swaps arguments would go undetected.

**Do:**

```java
ImmutableSet.of(
    Math.clamp(1, 2, 3),
    Math.clamp(4, 5, 6));
```

**Don't:**

```java
ImmutableSet.of(
    Math.clamp(1, 2, 3),
    Math.clamp(1, 2, 3));
```

### Use metasyntactic variable names

For string test values, use: `"foo"`, `"bar"`, `"baz"`, `"qux"`, `"quux"`,
`"quuz"`, `"corge"` and other metasyntactic variables, in their canonical
order. For integers, use `1`, `2`, `3`, etc. For detailed conventions on
Refaster rule design, see
[refaster-rules.instructions.md][refaster-instructions].

### Format test source strings with Google Java Format

Source code embedded in test strings must follow Google Java Format (2-space
indentation). Apply the same formatting rules as production code.

## `BugChecker` testing

`BugChecker` tests use a two-phase pattern: identification (does the check flag
the right code?) and replacement (does the suggested fix produce correct
output?). For the complete conventions, templates, and examples, see
[`bug-checkers.instructions.md`][bug-checker-instructions].

## Refaster rule testing

Refaster rules are tested using input/output file pairs. For the complete
conventions, templates, and examples, see
[`refaster-rules.instructions.md`][refaster-instructions].

## Utility and matcher testing

Utilities and matchers in `error-prone-utils` and `refaster-support` require a
different testing approach because they are not `BugChecker`s themselves.

### Wrapper `BugChecker` pattern for matchers

To test a `Matcher<ExpressionTree>` implementation, create a private inner
`BugChecker` that wraps it using `AbstractMatcherTestChecker`:

```java
final class IsEmptyTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  // BUG: Diagnostic contains:",
            "  Object m() { return ImmutableList.of(); }",
            "  Object negative() { return ImmutableList.of(1); }",
            "}")
        .doTest();
  }

  @BugPattern(summary = "Flags expressions matched by `IsEmpty`", severity = ERROR)
  private static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    private MatcherTestChecker() {
      super(new IsEmpty());
    }
  }
}
```

### Testing utility methods

Utility methods that operate on AST nodes (e.g., `MoreASTHelpers`) are tested
using the same `CompilationTestHelper` approach with a custom wrapper
`BugChecker` that exercises the utility method and reports results as
diagnostics.

## Documentation module testing

Tests in `documentation-support` verify documentation generation from Java
source annotations. They typically:

- Compile test source code using
  `Compilation.compileWithDocumentationGenerator()`.
- Verify generated JSON files using `Json.read()` and AssertJ assertions.
- Use `@TempDir` for temporary output directories.

## Mutation testing

### Run mutation tests

After all tests pass, run mutation tests to verify coverage:

```sh
./run-branch-mutation-tests.sh
```

This runs Pitest with `EXTENDED` and `STRONGER` mutator sets. Results are
written to `<module>/target/pit-reports/mutations.csv`.

### Kill every killable mutant

Every mutant that can be killed must be killed. If a surviving mutant indicates
a gap in test coverage, add a test case; even if the test case seems contrived.
High mutation test coverage is a hard requirement.

Surviving mutants may also indicate opportunities to simplify production code.
If a mutation survives because a code path is unreachable, consider removing
the dead code.

### Interpreting mutation results
<!-- check: Mutation test results analyzed and surviving mutants addressed -->

Each line in `mutations.csv` describes a mutation and its status:

- `KILLED`: a test detected the mutation (good)
- `SURVIVED`: no test detected the mutation (fix this)
- `NO_COVERAGE`: no test covers this line (fix this)
- `TIMED_OUT`: mutation caused infinite loop (usually acceptable)

Focus on `SURVIVED` and `NO_COVERAGE` entries. For each one, either add a test
that detects the mutation or verify that the mutated code path is genuinely
unreachable (and simplify accordingly).

### Document unkillable mutants

When a surviving mutant is genuinely unkillable (such as a performance
optimization that does not affect observable behavior), add an `// XXX:`
comment next to the code explaining why the mutant survives and why the code is
intentional. This prevents future developers from wasting time trying to kill
it.

A common case is a short-circuit guard that improves performance but is not
observable in test output. Mark such guards with a `/* Fast path: <reason>. */`
comment to signal that the early return is intentional and the resulting mutant
is unkillable.

[bug-checker-instructions]: bug-checkers.instructions.md
[java-style-instructions]: java-style.instructions.md
[refaster-instructions]: refaster-rules.instructions.md
