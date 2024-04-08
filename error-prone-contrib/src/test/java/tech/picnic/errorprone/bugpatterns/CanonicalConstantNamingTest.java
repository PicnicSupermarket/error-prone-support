package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class CanonicalConstantNamingTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(CanonicalConstantNaming.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  // BUG: Diagnostic contains:",
            "  private static final int foo = 1;",
            "",
            "  // BUG: Diagnostic contains:",
            "  private static final int bar_BAR = 2;",
            "",
            "  private final int baz= 2;",
            "",
            "  public static final int qux = 3;",
            "",
            "  static final int quux = 4;",
            "",
            "  private final int quuz = 5;",
            "",
            "  int corge = 6;",
            "",
            "  private static final long serialVersionUID = 1L;",
            "",
            "  // BUG: Diagnostic contains: a variable named `NUMBER` is already defined in this scope",
            "  private static final int number = B.NUMBER;",
            "",
            "  class B {",
            "    private static final int NUMBER = 1;",
            "",
            "    // BUG: Diagnostic contains:",
            "    private static final int foo = 2;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(CanonicalConstantNaming.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "  private static final int number = 1;",
            "",
            "  int referenceToNumberFromAnotherClass = B.numberFromAnotherClass;",
            "",
            "  static int getConstantNumber() {",
            "    return number;",
            "  }",
            "",
            "  static int getLocalNumber() {",
            "    int number = 3;",
            "",
            "    return number;",
            "  }",
            "",
            "  class B {",
            "    private static final int number = 4;",
            "",
            "    private static final int numberFromAnotherClass = 5;",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "  private static final int NUMBER = 1;",
            "",
            "  int referenceToNumberFromAnotherClass = B.NUMBER_FROM_ANOTHER_CLASS;",
            "",
            "  static int getConstantNumber() {",
            "    return NUMBER;",
            "  }",
            "",
            "  static int getLocalNumber() {",
            "    int number = 3;",
            "",
            "    return number;",
            "  }",
            "",
            "  class B {",
            "    private static final int NUMBER = 4;",
            "",
            "    private static final int NUMBER_FROM_ANOTHER_CLASS = 5;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void allowedConstantsFlag() {
    BugCheckerRefactoringTestHelper.newInstance(CanonicalConstantNaming.class, getClass())
        .setArgs("-XepOpt:CanonicalConstantNaming:AllowedConstantNames=foo")
        .addInputLines(
            "A.java",
            "class A {",
            "  private static final int number = 1;",
            "",
            "  private static final int foo = 3;",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "  private static final int NUMBER = 1;",
            "",
            "  private static final int foo = 3;",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
