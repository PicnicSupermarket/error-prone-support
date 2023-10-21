package tech.picnic.errorprone.workshop.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Enable this when implementing the BugChecker.")
final class Assignment0Assignment0DeleteEmptyMethodTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(Assignment0DeleteEmptyMethod.class, getClass())
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
            "  // BUG: Diagnostic contains:",
            "  static void m3() {}",
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
            "  /** Javadoc. */",
            "  // BUG: Diagnostic contains:",
            "  void m4() {}",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(Assignment0DeleteEmptyMethod.class, getClass())
        .addInputLines(
            "A.java",
            "final class A {",
            "",
            "  void instanceMethod() {}",
            "",
            "  static void staticMethod() {}",
            "",
            "  static void staticMethodWithComment() {",
            "    System.out.println(42);",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "final class A {",
            "",
            "  static void staticMethodWithComment() {",
            "    System.out.println(42);",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
