package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class TestMethodNameCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(TestMethodNameCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(new TestMethodNameCheck(), getClass());

  @Test
  public void testIdentification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import org.junit.jupiter.api.Test;",
            "",
            "final class A {",
            "  @Test void method1() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Test void testMethod2() {}",
            "}")
        .addSourceLines(
            "B.java",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "",
            "final class B {",
            "  @ParameterizedTest void method3() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @ParameterizedTest void testMethod4() {}",
            "",
            "}")
        .doTest();
  }

  @Test
  public void testReplacement() {
    refactoringTestHelper
        .addInputLines(
            "in/Container.java",
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "",
            "interface Container {",
            "  class A {",
            "    @Test void test() {}",
            "    @Test void testMethod1() {}",
            "    @Test void test2() {}",
            "    @Test void method3() {}",
            "    void method4() {}",
            "  }",
            "",
            "  class B {",
            "    @ParameterizedTest void test() {}",
            "    @ParameterizedTest void testMethod1() {}",
            "    @ParameterizedTest void test2() {}",
            "    @ParameterizedTest void method3() {}",
            "    void method4() {}",
            "  }",
            "}")
        .addOutputLines(
            "out/Container.java",
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "",
            "interface Container {",
            "  class A {",
            "    @Test void test() {}",
            "    @Test void method1() {}",
            "    @Test void test2() {}",
            "    @Test void method3() {}",
            "    void method4() {}",
            "  }",
            "",
            "  class B {",
            "    @ParameterizedTest void test() {}",
            "    @ParameterizedTest void method1() {}",
            "    @ParameterizedTest void test2() {}",
            "    @ParameterizedTest void method3() {}",
            "    void method4() {}",
            "  }",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }
}
