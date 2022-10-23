package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

final class ScheduledTransactionTraceTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(ScheduledTransactionTrace.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(ScheduledTransactionTrace.class, getClass());

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

  @Test
  void identificationWithoutNewRelicAgentApiOnClasspath() {
    compilationTestHelper
        .withClasspath(Scheduled.class)
        .addSourceLines(
            "A.java",
            "import org.springframework.scheduling.annotation.Scheduled;",
            "",
            "class A {",
            "  @Scheduled(fixedDelay = 1)",
            "  void scheduledButNotTraced() {}",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
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
            "A.java",
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
