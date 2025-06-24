package tech.picnic.errorprone.experimental.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
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
            // XXX: Check whitespace in-between members. Enforce different clustering based on the
            // members modifiers, e.g. empty line between static and non-static fields.
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

  @Test
  void classBodyBlock() {
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
            "",
            "    private static final int foo = 1;",
            "  }",
            "",
            "  static final class C {",
            "",
            "    private static final int foo = 1;",
            "  }",
            "",
            "  static final class D {",
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
            "    // comment",
            "",
            "    private static final int foo = 1;",
            "  }",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }

  @Test
  void methodBodyBlock() {
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
            "",
            "    return 1;",
            "  }",
            "",
            "  static int methodC() {",
            "",
            "    return 1;",
            "  }",
            "",
            "  static int methodD() {",
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
            "    // comment",
            "",
            "    return 1;",
            "  }",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }
}
