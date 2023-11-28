package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class MonoZipOfMonoVoidUsageTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(MonoZipOfMonoVoidUsage.class, getClass())
        .addSourceLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  void m() {",
            "    Mono<Void> a = Mono.empty();",
            "    Mono<Integer> b = Mono.empty();",
            "    Mono<Integer> c = Mono.just(1);",
            "    Mono<Integer> d = this.publisher();",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(a, a);",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(d, c, b, a);",
            "    Mono.zip(d, c, b);",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(Mono.empty(), a);",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(Mono.empty(), Mono.empty());",
            "    // BUG: Diagnostic contains:",
            "    Mono.empty().zipWith(a);",
            "    // BUG: Diagnostic contains:",
            "    Mono.empty().zipWith(Mono.empty());",
            "    b.zipWith(b).zipWith(c).map(entry -> entry);",
            "    // BUG: Diagnostic contains:",
            "    c.zipWith(Mono.empty());",
            "    c.zipWith(b);",
            "    c.zipWith(d);",
            "    Mono.just(1).zipWith(Mono.just(1));",
            "    Mono.zip(Mono.just(1), Mono.just(1));",
            "    c.zipWith(c);",
            "  }",
            "",
            "  private Mono<Integer> publisher() {return Mono.empty();}",
            "}")
        .doTest();
  }
}
