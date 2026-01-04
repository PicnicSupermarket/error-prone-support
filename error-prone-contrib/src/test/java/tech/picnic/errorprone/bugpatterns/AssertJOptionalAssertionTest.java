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
            "import java.util.stream.Stream;",
            "import org.assertj.core.api.AbstractAssert;",
            "",
            "class A {",
            "  void m() {",
            "    assertThat(\"foo\").isEqualTo(\"bar\");",
            "    assertThat(\"baz\").isSameAs(\"qux\");",
            "",
            "    assertThat(Optional.of(1)).isEqualTo(Optional.of(2));",
            "    assertThat(Optional.of(3)).isSameAs(Optional.of(4));",
            "",
            "    assertThat(1).isEqualTo(Optional.of(2).orElseThrow()).isEqualTo(3);",
            "    assertThat(4).isSameAs(Optional.of(5).orElseThrow()).isSameAs(6);",
            "",
            "    nullaryAssertThat().isEqualTo(1);",
            "    nullaryAssertThat().isSameAs(2);",
            "",
            "    AbstractAssert<?, ?> assertion = assertThat(Optional.of(1).orElseThrow());",
            "    assertion.isEqualTo(2);",
            "    assertion.isSameAs(3);",
            "",
            "    assertThat(Optional.of(1).get()).isNotEqualTo(2);",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Optional.of(\"foo\").get()).isEqualTo(\"bar\");",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Stream.empty().findAny().get()).isSameAs(new Object());",
            "",
            "    assertThat(Optional.<Number>of(1).orElseThrow()).isNotEqualTo(2);",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Optional.<Object>of(\"foo\").orElseThrow()).isEqualTo(\"bar\");",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Stream.<String>empty().findFirst().orElseThrow()).isSameAs(toString());",
            "",
            "    assertThat(Optional.of(1).orElseThrow(IllegalArgumentException::new)).isNotEqualTo(2);",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Optional.of(\"foo\").orElseThrow(IllegalStateException::new)).isEqualTo(\"bar\");",
            "    // BUG: Diagnostic contains:",
            "    assertThat(Stream.empty().findAny().orElseThrow(RuntimeException::new)).isSameAs(new Object());",
            "  }",
            "",
            "  static <T> AbstractAssert<?, ?> nullaryAssertThat() {",
            "    return null;",
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
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  void m() {",
            "    assertThat(Optional.of(1).get()).isEqualTo(2);",
            "    assertThat(Optional.of(\"foo\").orElseThrow()).isSameAs(\"bar\");",
            "    assertThat(Stream.empty().findAny().orElseThrow(IllegalArgumentException::new))",
            "        .isEqualTo(new Object());",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.assertj.core.api.Assertions.assertThat;",
            "",
            "import java.util.Optional;",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  void m() {",
            "    assertThat(Optional.of(1)).hasValue(2);",
            "    assertThat(Optional.of(\"foo\")).containsSame(\"bar\");",
            "    assertThat(Stream.empty().findAny()).hasValue(new Object());",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
