package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class MonoOfPublishersTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(MonoOfPublishers.class, getClass())
        .addSourceLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  void m() {",
            "    Mono.empty();",
            "    Mono.just(1);",
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
            "    Mono.justOrEmpty(null);",
            "    // BUG: Diagnostic contains:",
            "    Mono.justOrEmpty(Mono.just(1));",
            "    // BUG: Diagnostic contains:",
            "    Mono.justOrEmpty(Flux.just(1));",
            "    // BUG: Diagnostic contains:",
            "    Mono.justOrEmpty(1).map(Mono::just);",
            "    // BUG: Diagnostic contains:",
            "    Mono.justOrEmpty(1).map(Flux::just);",
            "  }",
            "}")
        .doTest();
  }
}
