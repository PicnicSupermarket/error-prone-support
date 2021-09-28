package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class SimplifyTimeBasedAnnotationCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(SimplifyTimeBasedAnnotationCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(
          SimplifyTimeBasedAnnotationCheck.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import java.util.concurrent.TimeUnit;",
            "import org.junit.jupiter.api.Timeout;",
            "",
            "interface A {",
            "  @Timeout(6) A noSimplification();",
            "  // BUG: Diagnostic contains:",
            "  @Timeout(60) A simple();",
            "  // BUG: Diagnostic contains:",
            "  @Timeout(value = 60 * 1000, unit = TimeUnit.MILLISECONDS) A explicitUnit();",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "import org.junit.jupiter.api.Timeout;",
            "",
            "interface A {",
            "  @Timeout(value = 60) A simple();",
            "}")
        .addOutputLines(
            "out/A.java",
            "import static java.util.concurrent.TimeUnit.MINUTES;",
            "",
            "import org.junit.jupiter.api.Timeout;",
            "",
            "interface A {",
            "  @Timeout(value = 1, unit = MINUTES) A simple();",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }
}
