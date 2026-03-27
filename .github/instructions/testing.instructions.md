---
applyTo: "**/src/test/**"
---

# Testing Conventions

This document serves as the canonical reference for testing conventions in this
repository, for both AI coding agents and human contributors. It covers the
different testing patterns used across modules.

For detailed step-by-step guides on testing specific types of code, see also:

- [Bug checker testing][bug-checker-instructions] (Steps 3-4)
- [Refaster rule testing][refaster-instructions] (Steps 3-6)

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
output?).

### Identification tests

Use `CompilationTestHelper` to verify that the checker flags the expected
diagnostics. Place `// BUG: Diagnostic contains:` on the line immediately
before the flagged code.

```java
@Test
void identification() {
  CompilationTestHelper.newInstance(MyCheck.class, getClass())
      .addSourceLines(
          "A.java",
          "class A {",
          "  void m() {",
          "    // BUG: Diagnostic contains:",
          "    Optional.of(1).get();",
          "  }",
          "}")
      .doTest();
}
```

For checks that produce multiple distinct messages, use `expectErrorMessage()`
with a key and a lambda predicate:

```java
CompilationTestHelper.newInstance(MyCheck.class, getClass())
    .expectErrorMessage("key1", m -> m.contains("Prefer X"))
    .expectErrorMessage("key2", m -> m.contains("Prefer Y"))
    .addSourceLines(
        "A.java",
        "class A {",
        "  // BUG: Diagnostic matches: key1",
        "  ...",
        "}")
    .doTest();
```

Note that `// BUG: Diagnostic contains:` is the "any diagnostic message will
do" variant of the more general `// BUG: Diagnostic contains: <text>`
assertion. A `// BUG: Diagnostic contains: <text>` comment asserts that the
check reports a diagnostic on the subsequent line whose message contains
`<text>` as a substring. This form of validation is useful for checks with
dynamic diagnostic messages and for reducing duplication between
`identification()` and `replacement()` tests by matching against the suggested
replacement code. When using this approach, ensure that enough replacement
cases remain to validate that the suggested fix compiles.

### Replacement tests

Use `BugCheckerRefactoringTestHelper` to verify the suggested fix transforms
input code to expected output.

```java
@Test
void replacement() {
  BugCheckerRefactoringTestHelper.newInstance(MyCheck.class, getClass())
      .addInputLines(
          "A.java",
          "class A {",
          "  void m() {",
          "    Optional.of(1).get();",
          "  }",
          "}")
      .addOutputLines(
          "A.java",
          "class A {",
          "  void m() {",
          "    Optional.of(1).orElseThrow();",
          "  }",
          "}")
      .doTest(TestMode.TEXT_MATCH);
}
```

### Prefer single test files

Prefer a single `identification()` and `replacement()` test method per test
class. Prefer a single `A.java` test file per test. Introduce additional test
methods or files only when required (e.g., to test different flag
configurations or multi-file scenarios).

### Include negative cases, listed first

Always test code that should NOT trigger the check. List non-violating
(negative) cases before violating (positive) cases in the identification test.
Code without a `// BUG:` comment is implicitly a negative case.

### Testing flag-based configuration

For BugCheckers that accept flags, use `.setArgs()`:

```java
@Test
void replacementWithCustomFlag() {
  BugCheckerRefactoringTestHelper.newInstance(MyCheck.class, getClass())
      .setArgs("-XepOpt:MyCheck:FlagName=value")
      .addInputLines("A.java", ...)
      .addOutputLines("A.java", ...)
      .doTest(TestMode.TEXT_MATCH);
}
```

### Testing multiple suggested fixes

When a check provides multiple fix alternatives, use `.setFixChooser()` to
select which fix to test:

```java
BugCheckerRefactoringTestHelper.newInstance(MyCheck.class, getClass())
    .setFixChooser(SECOND)
    .addInputLines("A.java", ...)
    .addOutputLines("A.java", ...)
    .doTest(TestMode.TEXT_MATCH);
```

### Identification tests are comprehensive; replacement tests are focused

The `identification()` test should cover all edge cases, including negative
cases and all supported code patterns. The `replacement()` test only needs to
verify that the fix transformation is correct and yields valid code in all
relevant cases; it does not need to repeat all edge cases from
`identification()`.

## Refaster rule testing

Refaster rules are tested using input/output file pairs that demonstrate the
before and after of each rule. For detailed conventions on Refaster rule
design, see [refaster-rules.instructions.md][refaster-instructions].

### File pair convention

Each rule collection `{Topic}Rules.java` has:

- `src/test/resources/.../refasterrules/{Topic}RulesTestInput.java`
- `src/test/resources/.../refasterrules/{Topic}RulesTestOutput.java`

Both files define a class named `{Topic}RulesTest` that implements
`RefasterRuleCollectionTestCase`.

### Test method naming
<!-- check: Test method names match inner class names exactly (`testFooBar` for `FooBar`) -->

Each inner rule class `FooBar` in a rule collection must have a corresponding
`testFooBar()` method in both the input and output files of said rule
collection.

### Registration
<!-- check: Collection registered in `RefasterRulesTest.java` `RULE_COLLECTIONS` -->

New rule collections must be registered in `RefasterRulesTest.java`'s
`RULE_COLLECTIONS` set, in alphabetical order.

### `elidedTypesAndStaticImports()`
<!-- check: `elidedTypesAndStaticImports()` lists all replaced types/imports -->

Override this method in the test input class to preserve imports that are only
used in code replaced by rules. Without this, the compiler would flag unused
imports.

```java
@Override
public ImmutableSet<Object> elidedTypesAndStaticImports() {
  return ImmutableSet.of(
      Stream.class,
      collectingAndThen(null, null),
      counting());
}
```

### Avoid local variables in Refaster test code

Do not create local variables in Refaster test input/output files. Inline
expressions directly. This keeps tests minimal and matches the expression-level
granularity of Refaster rules. Refaster test methods must also be nullary (take
no parameters).

**Do:**

```java
Optional<String> testOptionalIsEmpty() {
  return Optional.of("foo").filter(String::isEmpty).isPresent();
}
```

**Don't:**

```java
Optional<String> testOptionalIsEmpty() {
  Optional<String> opt = Optional.of("foo");
  return opt.filter(String::isEmpty).isPresent();
}
```

For the full step-by-step guide, see
[refaster-rules.instructions.md][refaster-instructions].

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
