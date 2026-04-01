---
applyTo: "**/bugpatterns/**"
---

# Bug Checker Conventions

This document describes the conventions for `BugChecker` implementations and
tests in this project. It serves as the canonical reference for all AI coding
agents and human contributors.

For general Java style conventions (collections, nullability, imports, etc.),
see [`java-style.instructions.md`][java-style]. For general testing
conventions, see [`testing.instructions.md`][testing].

## Overview
<!-- check: skip -->

A [`BugChecker`][bug-checker] is an Error Prone analysis pass that visits
specific AST node types and optionally suggests a fix. This project extends
Error Prone with additional checkers for code quality, maintainability, and
consistency. See Error Prone's [criteria for new checks][error-prone-criteria]
for general guidance; this project additionally focuses on style enforcement.

## File locations
<!-- check: Checker is in the correct module (contrib vs experimental vs guidelines) -->

| Purpose | Path |
|---------|------|
| Checker source | `{module}/src/main/java/tech/picnic/errorprone/bugpatterns/{CheckName}.java` |
| Test class | `{module}/src/test/java/tech/picnic/errorprone/bugpatterns/{CheckName}Test.java` |
| Package info | `{module}/src/main/java/tech/picnic/errorprone/bugpatterns/package-info.java` (already exists) |

Where `{module}` is one of:

| Module | Use for |
|--------|---------|
| `error-prone-contrib` | General-purpose checks applicable to any Java codebase |
| `error-prone-experimental` | Experimental checks not yet ready for general use |
| `error-prone-guidelines` | Checks enforcing conventions specific to this project itself |

The same conventions apply to all three modules.

## Checker file structure
<!-- check: `serialVersionUID = 1L` is present -->
<!-- check: Public no-arg constructor with Javadoc is present -->
<!-- check: `SourceCode#treeToString` used instead of `Tree#toString()` -->

Checker files are placed in the appropriate module's `bugpatterns/` directory as
`{CheckName}.java`. A complete checker looks like this:

```java
package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.XxxTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.XxxTree;

/** A {@link BugChecker} that flags {description}. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Imperative sentence describing what to do instead",
    link = BUG_PATTERNS_BASE_URL + "{CheckName}",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class {CheckName} extends BugChecker implements XxxTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link {CheckName}} instance. */
  public {CheckName}() {}

  @Override
  public Description matchXxx(XxxTree tree, VisitorState state) {
    if (!shouldFlag(tree, state)) {
      return Description.NO_MATCH;
    }

    return describeMatch(tree, SuggestedFix.replace(tree, "replacement"));
  }
}
```

Conventions:
- **Class-level Javadoc**: `/** A {@link BugChecker} that flags {description}. */`
- **`@AutoService(BugChecker.class)`**: Provides auto-registration on the
  classpath (no manual registration step needed, unlike Refaster rules).
- **`@BugPattern` fields**:
  - `summary`: An imperative sentence (e.g., "Prefer X over Y").
  - `link`: Always `BUG_PATTERNS_BASE_URL + "{CheckName}"`.
    `BUG_PATTERNS_BASE_URL` is defined in
    [`Documentation.java`][documentation-java].
  - `linkType`: Always `CUSTOM`.
  - `severity`: One of `ERROR`, `WARNING`, or `SUGGESTION`.
  - `tags`: One or more from `StandardTags`: `CONCURRENCY`, `FRAGILE_CODE`,
    `LIKELY_ERROR`, `PERFORMANCE`, `SIMPLIFICATION`, `STYLE` and `REFACTORING`.
- **`private static final long serialVersionUID = 1L;`**: Required because
  `BugChecker` is `Serializable`.
- **Explicit public nullary constructor** with Javadoc: `/** Instantiates a new
  {@link CheckName} instance. */`
- **`matchXxx()` return values**: `Description.NO_MATCH` for non-matches, and
  generally `describeMatch(tree, fix)` for matches.

### Real example: `IsInstanceLambdaUsage`
<!-- check: skip -->

This is just about the simplest complete checker with a fix:

