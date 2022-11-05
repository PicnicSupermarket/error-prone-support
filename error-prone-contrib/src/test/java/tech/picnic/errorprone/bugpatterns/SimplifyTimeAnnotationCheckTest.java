package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class SimplifyTimeAnnotationCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(SimplifyTimeAnnotationCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(SimplifyTimeAnnotationCheck.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import java.util.concurrent.TimeUnit;",
            "import org.junit.jupiter.api.Timeout;",
            "",
            "interface A {",
            "  @Timeout(6)",
            "  A noSimplification();",
            "  // BUG: Diagnostic contains:",
            "  @Timeout(60)",
            "  A simple();",
            "  // BUG: Diagnostic contains:",
            "  @Timeout(value = 60 * 1000, unit = TimeUnit.MILLISECONDS)",
            "  A explicitUnit();",
            "}")
        .doTest();
  }

  @Test
  void identificationBannedField() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import org.springframework.scheduling.annotation.Scheduled;",
            "",
            "interface A {",
            "  // BUG: Diagnostic contains:",
            "  @Scheduled(fixedDelay = 6_000)",
            "  A scheduledFixedDelay();",
            "",
            "  @Scheduled(fixedDelay = 6_000, fixedRateString = \"\")",
            "  A bannedAttribute();",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "import org.junit.jupiter.api.Timeout;",
            "import org.springframework.scheduling.annotation.Scheduled;",
            "",
            "interface A {",
            "  @Timeout(value = 60)",
            "  A simple();",
            "",
            "  @Scheduled(fixedDelay = 6_000)",
            "  A scheduledFixedDelay();",
            "",
            "  @Scheduled(fixedDelay = 5_000, initialDelay = 6_000, fixedRate = 7_000)",
            "  A scheduledMultiple();",
            "",
            "  @Scheduled(fixedDelay = 60_000, initialDelay = 6_000, fixedRate = 7_000)",
            "  A scheduledCommonUnit();",
            "",
            "  @Scheduled(fixedDelay = 5, initialDelay = 6_000, fixedRate = 7_000)",
            "  A scheduledNoSimplification();",
            "}")
        .addOutputLines(
            "out/A.java",
            "import static java.util.concurrent.TimeUnit.MINUTES;",
            "import static java.util.concurrent.TimeUnit.SECONDS;",
            "",
            "import org.junit.jupiter.api.Timeout;",
            "import org.springframework.scheduling.annotation.Scheduled;",
            "",
            "interface A {",
            "  @Timeout(value = 1, unit = MINUTES)",
            "  A simple();",
            "",
            "  @Scheduled(timeUnit = SECONDS, fixedDelay = 6)",
            "  A scheduledFixedDelay();",
            "",
            "  @Scheduled(timeUnit = SECONDS, fixedDelay = 5, initialDelay = 6, fixedRate = 7)",
            "  A scheduledMultiple();",
            "",
            "  @Scheduled(timeUnit = SECONDS, fixedDelay = 60, initialDelay = 6, fixedRate = 7)",
            "  A scheduledCommonUnit();",
            "",
            "  @Scheduled(fixedDelay = 5, initialDelay = 6_000, fixedRate = 7_000)",
            "  A scheduledNoSimplification();",
            "}")
        .doTest();
  }

  @Test
  void replacementValueOnly() {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "import org.junit.jupiter.api.Timeout;",
            "",
            "interface A {",
            "  @Timeout(60)",
            "  A simple();",
            "}")
        .addOutputLines(
            "out/A.java",
            "import static java.util.concurrent.TimeUnit.MINUTES;",
            "",
            "import org.junit.jupiter.api.Timeout;",
            "",
            "interface A {",
            "  @Timeout(value = 1, unit = MINUTES)",
            "  A simple();",
            "}")
        .doTest();
  }

  @Test
  void replacementFqcn() {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "interface A {",
            "  @org.junit.jupiter.api.Timeout(60)",
            "  A simple();",
            "}")
        .addOutputLines(
            "out/A.java",
            "import static java.util.concurrent.TimeUnit.MINUTES;",
            "",
            "interface A {",
            "  @org.junit.jupiter.api.Timeout(value = 1, unit = MINUTES)",
            "  A simple();",
            "}")
        .doTest();
  }
}
