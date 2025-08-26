package tech.picnic.errorprone.experimental.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class BlockStartWhitespaceTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(BlockStartWhitespace.class, getClass())
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains:",
            "class A {",
            "",
            "  private static final int foo = 1;",
            "",
            "  // BUG: Diagnostic contains:",
            "  void method(String foo) {",
            "",
            "    System.out.println(foo);",
            "  }",
            "}")
        .addSourceLines(
            "B.java",
            "class B {",
            "  private static final int foo = 1;",
            "",
            "  void method(String foo) {",
            "    System.out.println(foo);",
            "  }",
            "}")
        .doTest();
  }

  // This test intentionally uses tabs for indentation, and so disables the format checker.
  @SuppressWarnings("ErrorProneTestHelperSourceFormat")
  @Test
  void replacementClassBodyBlock() {
    BugCheckerRefactoringTestHelper.newInstance(BlockStartWhitespace.class, getClass())
        .addInputLines(
            "Nested.java",
            "class Nested {",
            "  static final class A {",
            "",
            "    private static final int foo = 1;",
            "  }",
            "",
            "  static final class B {",
            "    ",
            "    private static final int foo = 1;",
            "  }",
            "",
            "  static final class C {",
            "",
            "",
            "    private static final int foo = 1;",
            "  }",
            "",
            "  static final class D {",
            "\t\t",
            "\t\tprivate static final int foo = 1;",
            "  }",
            "",
            "  static final class E {",
            "",
            "    // comment",
            "",
            "    private static final int foo = 1;",
            "  }",
            "}")
        .addOutputLines(
            "Nested.java",
            "class Nested {",
            "  static final class A {",
            "    private static final int foo = 1;",
            "  }",
            "",
            "  static final class B {",
            "    private static final int foo = 1;",
            "  }",
            "",
            "  static final class C {",
            "    private static final int foo = 1;",
            "  }",
            "",
            "  static final class D {",
            "    private static final int foo = 1;",
            "  }",
            "",
            "  static final class E {",
            "    // comment",
            "",
            "    private static final int foo = 1;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  // This test intentionally uses tabs for indentation, and so disables the format checker.
  @SuppressWarnings("ErrorProneTestHelperSourceFormat")
  @Test
  void replacementMethodBodyBlock() {
    BugCheckerRefactoringTestHelper.newInstance(BlockStartWhitespace.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "  static int methodA() {",
            "",
            "    return 1;",
            "  }",
            "",
            "  static int methodB() {",
            "    ",
            "    return 1;",
            "  }",
            "",
            "  static int methodC() {",
            "",
            "",
            "    return 1;",
            "  }",
            "",
            "  static int methodD() {",
            "\t\t",
            "\t\treturn 1;",
            "  }",
            "",
            "  static int methodE() {",
            "",
            "    // comment",
            "",
            "    return 1;",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "  static int methodA() {",
            "    return 1;",
            "  }",
            "",
            "  static int methodB() {",
            "    return 1;",
            "  }",
            "",
            "  static int methodC() {",
            "    return 1;",
            "  }",
            "",
            "  static int methodD() {",
            "    return 1;",
            "  }",
            "",
            "  static int methodE() {",
            "    // comment",
            "",
            "    return 1;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void handlesNestedTypeFixes() {
    BugCheckerRefactoringTestHelper.newInstance(BlockStartWhitespace.class, getClass())
        .addInputLines(
            "Nested.java",
            "class Nested {",
            "",
            "  static final class A {",
            "",
            "    private static final int foo = 1;",
            "  }",
            "}")
        .addOutputLines(
            "Nested.java",
            "class Nested {",
            "  static final class A {",
            "    private static final int foo = 1;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  // Disable the formatter to prevent collapsing the test's input and output lines.
  @SuppressWarnings("ErrorProneTestHelperSourceFormat")
  @Test
  void handlesCommentsOddCommentsContainingLBraces() {
    BugCheckerRefactoringTestHelper.newInstance(BlockStartWhitespace.class, getClass())
        .addInputLines(
            "A.java",
            "class A /* { */",
            "",
            "{",
            "",
            "  private static final int foo = 1;",
            "}")
        .addOutputLines(
            "A.java",
            "class A /* { */ {",
            "",
            "  private static final int foo = 1;",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
