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
            "    Flux.just().collect(toImmutableList()).single();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collect(toImmutableList()).defaultIfEmpty(ImmutableList.of());",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collect(toImmutableList()).switchIfEmpty(Mono.just(ImmutableList.of()));",
            "",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collect(ImmutableList::of, (list, item) -> {}).single();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collect(ImmutableList::of, (list, item) -> {}).defaultIfEmpty(ImmutableList.of());",
            "    Flux.just()",
            "        .collect(ImmutableList::of, (list, item) -> {})",
            "        // BUG: Diagnostic contains:",
            "        .switchIfEmpty(Mono.just(ImmutableList.of()));",
            "",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectList().single();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectList().defaultIfEmpty(ImmutableList.of());",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectList().switchIfEmpty(Mono.just(ImmutableList.of()));",
            "",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectSortedList().single();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectSortedList().defaultIfEmpty(ImmutableList.of());",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectSortedList().switchIfEmpty(Mono.just(ImmutableList.of()));",
            "",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectSortedList((o1, o2) -> 1).single();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectSortedList((o1, o2) -> 1).defaultIfEmpty(ImmutableList.of());",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectSortedList((o1, o2) -> 1).switchIfEmpty(Mono.just(ImmutableList.of()));",
            "",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMap(identity()).single();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMap(identity()).defaultIfEmpty(ImmutableMap.of());",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMap(identity()).switchIfEmpty(Mono.just(ImmutableMap.of()));",
            "",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMap(identity(), identity()).single();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMap(identity(), identity()).defaultIfEmpty(ImmutableMap.of());",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMap(identity(), identity()).switchIfEmpty(Mono.just(ImmutableMap.of()));",
            "",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMap(identity(), identity(), ImmutableMap::of).single();",
            "    Flux.just()",
            "        .collectMap(identity(), identity(), ImmutableMap::of)",
            "        // BUG: Diagnostic contains:",
            "        .defaultIfEmpty(ImmutableMap.of());",
            "    Flux.just()",
            "        .collectMap(identity(), identity(), ImmutableMap::of)",
            "        // BUG: Diagnostic contains:",
            "        .switchIfEmpty(Mono.just(ImmutableMap.of()));",
            "",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMultimap(identity()).single();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMultimap(identity()).defaultIfEmpty(ImmutableMap.of());",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMultimap(identity()).switchIfEmpty(Mono.just(ImmutableMap.of()));",
            "",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMultimap(identity(), identity()).single();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMultimap(identity(), identity()).defaultIfEmpty(ImmutableMap.of());",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMultimap(identity(), identity()).switchIfEmpty(Mono.just(ImmutableMap.of()));",
            "",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMultimap(identity(), identity(), ImmutableMap::of).single();",
            "    Flux.just()",
            "        .collectMultimap(identity(), identity(), ImmutableMap::of)",
            "        // BUG: Diagnostic contains:",
            "        .defaultIfEmpty(ImmutableMap.of());",
            "    Flux.just()",
            "        .collectMultimap(identity(), identity(), ImmutableMap::of)",
            "        // BUG: Diagnostic contains:",
            "        .switchIfEmpty(Mono.just(ImmutableMap.of()));",
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
