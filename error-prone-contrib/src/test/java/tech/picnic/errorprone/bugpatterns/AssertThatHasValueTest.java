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
            "    assertThat(\"foo\").isEqualTo(\"bar\");",
            "    assertThat(Optional.of(2)).isEqualTo(Optional.of(3));",
            "",
            "    assertThat(Optional.of(4).orElseThrow()).isNotEqualTo(5);",
            "",
            "    assertThat(Optional.of(6).orElseThrow(IllegalStateException::new)).isEqualTo(7);",
            "    assertThat(Optional.of(8).orElseThrow(IllegalStateException::new)).isSameAs(9);",
            "",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Optional.of(10).orElseThrow()).isEqualTo(11);",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Optional.of(\"foo\").orElseThrow()).isEqualTo(\"bar\");",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Optional.<Number>of(12).orElseThrow()).isEqualTo(13);",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Optional.of(14).orElseThrow()).isSameAs(15);",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Optional.of(\"baz\").orElseThrow()).isSameAs(\"qux\");",
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
            "  private final Optional<String> field = Optional.of(\"foo\");",
            "",
            "  void m() {",
            "    assertThat(Optional.of(1).orElseThrow()).isEqualTo(2);",
            "    assertThat(Optional.of(\"bar\").orElseThrow()).isEqualTo(\"baz\");",
            "    assertThat(field.orElseThrow()).isEqualTo(\"qux\");",
            "    assertThat(getOptional().orElseThrow()).isEqualTo(\"quux\");",
            "    assertThat(Optional.of(3).orElseThrow()).isSameAs(4);",
            "    assertThat(Optional.of(\"foobar\").orElseThrow()).isSameAs(\"foobaz\");",
            "  }",
            "",
            "  Optional<String> getOptional() {",
            "    return Optional.of(\"quux\");",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "",
            "import java.util.Optional;",
            "",
            "class A {",
            "  private final Optional<String> field = Optional.of(\"foo\");",
            "",
            "  void m() {",
            "    assertThat(Optional.of(1)).hasValue(2);",
            "    assertThat(Optional.of(\"bar\")).hasValue(\"baz\");",
            "    assertThat(field).hasValue(\"qux\");",
            "    assertThat(getOptional()).hasValue(\"quux\");",
            "    assertThat(Optional.of(3)).containsSame(4);",
            "    assertThat(Optional.of(\"foobar\")).containsSame(\"foobaz\");",
            "  }",
            "",
            "  Optional<String> getOptional() {",
            "    return Optional.of(\"quux\");",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
