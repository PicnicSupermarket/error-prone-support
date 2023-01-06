package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class NestedOptionalsTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(NestedOptionals.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.Optional;",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  void m() {",
            "    Optional.empty();",
            "    Optional.of(1);",
            "    // BUG: Diagnostic contains:",
            "    Optional.of(Optional.empty());",
            "    // BUG: Diagnostic contains:",
            "    Optional.of(Optional.of(1));",
            "",
            "    Optional.ofNullable(null);",
            "    // BUG: Diagnostic contains:",
            "    Optional.ofNullable((Optional) null);",
            "",
            "    Optional.of(\"foo\").map(String::length);",
            "    // BUG: Diagnostic contains:",
            "    Optional.of(\"foo\").map(Optional::of);",
            "",
            "    Stream.of(\"foo\").findFirst();",
            "    // BUG: Diagnostic contains:",
            "    Stream.of(\"foo\").map(Optional::of).findFirst();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void identificationOptionalTypeNotLoaded() {
    CompilationTestHelper.newInstance(NestedOptionals.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.time.Duration;",
            "",
            "class A {",
            "  void m() {",
            "    Duration.ofSeconds(1);",
            "  }",
            "}")
        .doTest();
  }
}
