package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class AnnotationAttributeReplacementTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(AnnotationAttributeReplacement.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(AnnotationAttributeReplacement.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import org.testng.annotations.Test;",
            "",
            "class A {",
            "  @Test(priority = 10)",
            "  // BUG: Diagnostic contains:",
            "  public void foo() {}",
            "",
            "  @Test(description = \"unit\")",
            "  // BUG: Diagnostic contains:",
            "  public void bar() {}",
            "",
            "  @Test(dataProvider = \"unit\")",
            "  public void baz() {}",
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
            "  @Test(priority = 1, groups = \"unit\", description = \"test\")",
            "  public void foo() {}",
            "}")
        .addOutputLines(
            "A.java",
            "import org.junit.jupiter.api.MethodOrderer;",
                "import org.junit.jupiter.api.TestMethodOrder;",
            "import org.testng.annotations.Test;",
            "",
            "@TestMethodOrder(MethodOrderer.OrderAnnotation.class)",
            "class A {",
            "  @Test",
            "  @org.junit.jupiter.api.Order(1)",
            "  @org.junit.jupiter.api.DisplayName(\"test\")",
            "  @org.junit.jupiter.api.Tag(\"unit\")",
            "  public void foo() {}",
            "}")
        .doTest(TEXT_MATCH);
  }

  @Test
  void replacementUpdateArgumentListAfterSingleArgument() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import org.testng.annotations.Test;",
            "",
            "class A {",
            "  @Test(priority = 2, invocationTimeOut = 10L)",
            "  public void foo() {}",
            "}")
        .addOutputLines(
            "A.java",
            "import org.junit.jupiter.api.MethodOrderer;",
            "import org.junit.jupiter.api.TestMethodOrder;",
            "import org.testng.annotations.Test;",
            "",
            "@TestMethodOrder(MethodOrderer.OrderAnnotation.class)",
            "class A {",
            "  @Test(invocationTimeOut = 10L)",
            "  @org.junit.jupiter.api.Order(2)",
            "  public void foo() {}",
            "}")
        .doTest(TEXT_MATCH);
  }

  @Test
  void replacementSingleThreaded() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import org.testng.annotations.Test;",
            "",
            "class A {",
            "  @Test(singleThreaded = true)",
            "  public void foo() {}",
            "}")
        .addOutputLines(
            "A.java",
            "import org.testng.annotations.Test;",
            "",
            "class A {",
            "  // XXX: Removed argument `singleThreaded = true`, as this cannot be migrated to JUnit!",
            "  @Test",
            "  public void foo() {}",
            "}")
        .doTest(TEXT_MATCH);
  }
}
