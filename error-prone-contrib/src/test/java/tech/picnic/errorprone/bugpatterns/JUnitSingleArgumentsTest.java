package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class JUnitSingleArgumentsTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(JUnitSingleArguments.class, getClass())
        .addSourceLines(
            "A.java",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic contains:",
            "    arguments();",
            "    // BUG: Diagnostic contains:",
            "    arguments(1);",
            "    arguments(1,2);",
            "  }",
            "}")
        .doTest();
  }
}
