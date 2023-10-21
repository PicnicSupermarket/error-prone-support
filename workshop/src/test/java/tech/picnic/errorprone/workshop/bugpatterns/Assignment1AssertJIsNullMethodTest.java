package tech.picnic.errorprone.workshop.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Enable this when implementing the BugChecker.")
final class Assignment1AssertJIsNullMethodTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(Assignment1AssertJIsNullMethod.class, getClass())
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
    BugCheckerRefactoringTestHelper.newInstance(Assignment1AssertJIsNullMethod.class, getClass())
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
        .doTest(TestMode.TEXT_MATCH);
  }
}
