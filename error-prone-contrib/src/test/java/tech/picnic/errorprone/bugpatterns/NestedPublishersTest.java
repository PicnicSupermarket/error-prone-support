package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class NestedPublishersTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(NestedPublishers.class, getClass())
        .addSourceLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic contains:",
            "    Mono.just(Mono.empty());",
            "    // BUG: Diagnostic contains:",
            "    Mono.just(Mono.just(1));",
            "    // BUG: Diagnostic contains:",
            "    Mono.just(Flux.empty());",
            "    // BUG: Diagnostic contains:",
            "    Mono.just(Flux.just(1));",
            "    // BUG: Diagnostic contains:",
            "    Mono.just(1).map(Mono::just);",
            "    // BUG: Diagnostic contains:",
            "    Mono.just(1).map(Flux::just);",
            "",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(Flux.empty());",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(Flux.just(1));",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(Mono.empty());",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(Mono.just(1));",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).map(Flux::just);",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).map(Mono::just);",
            "",
            "    Flux.just(1, 2).groupBy(i -> i);",
            "  }",
            "}")
        .doTest();
  }
}