```java
/**
 * A {@link BugChecker} that flags lambda expressions that can be replaced with a method reference
 * of the form {@code T.class::isInstance}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Prefer `Class::isInstance` method reference over equivalent lambda expression",
    link = BUG_PATTERNS_BASE_URL + "IsInstanceLambdaUsage",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class IsInstanceLambdaUsage extends BugChecker
    implements LambdaExpressionTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link IsInstanceLambdaUsage} instance. */
  public IsInstanceLambdaUsage() {}

  @Override
  public Description matchLambdaExpression(LambdaExpressionTree tree, VisitorState state) {
    if (tree.getParameters().size() != 1
        || !(tree.getBody() instanceof InstanceOfTree instanceOf)) {
      return Description.NO_MATCH;
    }

    VariableTree param = Iterables.getOnlyElement(tree.getParameters());
    if (!ASTHelpers.getSymbol(param).equals(ASTHelpers.getSymbol(instanceOf.getExpression()))) {
      return Description.NO_MATCH;
    }

    return describeMatch(
        tree,
        SuggestedFix.replace(
            tree, SourceCode.treeToString(instanceOf.getType(), state) + ".class::isInstance"));
  }
}
```

## Advanced patterns
<!-- check: skip -->

### Static `Matcher<>` / `MultiMatcher<>` fields
<!-- check: skip -->

Define static matcher fields to check for AST nodes that represent specific
types or symbols. Example from `AutowiredConstructor`:

```java
private static final MultiMatcher<Tree, AnnotationTree> AUTOWIRED_ANNOTATION =
    annotations(AT_LEAST_ONE, isType("org.springframework.beans.factory.annotation.Autowired"));
```

### Multiple matcher interfaces
<!-- check: skip -->

A checker can implement multiple matcher interfaces to visit different node
types. Simply implement each interface and provide the corresponding
`matchXxx()` method:

```java
public final class MyCheck extends BugChecker implements ClassTreeMatcher, MethodTreeMatcher {
  @Override
  public Description matchClass(ClassTree tree, VisitorState state) { ... }

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) { ... }
}
```

### `SuggestedFix` options
<!-- check: skip -->

Common fix patterns:

```java
// Simple replacement.
SuggestedFix.replace(tree, "newCode");

// Delete a node and its trailing whitespace.
SourceCode.deleteWithTrailingWhitespace(tree, state);

// Rename a variable (updates all references).
SuggestedFixes.renameVariable(tree, "newName", state);

// Compose multiple fixes.
SuggestedFix.builder().merge(fix1).merge(fix2).build();
```

### `buildDescription(tree)` for custom messages or multiple fixes
<!-- check: skip -->

Use the builder API when you need a custom message or want to offer multiple
fix alternatives:

```java
return buildDescription(tree)
    .setMessage("Custom message: %s".formatted(detail))
    .addFix(primaryFix)
    .addFix(alternativeFix)
    .build();
```

### `@SuppressWarnings` on the checker class
<!-- check: skip -->

When the checker class itself triggers a static analysis warning that cannot be
resolved properly, suppress it on the class. For example, checkers with fields
(beyond `serialVersionUID`) may need:

```java
@SuppressWarnings("java:S2160" /* Super class equality definition suffices. */)
public final class ConstantNaming extends BugChecker implements VariableTreeMatcher {
```

### `ErrorProneFlags` for user-configurable behaviour
<!-- check: skip -->

For checkers that accept user-provided flags, use the two-constructor pattern:

```java
/** Instantiates a default {@link CheckName} instance. */
public CheckName() {
  this(ErrorProneFlags.empty());
}

@Inject
CheckName(ErrorProneFlags flags) {
  this.values = Flags.getSet(flags, "CheckName:FlagName");
}
```

Flag-based tests then use `.setArgs("-XepOpt:CheckName:FlagName=value")`.

### `ThirdPartyLibrary` guard
<!-- check: skip -->

When a fix introduces a dependency on a third-party library, guard it:

```java
if (!ThirdPartyLibrary.GUAVA.isIntroductionAllowed(state)) {
  return Description.NO_MATCH;
}
```

## Test file structure
<!-- check: `// BUG: Diagnostic contains:` is on the line before the flagged code -->
<!-- check: Identification test includes negative (non-flagged) cases listed first -->

Test files are placed in the corresponding module's test `bugpatterns/`
directory as `{CheckName}Test.java`. A complete test class looks like this:

```java
package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class {CheckName}Test {
  @Test
  void identification() {
    CompilationTestHelper.newInstance({CheckName}.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  void negative() {",
            "    // Code that should NOT be flagged.",
            "  }",
            "",
            "  void positive() {",
            "    // BUG: Diagnostic contains:",
            "    flaggedCode();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance({CheckName}.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    flaggedCode();",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    fixedCode();",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
```

