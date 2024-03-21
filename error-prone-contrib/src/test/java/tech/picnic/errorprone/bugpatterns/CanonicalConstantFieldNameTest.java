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
            "  // BUG: Diagnostic contains:",
            "class A {",
            "  private static final int foo = 1;",
            "  private static final int foobar = 1;",
            "  private static final int foobarbaz = 1;",
            "",
            "  private final int bar = 2;",
            "  public static final int baz = 3;",
            "  static final int qux = 4;",
            "  private final int quux = 5;",
            "  int quuz = 6;",
            "",
            "  private static final long serialVersionUID = 1L;",
            "",
            "  // BUG: Diagnostic contains: a variable named `NUMBER` is already defined in this scope",
            "  private static final int number = B.NUMBER;",
            "",
            "  class B {",
            "    private static final int NUMBER = 1;",
            "  }",
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
            "  int nonConstantNumber = 2;",
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
            "  int nonConstantNumber = 2;",
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
  void excludeFlaggedConstants() {
    BugCheckerRefactoringTestHelper.newInstance(CanonicalConstantFieldName.class, getClass())
        .setArgs("-XepOpt:CanonicalConstantFieldName:ExcludedConstantFieldNames=foo")
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

  @Test
  void includePublicConstants() {
    BugCheckerRefactoringTestHelper.newInstance(CanonicalConstantFieldName.class, getClass())
        .setArgs("-XepOpt:CanonicalConstantFieldName:IncludePublicConstantFields=true")
        .addInputLines(
            "A.java",
            "class A {",
            "  int nonConstantNumber = 1;",
            "",
            "  public static final int number = 2;",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "  int nonConstantNumber = 1;",
            "",
            "  public static final int NUMBER = 2;",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
