package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.jupiter.api.Test;

final class ReturnsMonoTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.function.Function;",
            "import java.util.function.Supplier;",
            "import java.util.function.UnaryOperator;",
            "import org.reactivestreams.Publisher;",
            "import reactor.core.publisher.Flux;",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  String negative1() {",
            "    return toString();",
            "  }",
            "",
            "  Supplier<Mono<String>> negative2() {",
            "    return () -> Mono.just(\"s\");",
            "  }",
            "",
            "  Supplier<Mono<String>> negative3() {",
            "    return Mono::empty;",
            "  }",
            "",
            "  Supplier<Mono<String>> negative4() {",
            "    return negative3();",
            "  }",
            "",
            "  Function<String, String> negative5() {",
            "    return s -> s;",
            "  }",
            "",
            "  Function<String, String> negative6() {",
            "    return String::valueOf;",
            "  }",
            "",
            "  Function<String, String> negative7() {",
            "    return negative6();",
            "  }",
            "",
            "  Function<String, Publisher<String>> negative8() {",
            "    return s -> Flux.just(s);",
            "  }",
            "",
            "  Function<String, Publisher<String>> negative9() {",
            "    return Flux::just;",
            "  }",
            "",
            "  Function<String, Publisher<String>> negative10() {",
            "    return negative9();",
            "  }",
            "",
            "  Function<String, Flux<String>> negative11() {",
            "    return s -> Flux.just(s);",
            "  }",
            "",
            "  Function<String, Flux<String>> negative12() {",
            "    return Flux::just;",
            "  }",
            "",
            "  Function<String, Flux<String>> negative13() {",
            "    return negative12();",
            "  }",
            "",
            "  Function<String, Mono<String>> positive1() {",
            "    // BUG: Diagnostic contains:",
            "    return s -> Mono.just(s);",
            "  }",
            "",
            "  Function<String, Mono<String>> positive2() {",
            "    // BUG: Diagnostic contains:",
            "    return Mono::just;",
            "  }",
            "",
            "  Function<String, Mono<String>> positive3() {",
            "    // BUG: Diagnostic contains:",
            "    return positive2();",
            "  }",
            "",
            "  UnaryOperator<Mono<String>> positive4() {",
            "    // BUG: Diagnostic contains:",
            "    return m -> m;",
            "  }",
            "",
            "  UnaryOperator<Mono<String>> positive5() {",
            "    // BUG: Diagnostic contains:",
            "    return Mono::onErrorComplete;",
            "  }",
            "",
            "  UnaryOperator<Mono<String>> positive6() {",
            "    // BUG: Diagnostic contains:",
            "    return positive5();",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that simply delegates to {@link ReturnsMono}. */
  @BugPattern(summary = "Flags expressions matched by `ReturnsMono`", severity = ERROR)
  public static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    // XXX: This is a false positive reported by Checkstyle. See
    // https://github.com/checkstyle/checkstyle/issues/10161#issuecomment-1242732120.
    @SuppressWarnings("RedundantModifier")
    public MatcherTestChecker() {
      super(new ReturnsMono());
    }
  }
}
