package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class EnumValueOfUsageTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(EnumValueOfUsage.class, getClass())
        .addSourceLines(
            "Test.java",
            "class Test {",
            "  void convertUnsafe(String raw, B b) {",
            "    // BUG: Diagnostic contains:",
            "    A.valueOf(\"FOUR\");",
            "    // BUG: Diagnostic contains:",
            "    A.valueOf(A.class, \"FOUR\");",
            "    // BUG: Diagnostic contains:",
            "    A.valueOf(raw);",
            "    // BUG: Diagnostic contains:",
            "    A.valueOf(A.class, raw);",
            "    // BUG: Diagnostic contains:",
            "    A.valueOf(b.name());",
            "    // BUG: Diagnostic contains:",
            "    A.valueOf(A.class, b.name());",
            "    var a =",
            "        switch (b) {",
            "          // BUG: Diagnostic contains:",
            "          case FOUR -> A.valueOf(b.name());",
            "          // BUG: Diagnostic contains:",
            "          case FIVE -> A.valueOf(A.class, b.name());",
            "          default -> null;",
            "        };",
            "  }",
            "",
            "  void convertSafe(String raw, B b) {",
            "    A.valueOf(\"ONE\");",
            "    A.valueOf(A.class, \"TWO\");",
            "    var a =",
            "        switch (b) {",
            "          case ONE, THREE -> A.valueOf(A.class, b.name());",
            "          case TWO -> A.valueOf(b.name());",
            "          default -> null;",
            "        };",
            "  }",
            "",
            "  enum A {",
            "    ONE,",
            "    TWO,",
            "    THREE",
            "  }",
            "",
            "  enum B {",
            "    ONE,",
            "    TWO,",
            "    THREE,",
            "    FOUR,",
            "    FIVE",
            "  }",
            "}")
        .doTest();
  }
}
