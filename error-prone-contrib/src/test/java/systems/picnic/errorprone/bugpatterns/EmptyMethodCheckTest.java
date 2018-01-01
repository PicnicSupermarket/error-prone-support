package systems.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class EmptyMethodCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(EmptyMethodCheck.class, getClass());

  @Test
  public void testNegative() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "class A {",
            "  Object m() {",
            "    return null;",
            "  }",
            "",
            "  void m2() {",
            "    System.out.println(42);",
            "  }",
            "",
            "  interface F {",
            "    void fun();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void testPositive() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "class A {",
            "  // BUG: Diagnostic contains:",
            "  void m() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  static void m2() {}",
            "}")
        .doTest();
  }
}
