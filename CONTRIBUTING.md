# Contributing

Thank you for checking this document! This project is free software, and we
(the maintainers) encourage and value any contribution.

Here are some guidelines to help you get started.

## 🐛 Reporting a bug

Like any non-trivial piece of software, this library is probably not bug-free.
If you found a bug, feel free to [report the issue][error-prone-support-issues]
on GitHub.

Before doing so, please:
- Verify that the issue is reproducible against the latest version of the
  project.
- Search through the existing set of issues to see whether the problem is
  already known. With some luck a solution is already in place, or a workaround
  may have been provided.

When filing a bug report, please include the following:
- Any relevant information about your environment. This should generally
  include the output of `java --version`, as well as the version of Error Prone
  you're using.
- A description of what is going on (e.g. logging output, stacktraces).
- A minimum reproducible example, so that other developers can try to reproduce
  (and optionally fix) the bug.
- Any additional information that may be relevant.

## 💡 Reporting an improvement

If you would like to see an improvement, you can file a [GitHub
issue][error-prone-support-issues]. This is also a good idea when you're
already working towards opening a pull request, as this allows for discussion
around the idea.

## 🚀 Opening a pull request

All submissions, including submissions by project members, require approval by
at least two reviewers. We use [GitHub pull
requests][error-prone-support-pulls] for this purpose.

Before opening a pull request, please check whether there are any existing
(open or closed) issues or pull requests addressing the same problem. This
avoids double work or lots of time spent on a solution that may ultimately not
be accepted. When in doubt, make sure to first raise an
[issue][error-prone-support-issues] to discuss the idea.

To the extent possible, the pull request process guards our coding guidelines.
Some pointers:
- Try to make sure that the
  [`./run-full-build.sh`][error-prone-support-full-build] script completes
  successfully, ideally before opening a pull request. See the [development
  instructions][error-prone-support-developing] for details on how to
  efficiently resolve many of the errors and warnings that may be reported. (In
  particular, make sure to run `mvn fmt:format` and
  [`./apply-error-prone-suggestions.sh`][error-prone-support-patch].) That
  said, if you feel that the build fails for invalid or debatable reasons, or
  if you're unsure how to best resolve an issue, don't let that discourage you
  from opening a PR with a failing build; we can have a look at the issue
  together!
- Checks should be _topical_: ideally they address a single concern.
- Where possible checks should provide _fixes_, and ideally these are
  completely behavior-preserving. In order for a check to be adopted by users
  it must not "get in the way". So for a check that addresses a relatively
  trivial stylistic concern it is doubly important that the violations it
  detects can be auto-patched.
- Make sure you have read Error Prone's [criteria for new
  checks][error-prone-criteria]. Most guidelines described there apply to this
  project as well, except that this project _does_ focus quite heavy on style
  enforcement. But that just makes the previous point doubly important.
- Make sure that a check's [(mutation) test
  coverage][error-prone-support-mutation-tests] is or remains about as high as
  it can be. Not only does this lead to better tests, it also points out
  opportunities to simplify the code.
- Please restrict the scope of a pull request to a single feature or fix. Don't
  sneak in unrelated changes; instead just open more than one pull request 😉.

## 🔧 Development guide

This section describes the project's architecture, development patterns, and
testing conventions.

### Project module overview

- `error-prone-contrib`: Contains the main bug checkers and Refaster rules.
- `error-prone-experimental`: Contains experimental bug checkers.
- `error-prone-guidelines`: Contains bug checkers that enforce project-specific
  guidelines checks.
- `error-prone-utils`: Contains shared utilities (`MoreASTHelpers`,
  `SourceCode`, `Documentation`, etc.)
- `refaster-compiler`: Contains a custom Refaster rule compiler.
- `refaster-runner`: Contains a Bug Checker that applies Refaster rules found
  on the classpath.
- `refaster-support`: Contains custom annotations and parameter matchers for
  Refaster rules.
- `refaster-test-support`: Contains testing utilities for Refaster rules.
- `documentation-support`: Contains documentation generation helpers.

### `BugChecker` development

Bug checkers live in `error-prone-contrib`, `error-prone-experimental`, and
`error-prone-guidelines`. The general pattern:

1. Extend `BugChecker` and implement a matcher interface (e.g.,
   `MethodInvocationTreeMatcher`).
2. Annotate with `@AutoService(BugChecker.class)` for
   auto-registration.
3. Annotate with `@BugPattern` specifying summary,
   `linkType = CUSTOM`,
   `link = BUG_PATTERNS_BASE_URL + "CheckName"`, severity, and tags.
   `BUG_PATTERNS_BASE_URL` is defined in
   [`Documentation.java`][documentation-java] in the
   `error-prone-utils` module.
4. Declare `private static final long serialVersionUID = 1L;`.
5. Return `Description` with `SuggestedFix` for auto-fixes.
6. Use utilities from `error-prone-utils` (`MoreASTHelpers`,
   `SourceCode`, `MoreMatchers`, etc.).
