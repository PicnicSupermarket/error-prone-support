# Generic coding intructions

- Generate code compatible with Java 17.
- Prefer `Optional` over `@Nullable`.
- Prefer early returns.

# Error Prone check coding guidelines

- Follow the coding style established by existing checks.
- Never use `VisitorState#getSourceForNode`; use `SourceCode#treeToString`
  instead.
- Each check should have a corresponding test class file.
- Generally test classes should have a single `identification` and
  `replacememt` test method.
- The `identification` test method should declare a single
  `CompilationTestHelper.newInstance` statement in which all scenarios are
  covered using a single `addSourceLines` call where possible. The source code
  should include both positive and negative cases and describe a class named
  `A`.
- Similarly, for checks that suggest code changes, the `replacement` test
  method should declare a single `BugCheckerRefactoringTestHelper.newInstance`
  statement, ideally with a single `addInputLines`/`addOutputLines` pair, in
  which all relacement scenarios are exercised. Here too the arguments should
  describe a class named `A`.

# Refaster rule coding guidelines

- Follow the coding style established by existing Refaster rules.
- For detailed conventions and step-by-step instructions, read
  `.github/instructions/refaster-rules.instructions.md`.
