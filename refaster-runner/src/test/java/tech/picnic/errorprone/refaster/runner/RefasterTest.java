package tech.picnic.errorprone.refaster.runner;

import static com.google.common.base.Predicates.containsPattern;
import static com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

// XXX: Verify the reported severity level. There does not appear to be a straightforward way to
// distinguish these.
// XXX: Also verify that overriding the severity level works.
final class RefasterTest {
  private final CompilationTestHelper compilationHelper =
      CompilationTestHelper.newInstance(Refaster.class, getClass())
          .matchAllDiagnostics()
          .expectErrorMessage(
              "StringOfSizeZeroTemplate",
              containsPattern(
                  "\\[Refaster Rule\\] FooTemplates\\.StringOfSizeZeroTemplate: Refactoring opportunity\\s+.+\\s+null"))
          .expectErrorMessage(
              "StringOfSizeOneTemplate",
              containsPattern(
                  "\\[Refaster Rule\\] FooTemplates\\.StringOfSizeOneTemplate: "
                      + "A custom description about matching single-char strings\\s+.+\\s+"
                      + "\\(see https://error-prone.picnic.tech/refastertemplates/FooTemplates#StringOfSizeOneTemplate\\)"))
          .expectErrorMessage(
              "StringOfSizeTwoTemplate",
              containsPattern(
                  "\\[Refaster Rule\\] FooTemplates\\.ExtraGrouping\\.StringOfSizeTwoTemplate: "
                      + "A custom subgroup description\\s+.+\\s+"
                      + "\\(see https://example.com/template/FooTemplates#ExtraGrouping.StringOfSizeTwoTemplate\\)"))
          .expectErrorMessage(
              "StringOfSizeThreeTemplate",
              containsPattern(
                  "\\[Refaster Rule\\] FooTemplates\\.ExtraGrouping\\.StringOfSizeThreeTemplate: "
                      + "A custom description about matching three-char strings\\s+.+\\s+"
                      + "\\(see https://example.com/custom\\)"));
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(Refaster.class, getClass());
  private final BugCheckerRefactoringTestHelper restrictedRefactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(Refaster.class, getClass())
          .setArgs(
              "-XepOpt:Refaster:NamePattern=.*\\$(StringOfSizeZeroVerboseTemplate|StringOfSizeTwoTemplate)$");

  @Test
  void identification() {
    compilationHelper
        .addSourceLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic matches: StringOfSizeZeroTemplate",
            "    boolean b1 = \"foo\".toCharArray().length == 0;",
            "",
            "    // BUG: Diagnostic matches: StringOfSizeOneTemplate",
            "    boolean b2 = \"bar\".toCharArray().length == 1;",
            "",
            "    // BUG: Diagnostic matches: StringOfSizeTwoTemplate",
            "    boolean b3 = \"baz\".toCharArray().length == 2;",
            "",
            "    // BUG: Diagnostic matches: StringOfSizeThreeTemplate",
            "    boolean b4 = \"qux\".toCharArray().length == 3;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    boolean b1 = \"foo\".toCharArray().length == 0;",
            "    boolean b2 = \"bar\".toCharArray().length == 1;",
            "    boolean b3 = \"baz\".toCharArray().length == 2;",
            "    boolean b4 = \"qux\".toCharArray().length == 3;",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    boolean b1 = \"foo\".isEmpty();",
            "    boolean b2 = \"bar\".length() == 1;",
            "    boolean b3 = \"baz\".length() == 2;",
            "    boolean b4 = \"qux\".length() == 3;",
            "  }",
            "}")
        .doTest(TEXT_MATCH);
  }

  @Test
  void restrictedReplacement() {
    restrictedRefactoringTestHelper
        .addInputLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    boolean b1 = \"foo\".toCharArray().length == 0;",
            "    boolean b2 = \"bar\".toCharArray().length == 1;",
            "    boolean b3 = \"baz\".toCharArray().length == 2;",
            "    boolean b4 = \"qux\".toCharArray().length == 3;",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    boolean b1 = \"foo\".length() + 1 == 1;",
            "    boolean b2 = \"bar\".toCharArray().length == 1;",
            "    boolean b3 = \"baz\".length() == 2;",
            "    boolean b4 = \"qux\".toCharArray().length == 3;",
            "  }",
            "}")
        .doTest(TEXT_MATCH);
  }
}
