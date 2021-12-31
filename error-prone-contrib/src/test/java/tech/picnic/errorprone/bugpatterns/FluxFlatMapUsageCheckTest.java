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
            "import java.util.function.BiFunction;",
            "import java.util.function.Function;",
            "import reactor.core.publisher.Mono;",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).flatMap(Flux::just);",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).<String>flatMap(i -> Flux.just(String.valueOf(i)));",
            "",
            "    Flux.just(1).concatMap(Flux::just);",
            "    Flux.just(1).flatMap(Flux::just, 1);",
            "    Flux.just(1).flatMap(Flux::just, 1, 1);",
            "    Flux.just(1).flatMap(Flux::just, throwable -> Flux.empty(), Flux::empty);",
            "",
            "    // BUG: Diagnostic contains:",
            "    this.<String, Flux<String>>sink(Flux::flatMap);",
            "    // BUG: Diagnostic contains:",
            "    this.<Integer, Flux<Integer>>sink(Flux::<Integer>flatMap);",
            "",
            "    this.<String, Mono<String>>sink(Mono::flatMap);",
            "  }",
            "",
            "  private <T, P> void sink(BiFunction<P, Function<T, P>, P> fun) {}",
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
            "  void m() {",
            "    Flux.just(1).flatMap(Flux::just);",
            "  }",
            "}")
        .addOutputLines(
            "out/A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(1).concatMap(Flux::just);",
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
            "  private static final int MAX_CONCURRENCY = 8;",
            "",
            "  void m() {",
            "    Flux.just(1).flatMap(Flux::just);",
            "  }",
            "}")
        .addOutputLines(
            "out/A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  private static final int MAX_CONCURRENCY = 8;",
            "",
            "  void m() {",
            "    Flux.just(1).flatMap(Flux::just, MAX_CONCURRENCY);",
            "  }",
            "}")
        .doTest();
  }
}
