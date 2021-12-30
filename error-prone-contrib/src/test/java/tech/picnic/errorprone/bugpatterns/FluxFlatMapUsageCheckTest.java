package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugCheckerRefactoringTestHelper.newInstance;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class FluxFlatMapUsageCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(FluxFlatMapUsageCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      newInstance(FluxFlatMapUsageCheck.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void positive() {",
            "      // BUG: Diagnostic contains:",
            "      Flux.just(1).flatMap(Flux::just);",
            "  }",
            "",
            "  void negative() {",
            "    Flux.just(1).concatMap(Flux::just);",
            "    Flux.just(1).flatMap(Flux::just, 1);",
            "    Flux.just(1).flatMap(Flux::just, 1, 1);",
            "    Flux.just(1).flatMap(Flux::just, throwable -> Flux.empty(), Flux::empty);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacementFirstSuggestedFix() {
    refactoringTestHelper
        .setFixChooser(FixChoosers.FIRST)
        .addInputLines(
            "in/A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void positive() {",
            "      Flux.just(1).flatMap(Flux::just);",
            "  }",
            "}")
        .addOutputLines(
            "out/A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void positive() {",
            "      Flux.just(1).concatMap(Flux::just);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacementSecondSuggestedFix() {
    refactoringTestHelper
        .setFixChooser(FixChoosers.SECOND)
        .addInputLines(
            "in/A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  private static final int MAX_CONCURRENCY = 10;",
            "",
            "  void positive() {",
            "      Flux.just(1).flatMap(Flux::just);",
            "  }",
            "}")
        .addOutputLines(
            "out/A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  private static final int MAX_CONCURRENCY = 10;",
            "",
            "  void positive() {",
            "      Flux.just(1).flatMap(Flux::just, MAX_CONCURRENCY);",
            "  }",
            "}")
        .doTest();
  }
}