Conventions:
- **Standalone class**: `final class {CheckName}Test`; no base class or shared
  infrastructure.
- **`identification()` test**: Uses `CompilationTestHelper`. Include both
  positive and negative cases. Use `// BUG: Diagnostic contains:` on the line
  **before** the flagged code. Optionally include a message fragment: `// BUG:
  Diagnostic contains: exact text`. The substring variant is useful for checks
  with dynamic diagnostic messages and for reducing duplication between
  `identification()` and `replacement()` tests by matching against the
  suggested replacement code. When using this approach, ensure that enough
  replacement cases remain to validate that the suggested fix compiles.
- **`replacement()` test**: Uses `BugCheckerRefactoringTestHelper` with
  `.doTest(TestMode.TEXT_MATCH)`.
- **Prefer single test files**: Prefer a single `identification()` and
  `replacement()` test method per test class. Prefer a single `A.java` test
  file per test. Introduce additional test methods or files only when required
  (e.g., to test different flag configurations or multi-file scenarios). Inline
  source as varargs strings.
### Testing multiple suggested fixes
<!-- check: skip -->

When a check provides multiple fix alternatives, use `.setFixChooser()` to
select which fix to test:

```java
BugCheckerRefactoringTestHelper.newInstance(MyCheck.class, getClass())
    .setFixChooser(SECOND)
    .addInputLines("A.java", ...)
    .addOutputLines("A.java", ...)
    .doTest(TestMode.TEXT_MATCH);
```

### Testing flag-based configuration
<!-- check: skip -->

For `BugChecker`s that accept flags (see the `ErrorProneFlags` section above),
use `.setArgs()` to test behaviour under different flag values:

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

### Diagnostic matching with predicates
<!-- check: skip -->

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

Note the `// BUG: Diagnostic matches: key1` syntax (instead of `// BUG:
Diagnostic contains:`).

### Test ordering and completeness
<!-- check: `identification()` test cases follow checker validation logic -->

The `identification()` test should structurally list both compliant
(not-flagged) and non-compliant (flagged) code examples, ordered to follow the
checker's validation logic, starting with early exits and progressing through
the decision pipeline. List non-violating (negative) cases before violating
(positive) cases. Code without a `// BUG:` comment is implicitly a negative
case. Where multiple examples exercise the same logic path, list them from
simple to complex. A human reviewer should be able to walk through the checker
source and test source in tandem and verify that all cases are covered.

The `identification()` test should cover all edge cases, including negative
cases and all supported code patterns. The `replacement()` test only needs to
verify that the fix transformation is correct and yields valid code in all
relevant cases; it does not need to repeat all edge cases from
`identification()`. Within `replacement()`, follow the same structural
ordering, with one test case per distinct fix pattern.

When tests are structured this way, high mutation test coverage follows
naturally. Regardless, always ensure maximal test coverage by running
`./run-branch-mutation-tests.sh` and adding additional test cases to address
surviving mutants. Such additional test cases must be placed appropriately
based on the specified guidelines.

### Real example: `IsInstanceLambdaUsageTest`
<!-- check: skip -->

```java
final class IsInstanceLambdaUsageTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(IsInstanceLambdaUsage.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  void m() {",
            "    Stream.of(0).map(i -> i);",
            "    Stream.of(1).filter(Integer.class::isInstance);",
            "",
            "    // BUG: Diagnostic contains:",
            "    Stream.of(2).filter(i -> i instanceof Integer);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(IsInstanceLambdaUsage.class, getClass())
        .addInputLines(
            "A.java",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  void m() {",
            "    Stream.of(1).filter(i -> i instanceof Integer);",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  void m() {",
            "    Stream.of(1).filter(Integer.class::isInstance);",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
```

### Real example: `AutowiredConstructorTest`
<!-- check: skip -->

```java
final class AutowiredConstructorTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(AutowiredConstructor.class, getClass())
        .addSourceLines(
            "Container.java",
            "import org.springframework.beans.factory.annotation.Autowired;",
            "",
            "interface Container {",
            "  class A {",
            "    A() {}",
            "  }",
            "",
            "  class B {",
            "    // BUG: Diagnostic contains:",
            "    @Autowired",
            "    B() {}",
            "  }",
            "",
            "  class C {",
            "    @Autowired",
            "    C() {}",
            "",
            "    C(String x) {}",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(AutowiredConstructor.class, getClass())
        .addInputLines(
            "Container.java",
            "import org.springframework.beans.factory.annotation.Autowired;",
            "",
            "interface Container {",
            "  class A {",
            "    @Autowired",
            "    A(String x) {}",
            "  }",
            "}")
        .addOutputLines(
            "Container.java",
            "import org.springframework.beans.factory.annotation.Autowired;",
            "",
            "interface Container {",
            "  class A {",
            "    A(String x) {}",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
```

