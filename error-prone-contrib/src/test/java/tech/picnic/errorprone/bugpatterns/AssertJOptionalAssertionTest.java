package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class AssertJOptionalAssertionTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(AssertJOptionalAssertion.class, getClass())
        .addSourceLines(
            "A.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "",
            "import java.util.Optional;",
            "import org.assertj.core.api.AbstractIntegerAssert;",
            "",
            "class A {",
            "  private final Optional<String> field = Optional.of(\"foo\");",
            "",
            "  void m() {",
            "    getAssertion().isEqualTo(1);",
            "",
            "    assertThat(Optional.of(2).get()).isEqualTo(3);",
            "    assertThat(\"foo\").isEqualTo(\"bar\");",
            "    assertThat(Optional.of(4)).isEqualTo(Optional.of(5));",
            "",
            "    assertThat(Optional.of(6).orElseThrow()).isNotEqualTo(7);",
            "",
            "    var assertion = assertThat(Optional.of(8).orElseThrow());",
            "    assertion.isEqualTo(9);",
            "",
            "    assertThat(Optional.of(10).orElseThrow(IllegalStateException::new)).isEqualTo(11);",
            "    assertThat(Optional.of(12).orElseThrow(IllegalStateException::new)).isSameAs(13);",
            "",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Optional.of(14).orElseThrow()).isEqualTo(15);",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Optional.of(\"foo\").orElseThrow()).isEqualTo(\"bar\");",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Optional.<Number>of(16).orElseThrow()).isEqualTo(17);",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Optional.of(18).orElseThrow()).isSameAs(19);",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Optional.of(\"baz\").orElseThrow()).isSameAs(\"qux\");",
            "    // BUG: Diagnostic contains:",
            "    assertThat(field.orElseThrow()).isEqualTo(\"quux\");",
            "    // BUG: Diagnostic contains:",
            "    assertThat(getOptional().orElseThrow()).isEqualTo(\"corge\");",
            "  }",
            "",
            "  AbstractIntegerAssert<?> getAssertion() {",
            "    return assertThat(1);",
            "  }",
            "",
            "  Optional<String> getOptional() {",
            "    return Optional.of(\"grault\");",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(AssertJOptionalAssertion.class, getClass())
        .addInputLines(
            "A.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "",
            "import java.util.Optional;",
            "",
            "class A {",
            "  void m() {",
            "    assertThat(Optional.of(1).orElseThrow()).isEqualTo(2);",
            "    assertThat(Optional.of(\"foo\").orElseThrow()).isEqualTo(\"bar\");",
            "    assertThat(Optional.of(3).orElseThrow()).isSameAs(4);",
            "    assertThat(Optional.of(\"baz\").orElseThrow()).isSameAs(\"qux\");",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "",
            "import java.util.Optional;",
            "",
            "class A {",
            "  void m() {",
            "    assertThat(Optional.of(1)).hasValue(2);",
            "    assertThat(Optional.of(\"foo\")).hasValue(\"bar\");",
            "    assertThat(Optional.of(3)).containsSame(4);",
            "    assertThat(Optional.of(\"baz\")).containsSame(\"qux\");",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
