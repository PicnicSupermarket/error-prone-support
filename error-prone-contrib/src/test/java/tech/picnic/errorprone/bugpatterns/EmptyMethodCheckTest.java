package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class EmptyMethodCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(EmptyMethodCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(new EmptyMethodCheck(), getClass());

  @Test
  public void testIdentification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "class A {",
            "  Object m1() {",
            "    return null;",
            "  }",
            "",
            "  void m2() {",
            "    System.out.println(42);",
            "  }",
            "",
            "  void m3() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  static void m4() {}",
            "",
            "  interface F {",
            "    void fun();",
            "  }",
            "}")
        .addSourceLines(
            "B.java",
            "final class B implements A.F {",
            "  @Override",
            "  public void fun() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  void m3() {}",
            "}")
        .doTest();
  }

  @Test
  public void testReplacement() {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "final class A {",
            "  void instanceMethod() {}",
            "",
            "  static void staticMethod() {}",
            "}")
        .addOutputLines("out/A.java", "final class A {}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