## Verification
<!-- check: skip -->

To confirm that the checker compiles and produces the expected diagnostics, run:

```sh
mvn test -pl error-prone-contrib -Dtest=MyCheckerTest -Dverification.skip
```

Replace the module (`-pl`) as needed for `error-prone-experimental` or
`error-prone-guidelines`.

## Reference: Matcher interfaces
<!-- check: skip -->

Common `BugChecker.*Matcher` interfaces and their `matchXxx()` methods:

| Interface | Tree type | Method |
|-----------|-----------|--------|
| `AnnotationTreeMatcher` | `AnnotationTree` | `matchAnnotation` |
| `ClassTreeMatcher` | `ClassTree` | `matchClass` |
| `CompilationUnitTreeMatcher` | `CompilationUnitTree` | `matchCompilationUnit` |
| `LambdaExpressionTreeMatcher` | `LambdaExpressionTree` | `matchLambdaExpression` |
| `MemberReferenceTreeMatcher` | `MemberReferenceTree` | `matchMemberReference` |
| `MethodInvocationTreeMatcher` | `MethodInvocationTree` | `matchMethodInvocation` |
| `MethodTreeMatcher` | `MethodTree` | `matchMethod` |
| `VariableTreeMatcher` | `VariableTree` | `matchVariable` |

There are many more matcher interfaces; one for each Java AST node type.

## Reference: `error-prone-utils` utilities
<!-- check: skip -->

Key methods from the `error-prone-utils` module
(`tech.picnic.errorprone.utils`):

| Class | Method | Purpose |
|-------|--------|---------|
| `SourceCode` | `treeToString(tree, state)` | Original source representation of a tree node |
| `SourceCode` | `deleteWithTrailingWhitespace(tree, state)` | `SuggestedFix` that deletes a node and trailing whitespace |
| `SourceCode` | `unwrapMethodInvocation(tree, state)` | `SuggestedFix` replacing `m(arg)` with `arg` |
| `SourceCode` | `toStringConstantExpression(value, state)` | Converts a value to a string constant expression |
| `MoreASTHelpers` | `findMethods(name, state)` | Finds methods by name in the enclosing class |
| `MoreASTHelpers` | `findMethodExitedOnReturn(state)` | Finds the method exited by a `return` statement |
| `MoreASTHelpers` | `areSameType(treeA, treeB, state)` | Checks whether two trees have the same type |
| `MoreMatchers` | `hasMetaAnnotation(annotationType)` | Matches annotations meta-annotated with the given type |
| `MoreMatchers` | `isSubTypeOf(type)` | Matches trees whose type is a subtype of the given type |
| `ThirdPartyLibrary` | `X.isIntroductionAllowed(state)` | Checks whether a third-party library dependency is acceptable |
| `Flags` | `getList(flags, name)` | Parses a comma-separated flag value into a list |
| `Flags` | `getSet(flags, name)` | Parses a comma-separated flag value into a set |
| `ConflictDetection` | `findMethodRenameBlocker(...)` | Validates that a method rename does not introduce conflicts |
| `MoreJUnitMatchers` | `TEST_METHOD` | Matches JUnit test methods |
| `MoreJUnitMatchers` | `SETUP_OR_TEARDOWN_METHOD` | Matches JUnit setup/teardown methods |

## Avoid suggesting breaking changes in fixes
<!-- check: Suggested fixes do not introduce compilation errors -->

Suggested fixes must not introduce compilation errors. If a fix might break
compilation (e.g., renaming a public method, making a field `final`), either
restrict the check to `private` members only, flag the issue without suggesting
a fix, or introduce a flag to control behavior.

[bug-checker]: https://errorprone.info/docs/plugins
[documentation-java]: ../../error-prone-utils/src/main/java/tech/picnic/errorprone/utils/Documentation.java
[error-prone-criteria]: https://errorprone.info/docs/criteria
[java-style]: java-style.instructions.md
[testing]: testing.instructions.md
