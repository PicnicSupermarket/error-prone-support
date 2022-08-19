package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class NonEmptyMonoTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(NonEmptyMono.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(NonEmptyMono.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static com.google.common.collect.ImmutableList.toImmutableList;",
            "import static java.util.function.Function.identity;",
            "",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableMap;",
            "import reactor.core.publisher.Flux;",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).all(x -> true).defaultIfEmpty(true);",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).any(x -> true).single();",
            "",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).collect(toImmutableList()).switchIfEmpty(Mono.just(ImmutableList.of()));",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).collectList().defaultIfEmpty(ImmutableList.of());",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).collectMap(identity()).single();",
            "    Flux.just(1)",
            "        .collectMultimap(identity(), identity(), ImmutableMap::of)",
            "        // BUG: Diagnostic contains:",
            "        .switchIfEmpty(Mono.just(ImmutableMap.of()));",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).collectSortedList((o1, o2) -> 1).defaultIfEmpty(ImmutableList.of());",
            "",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).count().single();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).elementAt(0).defaultIfEmpty(1);",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).hasElement(0).single();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).hasElements().switchIfEmpty(Mono.just(true));",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).last().defaultIfEmpty(1);",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).reduceWith(() -> 1, (x, y) -> x).single();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).single().switchIfEmpty(Mono.just(1));",
            "",
            "    Flux.just(1).reduce(Integer::sum).single();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).reduce(2, Integer::sum).single();",
            "",
            "    // BUG: Diagnostic contains:",
            "    Mono.just(1).defaultIfEmpty(1).defaultIfEmpty(2);",
            "    // BUG: Diagnostic contains:",
            "    Mono.just(1).hasElement().single();",
            "    // BUG: Diagnostic contains:",
            "    Mono.just(1).single().switchIfEmpty(Mono.just(2));",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import static com.google.common.collect.ImmutableList.toImmutableList;",
            "",
            "import com.google.common.collect.ImmutableList;",
            "import reactor.core.publisher.Flux;",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(1).collect(toImmutableList()).single();",
            "    Flux.just(1).collect(toImmutableList()).defaultIfEmpty(ImmutableList.of());",
            "    Flux.just(1).collect(toImmutableList()).switchIfEmpty(Mono.just(ImmutableList.of()));",
            "",
            "    Mono.just(2).hasElement().single();",
            "    Mono.just(2).hasElement().defaultIfEmpty(Boolean.TRUE);",
            "    Mono.just(2).hasElement().switchIfEmpty(Mono.just(Boolean.TRUE));",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static com.google.common.collect.ImmutableList.toImmutableList;",
            "",
            "import com.google.common.collect.ImmutableList;",
            "import reactor.core.publisher.Flux;",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(1).collect(toImmutableList());",
            "    Flux.just(1).collect(toImmutableList());",
            "    Flux.just(1).collect(toImmutableList());",
            "",
            "    Mono.just(2).hasElement();",
            "    Mono.just(2).hasElement();",
            "    Mono.just(2).hasElement();",
            "  }",
            "}")
        .doTest(TEXT_MATCH);
  }
}
