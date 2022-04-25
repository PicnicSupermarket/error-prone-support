package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class EmptyMethodTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(EmptyMethod.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(EmptyMethod.class, getClass());

  @Test
  void identification() {
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
            "",
            "  final class MyTestClass {",
            "    void helperMethod() {}",
            "  }",
            "}")
        .addSourceLines(
            "B.java",
            "import org.aspectj.lang.annotation.Pointcut;",
            "",
            "final class B implements A.F {",
            "  @Override",
            "  public void fun() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  void m3() {}",
            "",
            "  /** Javadoc. */",
            "  // BUG: Diagnostic contains:",
            "  void m4() {}",
            "",
            "  void m5() {",
            "    // Single-line comment.",
            "  }",
            "",
            "  void m6() {",
            "    /* Multi-line comment. */",
            "  }",
            "",
            "  @Pointcut",
            "  void m7() {}",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
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
