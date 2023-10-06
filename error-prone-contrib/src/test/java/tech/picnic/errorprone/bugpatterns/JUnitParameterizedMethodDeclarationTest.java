package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class JUnitParameterizedMethodDeclarationTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(JUnitParameterizedMethodDeclaration.class, getClass())
        .addSourceLines(
            "A.java",
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "",
            "class A {",
            "  @Test",
            "  void test() {}",
            "",
            "  @ParameterizedTest",
            "  // BUG: Diagnostic contains:",
            "  void badParameterizedTest() {}",
            "",
            "  @ParameterizedTest",
            "  void goodParameterizedTest(Object someArgument) {}",
            "",
            "  void nonTest() {}",
            "}")
        .doTest();
  }
}
