package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class StringIsEmptyCheckTest {
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(StringIsEmptyCheck.class, getClass());

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "class A {",
            "  void replaceEquals() {",
            "    String s = \"\";",
            "    boolean b = s.equals(\"\");",
            "  }",
            "}")
        .addOutputLines(
            "out/A.java",
            "class A {",
                "  void replaceEquals() {",
                "    String s = \"\";",
                "    boolean b = s.isEmpty();",
                "  }",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }
}
