package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class MonoZipOfMonoVoidUsageTest {
  // Line 33 won't be reported as a bug.
  // It's quite hard to catch this case as Mono.empty yields Mono<Object>, so matcher will be too
  // wide.
  // Additionally, it's not expected to occur in the real production code.
  @Test
  void identification() {
    CompilationTestHelper.newInstance(MonoZipOfMonoVoidUsage.class, getClass())
        .addSourceLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "class A<T> {",
            "  void m(T t) {",
            "    Mono<Void> a = Mono.empty();",
            "    Mono<Integer> b = Mono.empty();",
            "    Mono<Integer> c = Mono.just(1);",
            "    Mono<Integer> d = this.publisher();",
            "    Mono<T> e = Mono.just(t);",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(a, a);",
            "    Mono.zip(e, e);",
            "    e.zipWith(e);",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(d, c, b, a);",
            "    Mono.zip(d, c, b);",
            "    b.zipWith(b).zipWith(c).map(entry -> entry);",
            "    Mono.zip(d, Mono.empty());",
            "    c.zipWith(b);",
            "    c.zipWith(d);",
            "    Mono.just(1).zipWith(Mono.just(1));",
            "    Mono.zip(Mono.just(1), Mono.just(1));",
            "    c.zipWith(c);",
            "  }",
            "",
            "  private Mono<Integer> publisher() {",
            "    return Mono.empty();",
            "  }",
            "}")
        .doTest();
  }
}
