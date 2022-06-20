package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class TestNGAnnotationTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(TestNGAnnotation.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(TestNGAnnotation.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import org.testng.annotations.Test;",
            "",
            "@Test",
            "class A {",
            "  @Test",
            "  // BUG: Diagnostic contains:",
            "  public void foo() {",
            "    int number = 10;",
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
            "  @Test",
            "  public void foo() {",
            "    int number = 10;",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import org.testng.annotations.Test;",
            "",
            "class A {",
            "  @org.junit.jupiter.api.Test",
            "  public void foo() {",
            "    int number = 10;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