7. If the checker requires logic already implemented by another checker,
   consider moving said logic to `error-prone-utils`. (In this case, make sure
   to also properly cover it with tests.)

For flag-based configuration, inject `ErrorProneFlags` via an
`@Inject`-annotated constructor and use `Flags.getList()` or
`Flags.getSet()` to parse flag values.

Each package should have a `package-info.java` file annotated with
`@CheckReturnValue` and `@NullMarked`.

### Refaster rule development

Refaster rules live in
`error-prone-contrib/src/main/java/tech/picnic/errorprone/refasterrules/`.
The general pattern:

1. Create a `final class` named `{Topic}Rules` (e.g.,
   `BigDecimalRules`) annotated with `@OnlineDocumentation`, with a
   private constructor.
2. Define static inner classes, each with `@BeforeTemplate` and
   `@AfterTemplate` methods.
3. Use `Refaster.anyOf(...)` to match multiple before-patterns.
4. Apply `@UseImportPolicy(STATIC_IMPORT_ALWAYS)` where appropriate.

Available annotations include `@AlsoNegation`, `@Matches`,
`@NotMatches`, `@Placeholder`, and `@Repeated` (from Error Prone's
Refaster API) as well as `@Description` and `@Severity` (from the
`refaster-support` module). Use matchers from `refaster-support`
(`IsEmpty`, `RequiresComputation`, etc.) to constrain parameter types
where relevant.

### Testing conventions

Tests use JUnit 5 and follow this structure.

**Bug checker tests** use `CompilationTestHelper` for identification
tests and `BugCheckerRefactoringTestHelper` for replacement tests:

```java
@Test
void identification() {
  CompilationTestHelper.newInstance(MyCheck.class, getClass())
      .addSourceLines(
          "A.java", "// BUG: Diagnostic contains:", "...")
      .doTest();
}

@Test
void replacement() {
  BugCheckerRefactoringTestHelper.newInstance(
          MyCheck.class, getClass())
      .addInputLines("A.java", "...")
      .addOutputLines("A.java", "...")
      .doTest(TestMode.TEXT_MATCH);
}
```

Requirements:
- In identification tests, precede lines with expected diagnostics
  with `// BUG: Diagnostic contains:` comments.
- Prefer a single `identification` and `replacement` test per test
  class.
- Prefer a single `A.java` test file per test. Introduce additional
  files only if required.

**Refaster rule tests** use the `refaster-test-support` module
infrastructure:

- Each rule collection has a test class that calls
  `RefasterRuleCollection.validate(clazz)`.
- Test input and output are defined in paired resource files named
  `{Topic}RulesTestInput.java` and `{Topic}RulesTestOutput.java`,
  located in
  `src/test/resources/tech/picnic/errorprone/refasterrules/`.
- The test class inside these files is named `{Topic}RulesTest` and
  implements `RefasterRuleCollectionTestCase`.
- Each inner rule class `FooBar` should have a corresponding test
  method `testFooBar()`.
- New rule collections must be registered in
  [`RefasterRulesTest.java`][refaster-rules-test]'s
  `RULE_COLLECTIONS` set.

### Code style

- Favour `Optional` over `@Nullable` for parameters and return types.
- Use JSpecify `@NullMarked` at package level (in
  `package-info.java`).
- Google Java Format is enforced; run `mvn fmt:format` before
  committing.

### Key utilities

The `error-prone-utils` module provides shared utilities. A few worth
knowing about:

- `Documentation.BUG_PATTERNS_BASE_URL`: the base URL for bug pattern
  documentation links.
- `Flags.getList()` / `Flags.getSet()`: parse Error Prone flag
  values.
- `SourceCode.treeToString()`: prefer original source representation
  over prettified output.
- `ThirdPartyLibrary`: check whether a third-party library is
  available before suggesting its use.
- `ConflictDetection.findMethodRenameBlocker()`: validate that a
  method rename does not introduce conflicts.

[documentation-java]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/error-prone-utils/src/main/java/tech/picnic/errorprone/utils/Documentation.java
[error-prone-criteria]: https://errorprone.info/docs/criteria
[error-prone-support-developing]: https://github.com/PicnicSupermarket/error-prone-support/tree/master#-developing-error-prone-support
[error-prone-support-full-build]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/run-full-build.sh
[error-prone-support-issues]: https://github.com/PicnicSupermarket/error-prone-support/issues
[error-prone-support-mutation-tests]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/run-branch-mutation-tests.sh
[error-prone-support-patch]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/apply-error-prone-suggestions.sh
[error-prone-support-pulls]: https://github.com/PicnicSupermarket/error-prone-support/pulls
[refaster-rules-test]: https://github.com/PicnicSupermarket/error-prone-support/blob/master/error-prone-contrib/src/test/java/tech/picnic/errorprone/refasterrules/RefasterRulesTest.java
