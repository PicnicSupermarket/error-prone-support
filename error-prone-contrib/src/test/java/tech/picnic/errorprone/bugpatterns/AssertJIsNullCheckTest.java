package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class AssertJIsNullCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(AssertJIsNullCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(AssertJIsNullCheck.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "",
            "class A {",
            "  void m() {",
            "    assertThat(1).isEqualTo(1);",
            "    // BUG: Diagnostic contains:",
            "    assertThat(1).isEqualTo(null);",
            "    // BUG: Diagnostic contains:",
            "    assertThat(\"foo\").isEqualTo(null);",
            "    isEqualTo(null);",
            "  }",
            "",
            "  private boolean isEqualTo(Object value) {",
            "    return value.equals(\"bar\");",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "",
            "class A {",
            "  void m() {",
            "    assertThat(1).isEqualTo(null);",
            "    assertThat(\"foo\").isEqualTo(null);",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "",
            "class A {",
            "  void m() {",
            "    assertThat(1).isNull();",
            "    assertThat(\"foo\").isNull();",
            "  }",
            "}")
        .doTest(TEXT_MATCH);
  }
}
