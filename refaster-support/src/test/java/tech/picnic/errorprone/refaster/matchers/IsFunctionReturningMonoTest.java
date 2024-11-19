package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.jupiter.api.Test;

final class IsFunctionReturningMonoTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.function.Function;",
            "import java.util.function.Supplier;",
            "import reactor.core.publisher.Flux;",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  // BUG: Diagnostic contains:",
            "  Function<String, Mono<String>> positive = s -> Mono.just(s);",
            "",
            "  Function<String, Flux<String>> negative = s -> Flux.just(s);",
            "",
            "  Supplier<Mono<String>> negative2 = () -> Mono.just(\"s\");",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that simply delegates to {@link IsFunctionReturningMono}. */
  @BugPattern(summary = "Flags expressions matched by `IsFunctionReturningMono`", severity = ERROR)
  public static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    // XXX: This is a false positive reported by Checkstyle. See
    // https://github.com/checkstyle/checkstyle/issues/10161#issuecomment-1242732120.
    @SuppressWarnings("RedundantModifier")
    public MatcherTestChecker() {
      super(new IsFunctionReturningMono());
    }
  }
}
