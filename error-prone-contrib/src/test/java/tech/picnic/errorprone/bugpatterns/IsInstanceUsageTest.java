package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class IsInstanceUsageTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(IsInstanceUsage.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(IsInstanceUsage.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).filter(i -> i instanceof Integer);",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).filter(i -> Integer.class.isInstance(i));",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).onErrorResume(t -> t instanceof Exception, t -> Flux.empty());",
            "",
            "    Flux.just(1).filter(Integer.class::isInstance);",
            "    Flux.just(1).onErrorResume(Exception.class, t -> Flux.empty());",
            "    Flux.just(1).onErrorResume(Exception.class::isInstance, t -> Flux.empty());",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(1).filter(i -> i instanceof Integer);",
            "    Flux.just(1).filter(i -> Integer.class.isInstance(i));",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(1).filter(Integer.class::isInstance);",
            "    Flux.just(1).filter(Integer.class::isInstance);",
            "  }",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }
}
