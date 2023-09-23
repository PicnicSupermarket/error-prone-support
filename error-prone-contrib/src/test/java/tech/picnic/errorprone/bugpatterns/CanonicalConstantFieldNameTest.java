package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class CanonicalConstantFieldNameTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(CanonicalConstantFieldName.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  // BUG: Diagnostic contains: Suggested fix for constant name conflicts with an already defined",
            "  // variable `NUMBER`.",
            "  private static final int number = 1;",
            "",
            "  private static final int NUMBER = 2;",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(CanonicalConstantFieldName.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "  private static final int number = 1;",
            "",
            "  static int getConstantNumber() {",
            "    return number;",
            "  }",
            "",
            "  static int getLocalNumber() {",
            "    int number = 2;",
            "",
            "    return number;",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "  private static final int NUMBER = 1;",
            "",
            "  static int getConstantNumber() {",
            "    return NUMBER;",
            "  }",
            "",
            "  static int getLocalNumber() {",
            "    int number = 2;",
            "",
            "    return number;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void doNotReplaceExcludedOrPublicConstantsByDefault() {
    BugCheckerRefactoringTestHelper.newInstance(CanonicalConstantFieldName.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "  private static final long serialVersionUID = 1L;",
            "",
            "  public static final int number = 1;",
            "",
            "  static final int anotherNumber = 2;",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "  private static final long serialVersionUID = 1L;",
            "",
            "  public static final int number = 1;",
            "",
            "  static final int anotherNumber = 2;",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
