package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class IsInstanceLambdaUsageTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(IsInstanceLambdaUsage.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import java.util.stream.Stream;
            import reactor.core.publisher.Flux;

            class A {
              void m() {
                Integer localVariable = 0;

                Stream.of(0).map(i -> i + 1);
                Stream.of(1).filter(Integer.class::isInstance);
                Stream.of(2).filter(i -> i.getClass() instanceof Class);
                Stream.of(3).filter(i -> localVariable instanceof Integer);
                // XXX: Ideally this case is also flagged. Pick this up in the context of merging the
                // `IsInstanceLambdaUsage` and `MethodReferenceUsage` checks, or introduce a separate check that
                // simplifies unnecessary block lambda expressions.
                Stream.of(4)
                    .filter(
                        i -> {
                          return localVariable instanceof Integer;
                        });
                Flux.just(5, "foo").distinctUntilChanged(v -> v, (a, b) -> a instanceof Integer);

                // BUG: Diagnostic contains:
                Stream.of(6).filter(i -> i instanceof Integer);
              }
            }
            """)
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(IsInstanceLambdaUsage.class, getClass())
        .addInputLines(
            "A.java",
            """
            import java.util.stream.Stream;

            class A {
              void m() {
                Stream.of(1).filter(i -> i instanceof Integer);
              }
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import java.util.stream.Stream;

            class A {
              void m() {
                Stream.of(1).filter(Integer.class::isInstance);
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
