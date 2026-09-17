package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class FluxGroupByUsageTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(FluxGroupByUsage.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.function.BiFunction;",
            "import java.util.function.Consumer;",
            "import java.util.function.Function;",
            "import reactor.core.CoreSubscriber;",
            "import reactor.core.publisher.Flux;",
            "import reactor.core.publisher.GroupedFlux;",
            "",
            "class A {",
            "  void m() {",
            "    new Grouper().groupBy();",
            "    this.<Grouper>sink(Grouper::groupBy);",
            "    new CustomFlux().groupBy();",
            "",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).groupBy(Object::toString);",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(2).groupBy(Object::toString, 1);",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(3).groupBy(Object::toString, Object::toString);",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(4).groupBy(Object::toString, Object::toString, 1);",
            "",
            "    // BUG: Diagnostic contains:",
            "    this.<Integer, Flux<GroupedFlux<Integer, Integer>>>sink(Flux::groupBy);",
            "  }",
            "",
            "  @SuppressWarnings(\"FluxGroupByUsage\")",
            "  void suppressed() {",
            "    Flux.just(5).groupBy(i -> i % 2 == 0);",
            "  }",
            "",
            "  private <T, R> void sink(BiFunction<Flux<T>, Function<T, T>, R> function) {}",
            "  private <T> void sink(Consumer<T> consumer) {}",
            "",
            "  private static final class Grouper {",
            "    void groupBy() {}",
            "  }",
            "",
            "  private static final class CustomFlux extends Flux<Integer> {",
            "    void groupBy() {}",
            "",
            "    @Override",
            "    public void subscribe(CoreSubscriber<? super Integer> subscriber) {}",
            "  }",
            "}")
        .doTest();
  }
}
