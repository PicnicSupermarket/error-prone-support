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
            "    Flux.just().all(x -> true).defaultIfEmpty(true);",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().any(x -> true).single();",
            "",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collect(toImmutableList()).switchIfEmpty(Mono.just(ImmutableList.of()));",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectList().defaultIfEmpty(ImmutableList.of());",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMap(identity()).single();",
            "    Flux.just()",
            "        .collectMultimap(identity(), identity(), ImmutableMap::of)",
            "        // BUG: Diagnostic contains:",
            "        .switchIfEmpty(Mono.just(ImmutableMap.of()));",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectSortedList((o1, o2) -> 1).defaultIfEmpty(ImmutableList.of());",
            "",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().count().single();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().elementAt(0).defaultIfEmpty(1);",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().hasElement(0).single();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().hasElements().switchIfEmpty(Mono.just(true));",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().last().defaultIfEmpty(1);",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().reduceWith(() -> 1, (x, y) -> x).single();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().single().switchIfEmpty(Mono.just(1));",
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
