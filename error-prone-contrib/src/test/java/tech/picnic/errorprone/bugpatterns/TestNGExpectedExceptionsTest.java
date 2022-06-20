package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class TestNGExpectedExceptionsTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(TestNGExpectedExceptions.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(TestNGExpectedExceptions.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import org.testng.annotations.Test;",
            "",
            "class A {",
            "  // BUG: Diagnostic contains:",
            "  @Test(expectedExceptions = RuntimeException.class)",
            "  public void foo() {",
            "    throw new RuntimeException(\"foo\");",
            "  }",
            "",
            "  @Test(description = \"unit\")",
            "  public void bar() {",
            "    int number = 10;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import org.testng.annotations.Test;",
            "",
            "class A {",
            "  @Test(expectedExceptions = RuntimeException.class)",
            "  public void foo() {",
            "    throw new RuntimeException(\"foo\");",
            "  }",
            "",
            "  @Test(priority = 10, expectedExceptions = RuntimeException.class)",
            "  public void bar() {",
            "    throw new RuntimeException(\"bar\");",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import org.testng.annotations.Test;",
            "",
            "class A {",
            "  @Test",
            "  public void foo() {",
            "    org.junit.jupiter.api.Assertions.assertThrows(",
            "        RuntimeException.class,",
            "        () -> {",
            "          throw new RuntimeException(\"foo\");",
            "        });",
            "  }",
            "",
            "  @Test(priority = 10)",
            "  public void bar() {",
            "    org.junit.jupiter.api.Assertions.assertThrows(",
            "        RuntimeException.class,",
            "        () -> {",
            "          throw new RuntimeException(\"bar\");",
            "        });",
            "  }",
            "}")
        .doTest(TEXT_MATCH);
  }

  @Test
  void arrayReplacement() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import org.testng.annotations.Test;",
            "",
            "class A {",
            "  @Test(expectedExceptions = {RuntimeException.class, ArithmeticException.class})",
            "  public void foo() {",
            "    throw new RuntimeException(\"foo\");",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import org.testng.annotations.Test;",
            "",
            "class A {",
            "  // XXX: Removed handling of `ArithmeticException.class` because this migration doesn't support it.",
            "  @Test",
            "  public void foo() {",
            "    org.junit.jupiter.api.Assertions.assertThrows(",
            "        RuntimeException.class,",
            "        () -> {",
            "          throw new RuntimeException(\"foo\");",
            "        });",
            "  }",
            "}")
        .doTest(TEXT_MATCH);
  }
}
