package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class InstantNowCheckTest {
  private final CompilationTestHelper compilationHelper =
      CompilationTestHelper.newInstance(InstantNowCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(new JUnitMethodDeclarationCheck(), getClass());

  @Test
  public void testIdentification() {
    compilationHelper
        .addSourceLines(
            "A.java",
            "import java.time.Instant;",
            "",
            "class A {",
            "void m() {",
            "    // BUG: Diagnostic contains:",
            "Instant x = Instant.now();",
            "  }",
            "}")
        .addSourceLines(
            "B.java",
            "import java.time.Instant;",
            "",
            "class B {",
            "void m() {",
            "Instant x = Instant.EPOCH;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void testReplacement() {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "import java.time.Instant;",
            "",
            "class A {",
            "void m() {",
            "Instant x = Instant.now();",
            "  }",
            "}")
        .addOutputLines(
            "out/A.java",
            "import java.time.Instant;",
            "",
            "class A {",
            "void m() {",
            "Instant x = Instant.EPOCH;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
