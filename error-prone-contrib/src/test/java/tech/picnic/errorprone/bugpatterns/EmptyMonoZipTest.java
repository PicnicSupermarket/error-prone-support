package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class EmptyMonoZipTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(EmptyMonoZip.class, getClass())
        .expectErrorMessage(
            "ARGUMENT",
            m ->
                m.contains(
                    "Don't pass a `Mono<Void>` or `Mono.empty()` argument to `Mono#{zip,With}`"))
        .expectErrorMessage(
            "RECEIVER",
            m ->
                m.contains(
                    "Invoking `Mono#zipWith` on `Mono#empty()` or a `Mono<Void>` is a no-op"))
        .addSourceLines(
            "A.java",
            "import static reactor.core.publisher.Mono.zip;",
            "",
            "import reactor.core.publisher.Flux;",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(1).zip(Mono.empty(), Flux.just(2));",
            "",
            "    Mono<Void> voidMono = Mono.empty();",
            "    Mono<Integer> integerMono = Mono.empty();",
            "",
            "    zip(Mono.just(1), Mono.just(2));",
            "    Mono.zip(Mono.just(1), Mono.just(2));",
            "    Mono.zip(Mono.just(1), Mono.just(2), Mono.just(3));",
            "    Mono.zip(integerMono, integerMono);",
            "",
            "    // BUG: Diagnostic matches: ARGUMENT",
            "    zip(Mono.empty(), Mono.empty());",
            "    // BUG: Diagnostic matches: ARGUMENT",
            "    Mono.zip(Mono.empty(), Mono.empty());",
            "    // BUG: Diagnostic matches: ARGUMENT",
            "    Mono.zip(voidMono, Mono.just(1));",
            "    // BUG: Diagnostic matches: ARGUMENT",
            "    Mono.zip(voidMono, voidMono);",
            "    // BUG: Diagnostic matches: ARGUMENT",
            "    Mono.zip(Mono.just(1).then(), Mono.just(2));",
            "    // BUG: Diagnostic matches: ARGUMENT",
            "    Mono.zip(Mono.just(1), Mono.just(2), voidMono);",
            "",
            "    Mono.just(1).zipWith(Mono.just(2));",
            "    Mono.just(1).zipWith(integerMono);",
            "    Mono.just(1).zipWith(integerMono, (a, b) -> a + b);",
            "",
            "    // BUG: Diagnostic matches: ARGUMENT",
            "    Mono.just(1).zipWith(Mono.empty());",
            "    // BUG: Diagnostic matches: ARGUMENT",
            "    Mono.just(1).zipWith(voidMono);",
            "    // BUG: Diagnostic matches: RECEIVER",
            "    Mono.empty().zipWith(Mono.just(1));",
            "    // BUG: Diagnostic matches: RECEIVER",
            "    voidMono.zipWith(Mono.just(1));",
            "  }",
            "",
            "  abstract class MyMono extends Mono<Object> {",
            "    void m() {",
            "      zip(Mono.just(1), Mono.just(2));",
            "      // BUG: Diagnostic matches: ARGUMENT",
            "      zip(Mono.empty(), Mono.empty());",
            "",
            "      zipWith(Mono.just(1));",
            "      // BUG: Diagnostic matches: ARGUMENT",
            "      zipWith(Mono.empty());",
            "    }",
            "  }",
            "}")
        .doTest();
  }
}
