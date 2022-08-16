package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class FluxCollectTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(FluxCollect.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(FluxCollect.class, getClass());

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
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collect(ImmutableList::of, (list, item) -> {}).switchIfEmpty(Mono.just(ImmutableList.of()));",
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
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMap(identity(), identity(), ImmutableMap::of).defaultIfEmpty(ImmutableMap.of());",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMap(identity(), identity(), ImmutableMap::of).switchIfEmpty(Mono.just(ImmutableMap.of()));",
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
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMultimap(identity(), identity(), ImmutableMap::of).defaultIfEmpty(ImmutableMap.of());",
            "    // BUG: Diagnostic contains:",
            "    Flux.just().collectMultimap(identity(), identity(), ImmutableMap::of).switchIfEmpty(Mono.just(ImmutableMap.of()));",
            "  }",
            "}",
            "")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
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
            "    Flux.just().collect(toImmutableList()).single();",
            "    Flux.just().collect(toImmutableList()).defaultIfEmpty(ImmutableList.of());",
            "    Flux.just().collect(toImmutableList()).switchIfEmpty(Mono.just(ImmutableList.of()));",
            "",
            "    Flux.just().collect(ImmutableList::of, (list, item) -> {}).single();",
            "    Flux.just().collect(ImmutableList::of, (list, item) -> {}).defaultIfEmpty(ImmutableList.of());",
            "    Flux.just().collect(ImmutableList::of, (list, item) -> {}).switchIfEmpty(Mono.just(ImmutableList.of()));",
            "",
            "    Flux.just().collectList().single();",
            "    Flux.just().collectList().defaultIfEmpty(ImmutableList.of());",
            "    Flux.just().collectList().switchIfEmpty(Mono.just(ImmutableList.of()));",
            "",
            "    Flux.just().collectSortedList().single();",
            "    Flux.just().collectSortedList().defaultIfEmpty(ImmutableList.of());",
            "    Flux.just().collectSortedList().switchIfEmpty(Mono.just(ImmutableList.of()));",
            "",
            "    Flux.just().collectSortedList((o1, o2) -> 1).single();",
            "    Flux.just().collectSortedList((o1, o2) -> 1).defaultIfEmpty(ImmutableList.of());",
            "    Flux.just().collectSortedList((o1, o2) -> 1).switchIfEmpty(Mono.just(ImmutableList.of()));",
            "",
            "    Flux.just().collectMap(identity()).single();",
            "    Flux.just().collectMap(identity()).defaultIfEmpty(ImmutableMap.of());",
            "    Flux.just().collectMap(identity()).switchIfEmpty(Mono.just(ImmutableMap.of()));",
            "",
            "    Flux.just().collectMap(identity(), identity()).single();",
            "    Flux.just().collectMap(identity(), identity()).defaultIfEmpty(ImmutableMap.of());",
            "    Flux.just().collectMap(identity(), identity()).switchIfEmpty(Mono.just(ImmutableMap.of()));",
            "",
            "    Flux.just().collectMap(identity(), identity(), ImmutableMap::of).single();",
            "    Flux.just().collectMap(identity(), identity(), ImmutableMap::of).defaultIfEmpty(ImmutableMap.of());",
            "    Flux.just().collectMap(identity(), identity(), ImmutableMap::of).switchIfEmpty(Mono.just(ImmutableMap.of()));",
            "",
            "    Flux.just().collectMultimap(identity()).single();",
            "    Flux.just().collectMultimap(identity()).defaultIfEmpty(ImmutableMap.of());",
            "    Flux.just().collectMultimap(identity()).switchIfEmpty(Mono.just(ImmutableMap.of()));",
            "",
            "    Flux.just().collectMultimap(identity(), identity()).single();",
            "    Flux.just().collectMultimap(identity(), identity()).defaultIfEmpty(ImmutableMap.of());",
            "    Flux.just().collectMultimap(identity(), identity()).switchIfEmpty(Mono.just(ImmutableMap.of()));",
            "",
            "    Flux.just().collectMultimap(identity(), identity(), ImmutableMap::of).single();",
            "    Flux.just().collectMultimap(identity(), identity(), ImmutableMap::of).defaultIfEmpty(ImmutableMap.of());",
            "    Flux.just().collectMultimap(identity(), identity(), ImmutableMap::of).switchIfEmpty(Mono.just(ImmutableMap.of()));",
            "  }",
            "}",
            "")
        .addOutputLines(
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
            "    Flux.just().collect(toImmutableList());",
            "    Flux.just().collect(toImmutableList());",
            "    Flux.just().collect(toImmutableList());",
            "",
            "    Flux.just().collect(ImmutableList::of, (list, item) -> {});",
            "    Flux.just().collect(ImmutableList::of, (list, item) -> {});",
            "    Flux.just().collect(ImmutableList::of, (list, item) -> {});",
            "",
            "    Flux.just().collectList();",
            "    Flux.just().collectList();",
            "    Flux.just().collectList();",
            "",
            "    Flux.just().collectSortedList();",
            "    Flux.just().collectSortedList();",
            "    Flux.just().collectSortedList();",
            "",
            "    Flux.just().collectSortedList((o1, o2) -> 1);",
            "    Flux.just().collectSortedList((o1, o2) -> 1);",
            "    Flux.just().collectSortedList((o1, o2) -> 1);",
            "",
            "    Flux.just().collectMap(identity());",
            "    Flux.just().collectMap(identity());",
            "    Flux.just().collectMap(identity());",
            "",
            "    Flux.just().collectMap(identity(), identity());",
            "    Flux.just().collectMap(identity(), identity());",
            "    Flux.just().collectMap(identity(), identity());",
            "",
            "    Flux.just().collectMap(identity(), identity(), ImmutableMap::of);",
            "    Flux.just().collectMap(identity(), identity(), ImmutableMap::of);",
            "    Flux.just().collectMap(identity(), identity(), ImmutableMap::of);",
            "",
            "    Flux.just().collectMultimap(identity());",
            "    Flux.just().collectMultimap(identity());",
            "    Flux.just().collectMultimap(identity());",
            "",
            "    Flux.just().collectMultimap(identity(), identity());",
            "    Flux.just().collectMultimap(identity(), identity());",
            "    Flux.just().collectMultimap(identity(), identity());",
            "",
            "    Flux.just().collectMultimap(identity(), identity(), ImmutableMap::of);",
            "    Flux.just().collectMultimap(identity(), identity(), ImmutableMap::of);",
            "    Flux.just().collectMultimap(identity(), identity(), ImmutableMap::of);",
            "  }",
            "}",
            "")
        .doTest(TEXT_MATCH);
  }
}
