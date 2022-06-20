package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class TestNGClassLevelTestAnnotationTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(TestNGClassLevelTestAnnotation.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(TestNGClassLevelTestAnnotation.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import org.testng.annotations.Test;",
            "",
            "// BUG: Diagnostic contains:",
            "@Test",
            "class A {",
            "  public void foo() {}",
            "",
            "  @Test(description = \"unit\")",
            "  public void bar() {}",
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
            "@Test",
            "class A {",
            "  public void foo() {}",
            "",
            "  @Test(priority = 12)",
            "  public void bar() {}",
            "",
            "  private void baz() {}",
            "",
            "  protected void qux() {}",
            "",
            "  void quux() {}",
            "}")
        .addOutputLines(
            "A.java",
            "import org.testng.annotations.Test;",
            "",
            "class A {",
            "  @Test",
            "  public void foo() {}",
            "",
            "  @Test(priority = 12)",
            "  public void bar() {}",
            "",
            "  private void baz() {}",
            "",
            "  protected void qux() {}",
            "",
            "  void quux() {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
