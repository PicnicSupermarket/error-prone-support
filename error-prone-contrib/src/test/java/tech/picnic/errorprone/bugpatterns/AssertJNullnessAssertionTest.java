package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class AssertJNullnessAssertionTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(AssertJNullnessAssertion.class, getClass())
        .addSourceLines(
            "A.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "",
            "class A {",
            "  void m() {",
            "    assertThat(1).isEqualTo(1);",
            "    assertThat(\"foo\").isSameAs(\"foo\");",
            "    assertThat(this).isNotEqualTo(this);",
            "    assertThat(1.0).isNotSameAs(1.0);",
            "    // BUG: Diagnostic contains:",
            "    assertThat(1).isEqualTo(null);",
            "    // BUG: Diagnostic contains:",
            "    assertThat(\"foo\").isSameAs(null);",
            "    // BUG: Diagnostic contains:",
            "    assertThat(this).isNotEqualTo(null);",
            "    // BUG: Diagnostic contains:",
            "    assertThat(1.0).isNotSameAs(null);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(AssertJNullnessAssertion.class, getClass())
        .addInputLines(
            "A.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "",
            "class A {",
            "  void m() {",
            "    assertThat(1).isEqualTo(null);",
            "    assertThat(\"foo\").isSameAs(null);",
            "    assertThat(this).isNotEqualTo(null);",
            "    assertThat(1.0).isNotSameAs(null);",
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
            "    assertThat(this).isNotNull();",
            "    assertThat(1.0).isNotNull();",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
