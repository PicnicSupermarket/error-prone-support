package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class AssertThatHasValueTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(AssertThatHasValue.class, getClass())
        .addSourceLines(
            "A.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "",
            "import java.util.Optional;",
            "",
            "class A {",
            "  void m() {",
            "    assertThat(Optional.of(1).get()).isEqualTo(1);",
            "    assertThat(\"test\").isEqualTo(\"test\");",
            "    assertThat(Optional.of(1)).isEqualTo(Optional.of(1));",
            "",
            "    assertThat(Optional.of(1).orElseThrow()).isNotEqualTo(2);",
            "    assertThat(Optional.of(1).orElseThrow()).isSameAs(1);",
            "",
            "    assertThat(Optional.of(1).orElseThrow(IllegalStateException::new)).isEqualTo(1);",
            "",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Optional.of(1).orElseThrow()).isEqualTo(1);",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Optional.of(\"test\").orElseThrow()).isEqualTo(\"test\");",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Optional.<Number>of(1).orElseThrow()).isEqualTo(1);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(AssertThatHasValue.class, getClass())
        .addInputLines(
            "A.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "",
            "import java.util.Optional;",
            "",
            "class A {",
            "  private final Optional<String> field = Optional.of(\"value\");",
            "",
            "  void m() {",
            "    assertThat(Optional.of(1).orElseThrow()).isEqualTo(1);",
            "    assertThat(Optional.of(\"test\").orElseThrow()).isEqualTo(\"test\");",
            "    assertThat(field.orElseThrow()).isEqualTo(\"value\");",
            "    assertThat(getOptional().orElseThrow()).isEqualTo(\"result\");",
            "  }",
            "",
            "  Optional<String> getOptional() {",
            "    return Optional.of(\"result\");",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "",
            "import java.util.Optional;",
            "",
            "class A {",
            "  private final Optional<String> field = Optional.of(\"value\");",
            "",
            "  void m() {",
            "    assertThat(Optional.of(1)).hasValue(1);",
            "    assertThat(Optional.of(\"test\")).hasValue(\"test\");",
            "    assertThat(field).hasValue(\"value\");",
            "    assertThat(getOptional()).hasValue(\"result\");",
            "  }",
            "",
            "  Optional<String> getOptional() {",
            "    return Optional.of(\"result\");",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
