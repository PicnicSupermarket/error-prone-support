package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class MonoZipUsageTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(MonoZipUsage.class, getClass())
        .addSourceLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  <T> void m(T t) {",
            "    // BUG: Diagnostic contains:",
            "    Mono.empty().zipWith(Mono.just(1));",
            "",
            "    Mono<Void> emptyMono = Mono.empty();",
            "    Mono<Integer> integerMono = Mono.empty();",
            "",
            "    Mono.zip(Mono.just(1), Mono.just(2));",
            "    Mono.zip(Mono.just(3), Mono.just(4), Mono.just(5));",
            "    Mono.zip(integerMono, integerMono);",
            "",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(Mono.empty(), Mono.empty());",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(emptyMono, Mono.just(1));",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(emptyMono, emptyMono);",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(Mono.just(1).then(), Mono.just(2));",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(Mono.just(1), Mono.just(2), emptyMono);",
            "",
            "    Mono.just(1).zipWith(Mono.just(2));",
            "    Mono.just(1).zipWith(integerMono);",
            "    Mono.just(1).zipWith(integerMono, (a, b) -> a + b);",
            "",
            "    // BUG: Diagnostic contains:",
            "    Mono.just(1).zipWith(Mono.empty());",
            "    // BUG: Diagnostic contains:",
            "    Mono.just(1).zipWith(emptyMono);",
            "",
            "    // BUG: Diagnostic contains:",
            "    Mono.empty().zipWith(Mono.just(1));",
            //            "    e.zipWith(e);",
            //            "    b.zipWith(b).zipWith(c).map(entry -> entry);",
            //            "    c.zipWith(b);",
            //            "    c.zipWith(d);",
            //            "    c.zipWith(c);",
            //            "    // BUG: Diagnostic contains:",
            //            "    c.zipWith(a);",
            //            "    c.zipWith(b, (first, second) -> first + second);",
            //            "    // BUG: Diagnostic contains:",
            //            "    a.zipWith(c, (first, second) -> second);",
            //            "    // BUG: Diagnostic contains:",
            //            "    c.zipWith(Mono.empty());",
            "  }",
            "}")
        .doTest();
  }
}
