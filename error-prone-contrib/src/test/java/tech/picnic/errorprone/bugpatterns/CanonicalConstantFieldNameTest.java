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

  @Test
  void excludeFlaggedConstants() {
    BugCheckerRefactoringTestHelper.newInstance(CanonicalConstantFieldName.class, getClass())
        .setArgs("-XepOpt:CanonicalConstantFieldName:ExcludedConstantFliedNames=excludedField")
        .addInputLines(
            "A.java",
            "class A {",
            "  private static final int number = 1;",
            "",
            "  private static final int excludedField = 3;",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "  private static final int NUMBER = 1;",
            "",
            "  private static final int excludedField = 3;",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void IncludePublicConstants() {
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
