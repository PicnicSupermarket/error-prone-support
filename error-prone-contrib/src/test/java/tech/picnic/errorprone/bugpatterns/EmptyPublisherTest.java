package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class EmptyPublisherTest {
  // XXX: Reorder test cases.
  // XXX: Update numbering.
  @Test
  void identification() {
    CompilationTestHelper.newInstance(EmptyPublisher.class, getClass())
        .addSourceLines(
            "A.java",
            "import com.google.common.collect.ImmutableSet;",
            "import org.reactivestreams.Subscriber;",
            "import reactor.core.publisher.Flux;",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  void m() {",
            "    Mono.just(0).subscribe(i -> {});",
            "    Mono.just(1).doOnNext(i -> {});",
            "    Mono.just(2).filter(i -> true);",
            "    Mono.just(3).flatMap(Mono::just);",
            "    Mono.just(4).flatMapMany(Flux::just);",
            "    Mono.just(5).flatMapIterable(ImmutableSet::of);",
            "    Mono.just(6).handle((i, j) -> {});",
            "    Mono.just(7).map(i -> i);",
            "",
            "    // BUG: Diagnostic contains: doOnNext",
            "    Mono.empty().doOnNext(i -> {});",
            "    // BUG: Diagnostic contains: filter",
            "    Mono.empty().filter(i -> true);",
            "    // BUG: Diagnostic contains: flatMap",
            "    Mono.empty().flatMap(Mono::just);",
            "    // BUG: Diagnostic contains: flatMapMany",
            "    Mono.empty().flatMapMany(Flux::just);",
            "    // BUG: Diagnostic contains: flatMapIterable",
            "    Mono.empty().flatMapIterable(ImmutableSet::of);",
            "    // BUG: Diagnostic contains: handle",
            "    Mono.empty().handle((i, j) -> {});",
            "    // BUG: Diagnostic contains: map",
            "    Mono.empty().map(i -> i);",
            "",
            "    // BUG: Diagnostic contains: doOnNext",
            "    Flux.empty().doOnNext(i -> {});",
            "    // BUG: Diagnostic contains: filter",
            "    Flux.empty().filter(i -> true);",
            "    // BUG: Diagnostic contains: concatMap",
            "    Flux.empty().concatMap(Mono::just);",
            "    // BUG: Diagnostic contains: flatMap",
            "    Flux.empty().flatMap(Mono::just);",
            "    // BUG: Diagnostic contains: flatMapSequential",
            "    Flux.empty().flatMapSequential(Flux::just);",
            "    // BUG: Diagnostic contains: flatMapIterable",
            "    Flux.empty().flatMapIterable(ImmutableSet::of);",
            "    // BUG: Diagnostic contains: handle",
            "    Flux.empty().handle((i, j) -> {});",
            "    // BUG: Diagnostic contains: map",
            "    Flux.empty().map(i -> i);",
            "",
            "    // BUG: Diagnostic contains: doOnNext",
            "    Mono.just(8).then().doOnNext(i -> {});",
            "    // BUG: Diagnostic contains: filter",
            "    Mono.just(9).then().filter(i -> true);",
            "    // BUG: Diagnostic contains: flatMap",
            "    Mono.just(10).then().flatMap(Mono::just);",
            "    // BUG: Diagnostic contains: flatMapMany",
            "    Mono.just(11).then().flatMapMany(Flux::just);",
            "    // BUG: Diagnostic contains: flatMapIterable",
            "    Mono.just(12).then().flatMapIterable(ImmutableSet::of);",
            "    // BUG: Diagnostic contains: handle",
            "    Mono.just(13).then().handle((i, j) -> {});",
            "    // BUG: Diagnostic contains: map",
            "    Mono.just(14).then().map(i -> i);",
            "",
            "    Mono.just(15).then().subscribe();",
            "    Mono.just(16).then().subscribe((Subscriber<Object>) null);",
            "    Mono.just(17).then().subscribe(null, t -> {});",
            "",
            "    // BUG: Diagnostic contains:",
            "    Mono.just(17).then().subscribe(i -> {});",
            "    // BUG: Diagnostic contains:",
            "    Mono.just(18).then().subscribe(i -> {}, t -> {});",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(EmptyPublisher.class, getClass())
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  void m() {",
            "    Mono.empty().map(i -> 1);",
            "    Mono.empty().doOnNext(i -> {});",
            "    Mono.empty().doOnNext(i -> {}).onErrorComplete();",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  void m() {",
            "    Mono.empty().map(i -> 1);",
            "    Mono.empty();",
            "    Mono.empty().onErrorComplete();",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
