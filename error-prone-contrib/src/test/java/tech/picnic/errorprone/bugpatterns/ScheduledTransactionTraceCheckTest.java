package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

public final class ScheduledTransactionTraceCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(ScheduledTransactionTraceCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(ScheduledTransactionTraceCheck.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import com.newrelic.api.agent.Trace;",
            "import org.springframework.scheduling.annotation.Scheduled;",
            "",
            "class A {",
            "  void notScheduled() {}",
            "",
            "  @Scheduled(fixedDelay = 1)",
            "  // BUG: Diagnostic contains:",
            "  void scheduledButNotTraced() {}",
            "",
            "  @Scheduled(fixedDelay = 1)",
            "  // BUG: Diagnostic contains:",
            "  @Trace",
            "  void scheduledButImproperlyTraced1() {}",
            "",
            "  @Scheduled(fixedDelay = 1)",
            "  // BUG: Diagnostic contains:",
            "  @Trace(dispatcher = false)",
            "  void scheduledButImproperlyTraced2() {}",
            "",
            "  @Scheduled(fixedDelay = 1)",
            "  @Trace(dispatcher = true)",
            "  void scheduledAndProperlyTraced() {}",
            "}")
        .doTest();
  }

  // XXX: Enable this test for all JREs once https://github.com/google/error-prone/pull/2820 is
  // merged and released.
  @Test
  @DisabledForJreRange(min = JRE.JAVA_12)
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "import com.newrelic.api.agent.Trace;",
            "import org.springframework.scheduling.annotation.Scheduled;",
            "",
            "class A {",
            "  @Scheduled(fixedDelay = 1)",
            "  void scheduledButNotTraced() {}",
            "",
            "  @Scheduled(fixedDelay = 1)",
            "  @Trace",
            "  void scheduledButImproperlyTraced1() {}",
            "",
            "  @Scheduled(fixedDelay = 1)",
            "  @Trace(dispatcher = false)",
            "  void scheduledButImproperlyTraced2() {}",
            "",
            "  @Scheduled(fixedDelay = 1)",
            "  @Trace(leaf = true)",
            "  void scheduledButImproperlyTraced3() {}",
            "}")
        .addOutputLines(
            "out/A.java",
            "import com.newrelic.api.agent.Trace;",
            "import org.springframework.scheduling.annotation.Scheduled;",
            "",
            "class A {",
            "  @Trace(dispatcher = true)",
            "  @Scheduled(fixedDelay = 1)",
            "  void scheduledButNotTraced() {}",
            "",
            "  @Scheduled(fixedDelay = 1)",
            "  @Trace(dispatcher = true)",
            "  void scheduledButImproperlyTraced1() {}",
            "",
            "  @Scheduled(fixedDelay = 1)",
            "  @Trace(dispatcher = true)",
            "  void scheduledButImproperlyTraced2() {}",
            "",
            "  @Scheduled(fixedDelay = 1)",
            "  @Trace(dispatcher = true, leaf = true)",
            "  void scheduledButImproperlyTraced3() {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
