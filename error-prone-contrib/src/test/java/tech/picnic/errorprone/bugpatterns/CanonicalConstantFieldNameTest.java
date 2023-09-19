package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import org.junit.jupiter.api.Test;

final class CanonicalConstantFieldNameTest {
  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(CanonicalConstantFieldName.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "  private static final int number = 1;",
            "",
            "  static int getNumber() {",
            "    return number;",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "  private static final int NUMBER = 1;",
            "",
            "  static int getNumber() {",
            "    return NUMBER;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
