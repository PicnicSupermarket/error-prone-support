package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class NestedPublishersTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(NestedPublishers.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import org.reactivestreams.Publisher;
            import reactor.core.publisher.Flux;
            import reactor.core.publisher.GroupedFlux;
            import reactor.core.publisher.Mono;

            class A {
              void m() {
                Mono.empty();
                Flux.just(1);
                Flux.just(1, 2).groupBy(i -> i).map(groupedFlux -> (GroupedFlux) groupedFlux);

                // BUG: Diagnostic contains:
                Mono.just(Mono.empty());
                // BUG: Diagnostic contains:
                Flux.just(Flux.empty());
                // BUG: Diagnostic contains:
                Mono.just((Flux) Flux.just(1));
                // BUG: Diagnostic contains:
                Flux.just((Publisher) Mono.just(1));
                // BUG: Diagnostic contains:
                Mono.just(1).map(Mono::just);
              }
            }
            """)
        .doTest();
  }
}
