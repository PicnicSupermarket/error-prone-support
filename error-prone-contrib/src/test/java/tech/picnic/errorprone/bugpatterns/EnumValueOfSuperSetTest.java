package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class EnumValueOfSuperSetTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(EnumValueOfSuperSet.class, getClass())
        .addSourceLines(
            "Test.java",
            "class Test {",
            "  A convertUnsafe(B b) {",
            "    // BUG: Diagnostic contains:",
            "    return A.valueOf(b.name());",
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
            "    FOUR",
            "  }",
            "}")
        .doTest();
  }
}
