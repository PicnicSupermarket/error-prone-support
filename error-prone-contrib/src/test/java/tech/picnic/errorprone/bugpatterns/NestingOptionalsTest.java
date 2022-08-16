package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class NestingOptionalsTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(NestingOptionals.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import java.util.Optional;",
            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic contains:",
            "    Optional.of(Optional.empty());",
            "    // BUG: Diagnostic contains:",
            "    Optional.of(1).map(Optional::of);",
            "    // BUG: Diagnostic contains:",
            "    Optional.of(2).map(Optional::of).orElseThrow();",
            "    // BUG: Diagnostic contains:",
            "    Optional.of(3).map(value -> Optional.empty());",
            "    // BUG: Diagnostic contains:",
            "    Optional.of(4).map(value -> Optional.empty()).orElseThrow();",
            "",
            "    Optional.of(5).map(String::valueOf);",
            "    Optional.of(6).map(value -> value);",
            "    Optional.of(7).flatMap(Optional::of);",
            "    Optional.of(8).flatMap(value -> Optional.empty());",
            "  }",
            "}")
        .doTest();
  }
}
