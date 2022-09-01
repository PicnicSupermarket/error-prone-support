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
            "import org.testng.annotations.BeforeMethod;",
            "import org.testng.annotations.Test;",
            "",
            "@Test",
            "class A {",
            "  @BeforeMethod",
            "  // BUG: Diagnostic contains:",
            "  public void init() {}",
            "",
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
            "import org.testng.annotations.BeforeMethod;",
            "import org.testng.annotations.Test;",
            "",
            "class A {",
            "  @BeforeMethod",
            "  public void init() {}",
            "",
            "  @Test",
            "  public void foo() {}",
            "}")
        .addOutputLines(
            "A.java",
            "import org.testng.annotations.BeforeMethod;",
            "import org.testng.annotations.Test;",
            "",
            "class A {",
            "  @org.junit.jupiter.api.BeforeEach",
            "  public void init() {}",
            "",
            "  @org.junit.jupiter.api.Test",
            "  public void foo() {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
