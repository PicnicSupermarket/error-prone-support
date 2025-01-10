package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class NonEmptyMonoTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(NonEmptyMono.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import static com.google.common.collect.ImmutableList.toImmutableList;
            import static java.util.function.Function.identity;

            import com.google.common.collect.ImmutableList;
            import com.google.common.collect.ImmutableMap;
            import java.util.ArrayList;
            import java.util.HashMap;
            import java.util.List;
            import reactor.core.publisher.Flux;
            import reactor.core.publisher.Mono;

            class A {
              void m() {
                Mono.just(1).defaultIfEmpty(2);
                Mono.just(1).single();
                Mono.just(1).switchIfEmpty(Mono.just(2));

                // BUG: Diagnostic contains:
                Flux.just(1).all(x -> true).defaultIfEmpty(true);

                // BUG: Diagnostic contains:
                Flux.just(1).any(x -> true).single();

                // BUG: Diagnostic contains:
                Flux.just(1).collect(toImmutableList()).switchIfEmpty(Mono.just(ImmutableList.of()));
                // BUG: Diagnostic contains:
                Flux.just(1).collect(ArrayList::new, List::add).defaultIfEmpty(new ArrayList<>());

                // BUG: Diagnostic contains:
                Flux.just(1).collectList().single();

                // BUG: Diagnostic contains:
                Flux.just(1).collectMap(identity()).switchIfEmpty(Mono.just(ImmutableMap.of()));
                // BUG: Diagnostic contains:
                Flux.just(1).collectMap(identity(), identity()).defaultIfEmpty(ImmutableMap.of());
                // BUG: Diagnostic contains:
                Flux.just(1).collectMap(identity(), identity(), HashMap::new).single();

                // BUG: Diagnostic contains:
                Flux.just(1).collectMultimap(identity()).switchIfEmpty(Mono.just(ImmutableMap.of()));
                // BUG: Diagnostic contains:
                Flux.just(1).collectMultimap(identity(), identity()).defaultIfEmpty(ImmutableMap.of());
                // BUG: Diagnostic contains:
                Flux.just(1).collectMultimap(identity(), identity(), HashMap::new).single();

                // BUG: Diagnostic contains:
                Flux.just(1).collectSortedList().defaultIfEmpty(ImmutableList.of());
                // BUG: Diagnostic contains:
                Flux.just(1).collectSortedList((o1, o2) -> 0).single();

                // BUG: Diagnostic contains:
                Flux.just(1).count().switchIfEmpty(Mono.just(2L));

                // BUG: Diagnostic contains:
                Flux.just(1).elementAt(0).defaultIfEmpty(1);
                // BUG: Diagnostic contains:
                Flux.just(1).elementAt(0, 2).single();

                // BUG: Diagnostic contains:
                Flux.just(1).hasElement(2).switchIfEmpty(Mono.just(true));

                // BUG: Diagnostic contains:
                Flux.just(1).hasElements().defaultIfEmpty(true);

                // BUG: Diagnostic contains:
                Flux.just(1).last().single();
                // BUG: Diagnostic contains:
                Flux.just(1).last(2).switchIfEmpty(Mono.just(3));

                // BUG: Diagnostic contains:
                Flux.just(1).reduceWith(() -> 0, Integer::sum).defaultIfEmpty(2);

                // BUG: Diagnostic contains:
                Flux.just(1).single().single();
                // BUG: Diagnostic contains:
                Flux.just(1).single(2).switchIfEmpty(Mono.just(3));

                Flux.just(1).reduce(Integer::sum).defaultIfEmpty(2);
                // BUG: Diagnostic contains:
                Flux.just(1).reduce(2, Integer::sum).single();

                // BUG: Diagnostic contains:
                Mono.just(1).defaultIfEmpty(1).switchIfEmpty(Mono.just(2));
                // BUG: Diagnostic contains:
                Mono.just(1).hasElement().defaultIfEmpty(true);
                // BUG: Diagnostic contains:
                Mono.just(1).single().single();
              }
            }
            """)
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(NonEmptyMono.class, getClass())
        .addInputLines(
            "A.java",
            """
            import static com.google.common.collect.ImmutableList.toImmutableList;

            import com.google.common.collect.ImmutableList;
            import reactor.core.publisher.Flux;
            import reactor.core.publisher.Mono;

            class A {
              void m() {
                Flux.just(1).collect(toImmutableList()).single();
                Flux.just(1).collect(toImmutableList()).defaultIfEmpty(ImmutableList.of());
                Flux.just(1).collect(toImmutableList()).switchIfEmpty(Mono.just(ImmutableList.of()));

                Mono.just(2).hasElement().single();
                Mono.just(2).hasElement().defaultIfEmpty(true);
                Mono.just(2).hasElement().switchIfEmpty(Mono.just(true));
              }
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import static com.google.common.collect.ImmutableList.toImmutableList;

            import com.google.common.collect.ImmutableList;
            import reactor.core.publisher.Flux;
            import reactor.core.publisher.Mono;

            class A {
              void m() {
                Flux.just(1).collect(toImmutableList());
                Flux.just(1).collect(toImmutableList());
                Flux.just(1).collect(toImmutableList());

                Mono.just(2).hasElement();
                Mono.just(2).hasElement();
                Mono.just(2).hasElement();
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
